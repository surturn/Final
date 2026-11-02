package com.sydney.sydney.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.NewHostel
import com.sydney.sydney.data.model.StudentEntry
import com.sydney.sydney.data.model.UserProfile
import com.sydney.sydney.data.repo.AppException
import com.sydney.sydney.data.repo.AuthRepository
import com.sydney.sydney.data.repo.HostelRepository
import com.sydney.sydney.data.repo.Messages
import com.sydney.sydney.data.repo.ProfileRepository
import com.sydney.sydney.data.repo.StudentRepository
import com.sydney.sydney.domain.Validators
import java.util.UUID
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

private const val DIRECTORY_LIMIT = 500L
private const val HOSTEL_LIMIT = 50L

class FirebaseAuthRepository(private val auth: FirebaseAuth) : AuthRepository {

    override val currentUserId: String? get() = auth.currentUser?.uid

    override val currentEmail: String? get() = auth.currentUser?.email

    override val authState: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun signIn(email: String, password: String): Result<String> = firebaseCall {
        auth.signInWithEmailAndPassword(email.trim(), password).await().user?.uid
            ?: throw AppException(Messages.GENERIC)
    }

    override suspend fun signUp(email: String, password: String): Result<String> = firebaseCall {
        auth.createUserWithEmailAndPassword(email.trim(), password).await().user?.uid
            ?: throw AppException(Messages.GENERIC)
    }

    override fun signOut() = auth.signOut()
}

class FirestoreProfileRepository(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val images: ImageProcessor,
) : ProfileRepository {

    override suspend fun get(uid: String): Result<UserProfile?> = firebaseCall {
        db.collection(USERS).document(uid).get().await().takeIf { it.exists() }?.toUserProfile()
    }

    override suspend fun create(profile: UserProfile): Result<Unit> = firebaseCall {
        val batch = db.batch()
        batch.set(
            db.collection(USERS).document(profile.uid),
            mapOf(
                "displayName" to profile.displayName.trim(),
                "email" to profile.email,
                "campus" to profile.campus.trim(),
                "phone" to Validators.normalizePhone(profile.phone),
                "location" to profile.location.trim(),
                "landlordStatus" to profile.landlordStatus.wire,
                "createdAt" to profile.createdAt,
            ),
        )
        batch.set(
            db.collection(STUDENTS).document(profile.uid),
            mapOf(
                "displayName" to profile.displayName.trim(),
                "campus" to profile.campus.trim(),
                "location" to profile.location.trim(),
                "createdAt" to profile.createdAt,
            ),
        )
        batch.commit().await()
        Unit
    }

    override suspend fun update(profile: UserProfile, photoUri: String?): Result<Unit> = firebaseCall {
        val uid = profile.uid
        val studentDoc = db.collection(STUDENTS).document(uid)

        var newPhotoUrl: String? = null
        var oldPhotoUrl = ""
        if (photoUri != null) {
            oldPhotoUrl = studentDoc.get().await().getString("photoUrl").orEmpty()
            newPhotoUrl = upload("$STUDENTS/$uid/${UUID.randomUUID()}.jpg", images.compress(photoUri))
        }

        val directory = mutableMapOf<String, Any>(
            "displayName" to profile.displayName.trim(),
            "campus" to profile.campus.trim(),
            "location" to profile.location.trim(),
            "createdAt" to profile.createdAt,
        )
        if (newPhotoUrl != null) directory["photoUrl"] = newPhotoUrl

        try {
            val batch = db.batch()
            batch.update(
                db.collection(USERS).document(uid),
                mapOf(
                    "displayName" to profile.displayName.trim(),
                    "campus" to profile.campus.trim(),
                    "phone" to Validators.normalizePhone(profile.phone),
                    "location" to profile.location.trim(),
                ),
            )
            batch.set(studentDoc, directory, SetOptions.merge())
            batch.commit().await()
        } catch (e: Exception) {
            newPhotoUrl?.let { deleteQuietly(it) }
            throw e
        }
        if (newPhotoUrl != null && oldPhotoUrl.isNotEmpty()) deleteQuietly(oldPhotoUrl)
    }

    override suspend fun photoUrl(uid: String): Result<String> = firebaseCall {
        db.collection(STUDENTS).document(uid).get().await().getString("photoUrl").orEmpty()
    }

    override suspend fun applyForLandlord(uid: String): Result<Unit> = firebaseCall {
        db.collection(USERS).document(uid).update("landlordStatus", "pending").await()
        Unit
    }

    private suspend fun upload(path: String, bytes: ByteArray): String {
        val ref = storage.reference.child(path)
        ref.putBytes(bytes, StorageMetadata.Builder().setContentType("image/jpeg").build()).await()
        return ref.downloadUrl.await().toString()
    }

    private suspend fun deleteQuietly(url: String) {
        try {
            storage.getReferenceFromUrl(url).delete().await()
        } catch (e: Exception) {
            // Best effort cleanup of an unused photo. A leftover file is harmless.
        }
    }
}

class FirestoreStudentRepository(private val db: FirebaseFirestore) : StudentRepository {

    override fun observeDirectory(): Flow<List<StudentEntry>> = callbackFlow {
        val registration = db.collection(STUDENTS)
            .orderBy("displayName")
            .limit(DIRECTORY_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toAppException())
                    return@addSnapshotListener
                }
                val students = snapshot?.documents?.mapNotNull { it.toStudent() }.orEmpty()
                trySend(students.sortedBy { it.displayName.lowercase() })
            }
        awaitClose { registration.remove() }
    }
}

class FirestoreHostelRepository(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val images: ImageProcessor,
) : HostelRepository {

    override fun observeHostels(): Flow<List<Hostel>> = callbackFlow {
        val registration = db.collection(HOSTELS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(HOSTEL_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error.toAppException())
                    return@addSnapshotListener
                }
                trySend(snapshot?.documents?.mapNotNull { it.toHostel() }.orEmpty())
            }
        awaitClose { registration.remove() }
    }

    override suspend fun get(id: String): Result<Hostel?> = firebaseCall {
        db.collection(HOSTELS).document(id).get().await().takeIf { it.exists() }?.toHostel()
    }

    override suspend fun add(ownerId: String, hostel: NewHostel): Result<String> = firebaseCall {
        val bytes = images.compress(hostel.imageUri)
        val ref = storage.reference.child("$HOSTELS/$ownerId/${UUID.randomUUID()}.jpg")
        ref.putBytes(bytes, StorageMetadata.Builder().setContentType("image/jpeg").build()).await()
        val imageUrl = ref.downloadUrl.await().toString()

        val document = db.collection(HOSTELS).document()
        try {
            document.set(
                mapOf(
                    "name" to hostel.name.trim(),
                    "description" to hostel.description.trim(),
                    "priceKes" to hostel.priceKes,
                    "location" to hostel.location.trim(),
                    "contactPhone" to Validators.normalizePhone(hostel.contactPhone),
                    "imageUrl" to imageUrl,
                    "ownerId" to ownerId,
                    "createdAt" to System.currentTimeMillis(),
                ),
            ).await()
        } catch (e: Exception) {
            // Do not leave an orphaned photo behind when the listing itself was refused.
            try { ref.delete().await() } catch (ignored: Exception) { }
            throw e
        }
        document.id
    }
}

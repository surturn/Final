package com.sydney.sydney.data.firebase

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.data.model.StudentEntry
import com.sydney.sydney.data.model.UserProfile
import com.sydney.sydney.data.repo.AppException
import com.sydney.sydney.data.repo.Messages
import com.sydney.sydney.domain.AuthErrors
import kotlin.coroutines.cancellation.CancellationException

internal const val USERS = "users"
internal const val STUDENTS = "students"
internal const val HOSTELS = "hostels"

/** Runs [block] and turns any failure into a [Result] carrying a user-readable [AppException]. */
internal suspend fun <T> firebaseCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toAppException())
    }

internal fun Throwable.toAppException(): AppException = when (this) {
    is AppException -> this
    is FirebaseAuthException -> AppException(AuthErrors.message(errorCode), this)
    is FirebaseNetworkException -> AppException(Messages.NETWORK, this)
    is FirebaseFirestoreException -> AppException(
        when (code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> Messages.DENIED
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> Messages.SIGN_IN_AGAIN
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> Messages.NETWORK
            else -> Messages.GENERIC
        },
        this,
    )
    is StorageException -> AppException(
        when (errorCode) {
            StorageException.ERROR_NOT_AUTHORIZED -> Messages.DENIED
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> Messages.NETWORK
            else -> "Image upload failed. Please try again"
        },
        this,
    )
    else -> AppException(Messages.GENERIC, this)
}

internal fun DocumentSnapshot.toUserProfile(): UserProfile? {
    val name = getString("displayName") ?: return null
    return UserProfile(
        uid = id,
        displayName = name,
        email = getString("email").orEmpty(),
        campus = getString("campus").orEmpty(),
        phone = getString("phone").orEmpty(),
        location = getString("location").orEmpty(),
        landlordStatus = LandlordStatus.fromWire(getString("landlordStatus")),
        createdAt = getLong("createdAt") ?: 0L,
    )
}

internal fun DocumentSnapshot.toStudent(): StudentEntry? {
    val name = getString("displayName") ?: return null
    return StudentEntry(
        uid = id,
        displayName = name,
        campus = getString("campus").orEmpty(),
        location = getString("location").orEmpty(),
        photoUrl = getString("photoUrl").orEmpty(),
    )
}

/** Returns null for malformed documents (for example listings from before the price was numeric). */
internal fun DocumentSnapshot.toHostel(): Hostel? {
    val name = getString("name") ?: return null
    val price = getLong("priceKes") ?: return null
    return Hostel(
        id = id,
        name = name,
        description = getString("description").orEmpty(),
        priceKes = price,
        location = getString("location").orEmpty(),
        contactPhone = getString("contactPhone").orEmpty(),
        imageUrl = getString("imageUrl").orEmpty(),
        ownerId = getString("ownerId").orEmpty(),
        createdAt = getLong("createdAt") ?: 0L,
    )
}

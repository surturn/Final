package com.sydney.sydney.data.repo

import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.NewHostel
import com.sydney.sydney.data.model.StudentEntry
import com.sydney.sydney.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** The signed-in user's uid, or null when signed out. */
    val currentUserId: String?

    val currentEmail: String?

    /** Emits the current uid (or null) now and again on every sign in or sign out. */
    val authState: Flow<String?>

    suspend fun signIn(email: String, password: String): Result<String>

    suspend fun signUp(email: String, password: String): Result<String>

    fun signOut()
}

interface ProfileRepository {
    suspend fun get(uid: String): Result<UserProfile?>

    /** Creates users/{uid} and the matching students/{uid} directory entry. */
    suspend fun create(profile: UserProfile): Result<Unit>

    /** Saves edits to the profile and directory entry, uploading [photoUri] when given. */
    suspend fun update(profile: UserProfile, photoUri: String?): Result<Unit>

    /** The signed-in user's own directory photo URL, or empty when none is set. */
    suspend fun photoUrl(uid: String): Result<String>

    /** Moves landlordStatus from none to pending. An admin approves it from the console. */
    suspend fun applyForLandlord(uid: String): Result<Unit>
}

interface StudentRepository {
    fun observeDirectory(): Flow<List<StudentEntry>>
}

interface HostelRepository {
    fun observeHostels(): Flow<List<Hostel>>

    suspend fun get(id: String): Result<Hostel?>

    /** Uploads the photo first, then writes the listing, so a failed upload leaves no orphan. */
    suspend fun add(ownerId: String, hostel: NewHostel): Result<String>
}

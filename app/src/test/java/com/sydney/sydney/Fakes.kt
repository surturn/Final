package com.sydney.sydney

import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.data.model.NewHostel
import com.sydney.sydney.data.model.StudentEntry
import com.sydney.sydney.data.model.UserProfile
import com.sydney.sydney.data.repo.AppException
import com.sydney.sydney.data.repo.AuthRepository
import com.sydney.sydney.data.repo.HostelRepository
import com.sydney.sydney.data.repo.ProfileRepository
import com.sydney.sydney.data.repo.StudentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Runs viewModelScope work immediately so tests can assert right after calling an action. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeAuth(uid: String? = null, override var currentEmail: String? = null) : AuthRepository {
    private val uidFlow = MutableStateFlow(uid)
    var signInResult: Result<String> = Result.success("u1")
    var signUpResult: Result<String> = Result.success("u1")
    var signInCalls = 0
    var reauthResult: Result<Unit> = Result.success(Unit)
    var deleteResult: Result<Unit> = Result.success(Unit)
    var accountDeleted = false

    override val currentUserId: String? get() = uidFlow.value
    override val authState: Flow<String?> = uidFlow

    override suspend fun signIn(email: String, password: String): Result<String> {
        signInCalls++
        signInResult.onSuccess { uidFlow.value = it }
        return signInResult
    }

    override suspend fun signUp(email: String, password: String): Result<String> {
        signUpResult.onSuccess { uidFlow.value = it }
        return signUpResult
    }

    override fun signOut() {
        uidFlow.value = null
    }

    override suspend fun reauthenticate(password: String): Result<Unit> = reauthResult

    override suspend fun deleteAccount(): Result<Unit> {
        deleteResult.onSuccess {
            accountDeleted = true
            uidFlow.value = null
        }
        return deleteResult
    }

    fun setUser(uid: String?) {
        uidFlow.value = uid
    }
}

class FakeProfiles : ProfileRepository {
    val store = mutableMapOf<String, UserProfile>()
    var photo = ""
    var failGet: String? = null
    var failCreate: String? = null
    var created: UserProfile? = null
    var updated: UserProfile? = null
    var updatedPhoto: String? = null
    var applied = 0
    var deletedUid: String? = null
    var failDelete: String? = null

    override suspend fun get(uid: String): Result<UserProfile?> =
        failGet?.let { Result.failure(AppException(it)) } ?: Result.success(store[uid])

    override suspend fun create(profile: UserProfile): Result<Unit> {
        failCreate?.let { return Result.failure(AppException(it)) }
        created = profile
        store[profile.uid] = profile
        return Result.success(Unit)
    }

    override suspend fun update(profile: UserProfile, photoUri: String?): Result<Unit> {
        updated = profile
        updatedPhoto = photoUri
        store[profile.uid] = profile
        return Result.success(Unit)
    }

    override suspend fun photoUrl(uid: String): Result<String> = Result.success(photo)

    override suspend fun deleteAllData(uid: String): Result<Unit> {
        failDelete?.let { return Result.failure(AppException(it)) }
        deletedUid = uid
        store.remove(uid)
        return Result.success(Unit)
    }

    override suspend fun applyForLandlord(uid: String): Result<Unit> {
        applied++
        store[uid]?.let { store[uid] = it.copy(landlordStatus = LandlordStatus.PENDING) }
        return Result.success(Unit)
    }
}

class FakeHostels(var items: List<Hostel> = emptyList()) : HostelRepository {
    var failWith: String? = null
    var added: NewHostel? = null
    var addedOwner: String? = null
    var addResult: Result<String> = Result.success("h1")

    override fun observeHostels(): Flow<List<Hostel>> = flow {
        failWith?.let { throw AppException(it) }
        emit(items)
    }

    override suspend fun get(id: String): Result<Hostel?> = Result.success(items.firstOrNull { it.id == id })

    override suspend fun add(ownerId: String, hostel: NewHostel): Result<String> {
        added = hostel
        addedOwner = ownerId
        return addResult
    }
}

class FakeStudents(var items: List<StudentEntry> = emptyList()) : StudentRepository {
    var failWith: String? = null

    override fun observeDirectory(): Flow<List<StudentEntry>> = flow {
        failWith?.let { throw AppException(it) }
        emit(items)
    }
}

fun hostel(id: String, name: String = "Sunrise", location: String = "Gate B", price: Long = 6500) =
    Hostel(id, name, "A clean hostel with water and wifi", price, location, "0712345678", "https://img/$id", "owner")

fun student(uid: String, name: String, campus: String = "MMU", location: String = "Rongai") =
    StudentEntry(uid, name, campus, location)

fun profile(uid: String = "u1", status: LandlordStatus = LandlordStatus.NONE, phone: String = "") =
    UserProfile(uid, "Amina Otieno", "amina@example.com", "MMU", phone, "Rongai", status, 1L)

package com.sydney.sydney.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.data.model.UserProfile
import com.sydney.sydney.data.repo.AuthRepository
import com.sydney.sydney.data.repo.Messages
import com.sydney.sydney.data.repo.ProfileRepository
import com.sydney.sydney.domain.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    /** False when the account has no saved profile yet, for example after an interrupted sign up. */
    val exists: Boolean = false,
    val email: String = "",
    val name: String = "",
    val campus: String = "",
    val phone: String = "",
    val location: String = "",
    val photoUrl: String = "",
    val newPhotoUri: String? = null,
    val status: LandlordStatus = LandlordStatus.NONE,
    val nameError: String? = null,
    val campusError: String? = null,
    val phoneError: String? = null,
    val locationError: String? = null,
    val error: String? = null,
    val message: String? = null,
    val applying: Boolean = false,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
)

class ProfileViewModel(
    private val auth: AuthRepository,
    private val profiles: ProfileRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private var createdAt: Long = 0L

    init {
        load()
    }

    fun load() {
        val uid = auth.currentUserId
        if (uid == null) {
            _state.update { it.copy(loading = false, error = Messages.SIGN_IN_AGAIN) }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val profile = profiles.get(uid)
            val failure = profile.exceptionOrNull()
            if (failure != null) {
                _state.update { it.copy(loading = false, error = failure.message ?: Messages.GENERIC) }
                return@launch
            }
            val existing = profile.getOrNull()
            if (existing == null) {
                val email = auth.currentEmail.orEmpty()
                createdAt = clock()
                _state.update {
                    it.copy(loading = false, exists = false, email = email, name = email.substringBefore('@'))
                }
                return@launch
            }
            createdAt = existing.createdAt
            val photo = profiles.photoUrl(uid).getOrDefault("")
            _state.update {
                it.copy(
                    loading = false,
                    exists = true,
                    email = existing.email,
                    name = existing.displayName,
                    campus = existing.campus,
                    phone = existing.phone,
                    location = existing.location,
                    photoUrl = photo,
                    status = existing.landlordStatus,
                )
            }
        }
    }

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, error = null) }

    fun onCampus(value: String) = _state.update { it.copy(campus = value, campusError = null, error = null) }

    fun onPhone(value: String) = _state.update { it.copy(phone = value, phoneError = null, error = null) }

    fun onLocation(value: String) = _state.update { it.copy(location = value, locationError = null, error = null) }

    fun onPhoto(uri: String) = _state.update { it.copy(newPhotoUri = uri, error = null) }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    fun save() {
        val s = _state.value
        val uid = auth.currentUserId
        if (s.saving || s.loading) return
        if (uid == null) {
            _state.update { it.copy(error = Messages.SIGN_IN_AGAIN) }
            return
        }
        val nameError = Validators.displayName(s.name)
        val campusError = Validators.campus(s.campus)
        val phoneError = Validators.phone(s.phone)
        val locationError = Validators.location(s.location)
        if (listOf(nameError, campusError, phoneError, locationError).any { it != null }) {
            _state.update {
                it.copy(nameError = nameError, campusError = campusError, phoneError = phoneError, locationError = locationError)
            }
            return
        }
        val profile = UserProfile(
            uid = uid,
            displayName = s.name.trim(),
            email = s.email,
            campus = s.campus.trim(),
            phone = Validators.normalizePhone(s.phone),
            location = s.location.trim(),
            landlordStatus = s.status,
            createdAt = createdAt,
        )
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val created = if (s.exists) Result.success(Unit) else profiles.create(profile)
            val saved = if (created.isSuccess) {
                // A brand new profile has no photo yet, so only call update when there is one to add.
                if (s.exists || s.newPhotoUri != null) profiles.update(profile, s.newPhotoUri) else created
            } else {
                created
            }
            saved
                .onSuccess {
                    val url = if (s.newPhotoUri != null) profiles.photoUrl(uid).getOrDefault(s.photoUrl) else s.photoUrl
                    _state.update {
                        it.copy(saving = false, exists = true, photoUrl = url, newPhotoUri = null, message = "Profile saved")
                    }
                }
                .onFailure { e -> _state.update { it.copy(saving = false, error = e.message ?: Messages.GENERIC) } }
        }
    }

    fun applyForLandlord() {
        val s = _state.value
        val uid = auth.currentUserId ?: return
        if (!s.exists || s.applying || s.status != LandlordStatus.NONE) return
        _state.update { it.copy(applying = true, error = null) }
        viewModelScope.launch {
            profiles.applyForLandlord(uid)
                .onSuccess {
                    _state.update {
                        it.copy(applying = false, status = LandlordStatus.PENDING, message = "Request sent. We will review it soon")
                    }
                }
                .onFailure { e -> _state.update { it.copy(applying = false, error = e.message ?: Messages.GENERIC) } }
        }
    }

    /** Deletes the account for good: confirm the password, remove the data, then remove the sign in. */
    fun deleteAccount(password: String) {
        val uid = auth.currentUserId ?: return
        if (_state.value.deleting) return
        if (password.isEmpty()) {
            _state.update { it.copy(error = "Enter your password to confirm") }
            return
        }
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            val steps: List<suspend () -> Result<Unit>> = listOf(
                { auth.reauthenticate(password) },
                { profiles.deleteAllData(uid) },
                { auth.deleteAccount() },
            )
            for (step in steps) {
                val failure = step().exceptionOrNull()
                if (failure != null) {
                    _state.update { it.copy(deleting = false, error = failure.message ?: Messages.GENERIC) }
                    return@launch
                }
            }
            _state.update { it.copy(deleting = false, deleted = true) }
        }
    }
}

package com.sydney.sydney.ui.auth

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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

class LoginViewModel(private val auth: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, error = null) }

    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, error = null) }

    fun submit() {
        val current = _state.value
        if (current.loading) return
        val emailError = Validators.email(current.email)
        val passwordError = if (current.password.isEmpty()) "Password is required" else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            auth.signIn(current.email, current.password)
                .onSuccess { _state.update { it.copy(loading = false, done = true) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message ?: Messages.GENERIC) } }
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val isLandlord: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmationError: String? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

class RegisterViewModel(
    private val auth: AuthRepository,
    private val profiles: ProfileRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, error = null) }

    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, error = null) }

    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, error = null) }

    fun onConfirmation(value: String) =
        _state.update { it.copy(confirmation = value, confirmationError = null, error = null) }

    fun onLandlord(value: Boolean) = _state.update { it.copy(isLandlord = value) }

    fun submit() {
        val s = _state.value
        if (s.loading) return
        val nameError = Validators.displayName(s.name)
        val emailError = Validators.email(s.email)
        val passwordError = Validators.password(s.password)
        val confirmationError = Validators.passwordConfirmation(s.password, s.confirmation)
        if (listOf(nameError, emailError, passwordError, confirmationError).any { it != null }) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmationError = confirmationError,
                )
            }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            val signUp = auth.signUp(s.email, s.password)
            val uid = signUp.getOrNull()
            if (uid == null) {
                _state.update { it.copy(loading = false, error = signUp.exceptionOrNull()?.message ?: Messages.GENERIC) }
                return@launch
            }
            val profile = UserProfile(
                uid = uid,
                displayName = s.name.trim(),
                email = auth.currentEmail ?: s.email.trim().lowercase(),
                landlordStatus = if (s.isLandlord) LandlordStatus.PENDING else LandlordStatus.NONE,
                createdAt = clock(),
            )
            // If saving the profile fails the account still exists. My profile completes it later,
            // so the user is never stuck on this screen with a half-created account.
            profiles.create(profile)
            _state.update { it.copy(loading = false, done = true) }
        }
    }
}

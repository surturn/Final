package com.sydney.sydney.ui.hostels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.data.model.NewHostel
import com.sydney.sydney.data.repo.AuthRepository
import com.sydney.sydney.data.repo.HostelRepository
import com.sydney.sydney.data.repo.Messages
import com.sydney.sydney.data.repo.ProfileRepository
import com.sydney.sydney.domain.Filters
import com.sydney.sydney.domain.Validators
import com.sydney.sydney.ui.util.Loadable
import com.sydney.sydney.ui.util.asLoadable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HostelListUiState(
    val loading: Boolean = true,
    val hostels: List<Hostel> = emptyList(),
    val total: Int = 0,
    val query: String = "",
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HostelListViewModel(repository: HostelRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val attempt = MutableStateFlow(0)

    val state: StateFlow<HostelListUiState> =
        combine(attempt.flatMapLatest { repository.observeHostels().asLoadable() }, query) { loadable, q ->
            val all = loadable.items.orEmpty()
            HostelListUiState(
                loading = loadable.loading,
                hostels = Filters.hostels(all, q),
                total = all.size,
                query = q,
                error = loadable.error,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HostelListUiState())

    fun onQuery(value: String) {
        query.value = value
    }

    fun retry() = attempt.update { it + 1 }
}

data class HostelDetailUiState(
    val loading: Boolean = true,
    val hostel: Hostel? = null,
    val error: String? = null,
)

class HostelDetailViewModel(
    private val repository: HostelRepository,
    private val hostelId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(HostelDetailUiState())
    val state: StateFlow<HostelDetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = HostelDetailUiState(loading = true)
        viewModelScope.launch {
            repository.get(hostelId)
                .onSuccess { hostel ->
                    _state.value = HostelDetailUiState(
                        loading = false,
                        hostel = hostel,
                        error = if (hostel == null) "This hostel is no longer listed" else null,
                    )
                }
                .onFailure { e -> _state.value = HostelDetailUiState(loading = false, error = e.message ?: Messages.GENERIC) }
        }
    }
}

data class AddHostelUiState(
    val checking: Boolean = true,
    val status: LandlordStatus = LandlordStatus.NONE,
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val location: String = "",
    val phone: String = "",
    val imageUri: String? = null,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val priceError: String? = null,
    val phoneError: String? = null,
    val imageError: String? = null,
    val submitting: Boolean = false,
    val error: String? = null,
    val done: Boolean = false,
)

class AddHostelViewModel(
    private val auth: AuthRepository,
    private val profiles: ProfileRepository,
    private val hostels: HostelRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddHostelUiState())
    val state: StateFlow<AddHostelUiState> = _state.asStateFlow()

    init {
        checkStatus()
    }

    fun checkStatus() {
        val uid = auth.currentUserId
        if (uid == null) {
            _state.update { it.copy(checking = false, error = Messages.SIGN_IN_AGAIN) }
            return
        }
        _state.update { it.copy(checking = true, error = null) }
        viewModelScope.launch {
            profiles.get(uid)
                .onSuccess { profile ->
                    _state.update {
                        it.copy(
                            checking = false,
                            status = profile?.landlordStatus ?: LandlordStatus.NONE,
                            phone = if (it.phone.isEmpty()) profile?.phone.orEmpty() else it.phone,
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(checking = false, error = e.message) } }
        }
    }

    fun onName(value: String) = _state.update { it.copy(name = value, nameError = null, error = null) }

    fun onDescription(value: String) = _state.update { it.copy(description = value, descriptionError = null, error = null) }

    fun onPrice(value: String) = _state.update { it.copy(price = value, priceError = null, error = null) }

    fun onLocation(value: String) = _state.update { it.copy(location = value, error = null) }

    fun onPhone(value: String) = _state.update { it.copy(phone = value, phoneError = null, error = null) }

    fun onImage(uri: String) = _state.update { it.copy(imageUri = uri, imageError = null, error = null) }

    fun submit() {
        val s = _state.value
        val uid = auth.currentUserId
        if (s.submitting || s.status != LandlordStatus.APPROVED) return
        if (uid == null) {
            _state.update { it.copy(error = Messages.SIGN_IN_AGAIN) }
            return
        }
        val nameError = Validators.hostelName(s.name)
        val descriptionError = Validators.hostelDescription(s.description)
        val priceError = Validators.price(s.price)
        val phoneError = Validators.requiredPhone(s.phone)
        val imageError = if (s.imageUri == null) "Add a photo of the hostel" else null
        if (listOf(nameError, descriptionError, priceError, phoneError, imageError).any { it != null }) {
            _state.update {
                it.copy(
                    nameError = nameError,
                    descriptionError = descriptionError,
                    priceError = priceError,
                    phoneError = phoneError,
                    imageError = imageError,
                )
            }
            return
        }
        val listing = NewHostel(
            name = s.name,
            description = s.description,
            priceKes = requireNotNull(Validators.parsePrice(s.price)),
            location = s.location,
            contactPhone = s.phone,
            imageUri = requireNotNull(s.imageUri),
        )
        _state.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            hostels.add(uid, listing)
                .onSuccess { _state.update { it.copy(submitting = false, done = true) } }
                .onFailure { e -> _state.update { it.copy(submitting = false, error = e.message ?: Messages.GENERIC) } }
        }
    }
}

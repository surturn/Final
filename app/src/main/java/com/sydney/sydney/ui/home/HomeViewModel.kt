package com.sydney.sydney.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.data.model.UserProfile
import com.sydney.sydney.data.repo.AuthRepository
import com.sydney.sydney.data.repo.HostelRepository
import com.sydney.sydney.data.repo.RecentlyViewedStore
import com.sydney.sydney.data.repo.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val uid: String? = null,
    val profile: UserProfile? = null,
    val error: String? = null,
    val applying: Boolean = false,
    /** Hostels this user opened recently, newest first. Always empty for a guest. */
    val recent: List<Hostel> = emptyList(),
) {
    /** True when the profile is missing details that make it useful to other students and landlords. */
    val profileIncomplete: Boolean
        get() = profile != null && (profile.campus.isBlank() || profile.phone.isBlank() || profile.location.isBlank())
}

class HomeViewModel(
    private val auth: AuthRepository,
    private val profiles: ProfileRepository,
    private val hostels: HostelRepository,
    private val recentStore: RecentlyViewedStore,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val refreshTick = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            combine(auth.authState, refreshTick) { uid, _ -> uid }.collectLatest { uid -> load(uid) }
        }
    }

    private suspend fun load(uid: String?) {
        if (uid == null) {
            _state.value = HomeUiState(loading = false)
            return
        }
        // A different account signed in: drop the previous account's profile straight away.
        if (_state.value.uid != uid) _state.value = HomeUiState(loading = true, uid = uid)
        profiles.get(uid)
            .onSuccess { profile -> _state.update { it.copy(loading = false, profile = profile, error = null) } }
            .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        loadRecent(uid)
    }

    /** Resolves the recently opened ids to hostels, skipping any that were removed or cannot be read. */
    private suspend fun loadRecent(uid: String) {
        val found = recentStore.ids(uid).mapNotNull { id -> hostels.get(id).getOrNull() }
        _state.update { if (it.uid == uid) it.copy(recent = found) else it }
    }

    /** Re-reads the profile, for example when the app returns to the foreground after an approval. */
    fun refresh() = refreshTick.update { it + 1 }

    fun signOut() = auth.signOut()

    fun applyForLandlord() {
        val current = _state.value
        val uid = current.uid ?: return
        if (current.applying || current.profile?.landlordStatus != LandlordStatus.NONE) return
        _state.update { it.copy(applying = true, error = null) }
        viewModelScope.launch {
            profiles.applyForLandlord(uid)
                .onSuccess {
                    _state.update {
                        it.copy(
                            applying = false,
                            profile = it.profile?.copy(landlordStatus = LandlordStatus.PENDING),
                        )
                    }
                }
                .onFailure { e -> _state.update { it.copy(applying = false, error = e.message) } }
        }
    }
}

package com.sydney.sydney.ui.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sydney.sydney.data.model.StudentEntry
import com.sydney.sydney.data.repo.StudentRepository
import com.sydney.sydney.domain.Filters
import com.sydney.sydney.ui.util.asLoadable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class StudentsUiState(
    val loading: Boolean = true,
    val students: List<StudentEntry> = emptyList(),
    val query: String = "",
    val error: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class StudentsViewModel(repository: StudentRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val attempt = MutableStateFlow(0)

    val state: StateFlow<StudentsUiState> =
        combine(attempt.flatMapLatest { repository.observeDirectory().asLoadable() }, query) { loadable, q ->
            StudentsUiState(
                loading = loadable.loading,
                students = Filters.students(loadable.items.orEmpty(), q),
                query = q,
                error = loadable.error,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StudentsUiState())

    fun onQuery(value: String) {
        query.value = value
    }

    fun retry() = attempt.update { it + 1 }
}

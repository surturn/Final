package com.sydney.sydney.ui.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sydney.sydney.data.repo.Messages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/** Builds a ViewModel with constructor arguments without needing one factory class per screen. */
inline fun <reified VM : ViewModel> viewModelFactory(crossinline build: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = build() as T
    }

/** A list that is still loading (both fields null), loaded ([items]) or failed ([error]). */
data class Loadable<T>(val items: List<T>? = null, val error: String? = null) {
    val loading: Boolean get() = items == null && error == null
}

fun <T> Flow<List<T>>.asLoadable(): Flow<Loadable<T>> =
    map { Loadable(items = it) }
        .catch { emit(Loadable(error = it.message ?: Messages.GENERIC)) }
        .onStart { emit(Loadable()) }

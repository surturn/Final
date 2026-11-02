package com.sydney.sydney.data.local

import android.content.Context
import com.sydney.sydney.data.repo.RecentlyViewedStore

private const val MAX_RECENT = 5

/** Keeps the last few opened hostel ids per account in app-private preferences. */
class SharedPrefsRecentlyViewed(context: Context) : RecentlyViewedStore {
    private val prefs = context.getSharedPreferences("recently_viewed", Context.MODE_PRIVATE)

    override fun ids(uid: String): List<String> =
        prefs.getString(uid, "").orEmpty().split(',').filter { it.isNotEmpty() }

    override fun record(uid: String, hostelId: String) {
        val updated = (listOf(hostelId) + ids(uid).filter { it != hostelId }).take(MAX_RECENT)
        prefs.edit().putString(uid, updated.joinToString(",")).apply()
    }
}

package com.sydney.sydney.domain

import com.sydney.sydney.data.model.Hostel
import com.sydney.sydney.data.model.StudentEntry

/** Client side search. The lists are capped (50 hostels, 500 students) so a linear scan is cheap. */
object Filters {
    fun hostels(all: List<Hostel>, query: String): List<Hostel> {
        val q = query.trim()
        if (q.isEmpty()) return all
        return all.filter { it.name.contains(q, ignoreCase = true) || it.location.contains(q, ignoreCase = true) }
    }

    fun students(all: List<StudentEntry>, query: String): List<StudentEntry> {
        val q = query.trim()
        if (q.isEmpty()) return all
        return all.filter {
            it.displayName.contains(q, ignoreCase = true) ||
                it.campus.contains(q, ignoreCase = true) ||
                it.location.contains(q, ignoreCase = true)
        }
    }
}

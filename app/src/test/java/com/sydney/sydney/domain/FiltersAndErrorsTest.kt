package com.sydney.sydney.domain

import com.sydney.sydney.hostel
import com.sydney.sydney.student
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FiltersAndErrorsTest {
    private val hostels = listOf(hostel("1", "Sunrise", "Gate B"), hostel("2", "Moonlight", "Rongai"))
    private val students = listOf(student("1", "Amina", "MMU", "Rongai"), student("2", "Brian", "UoN", "Kilimani"))

    @Test fun blankQueryReturnsTheSameList() {
        assertSame(hostels, Filters.hostels(hostels, "  "))
        assertSame(students, Filters.students(students, ""))
    }

    @Test fun hostelsMatchNameOrAreaIgnoringCase() {
        assertEquals(listOf("1"), Filters.hostels(hostels, "SUN").map { it.id })
        assertEquals(listOf("2"), Filters.hostels(hostels, "rongai").map { it.id })
        assertEquals(emptyList<String>(), Filters.hostels(hostels, "zzz").map { it.id })
    }

    @Test fun studentsMatchNameCampusOrArea() {
        assertEquals(listOf("1"), Filters.students(students, "amin").map { it.uid })
        assertEquals(listOf("2"), Filters.students(students, "uon").map { it.uid })
        assertEquals(listOf("2"), Filters.students(students, "kili").map { it.uid })
    }

    @Test fun authErrorsMapToReadableMessages() {
        assertEquals("Incorrect email or password", AuthErrors.message("ERROR_INVALID_CREDENTIAL"))
        assertEquals("An account with this email already exists", AuthErrors.message("ERROR_EMAIL_ALREADY_IN_USE"))
        assertEquals(AuthErrors.message(null), AuthErrors.message("SOMETHING_NEW"))
    }
}

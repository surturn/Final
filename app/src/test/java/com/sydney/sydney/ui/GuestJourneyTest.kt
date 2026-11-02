package com.sydney.sydney.ui

import com.sydney.sydney.FakeAuth
import com.sydney.sydney.FakeHostels
import com.sydney.sydney.FakeProfiles
import com.sydney.sydney.FakeRecent
import com.sydney.sydney.FakeSaved
import com.sydney.sydney.MainDispatcherRule
import com.sydney.sydney.hostel
import com.sydney.sydney.profile
import com.sydney.sydney.ui.home.HomeViewModel
import com.sydney.sydney.ui.hostels.HostelDetailViewModel
import com.sydney.sydney.ui.hostels.HostelListViewModel
import com.sydney.sydney.ui.hostels.SavedViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GuestJourneyTest {
    @get:Rule val main = MainDispatcherRule()

    private val two = FakeHostels(listOf(hostel("1", "Sunrise"), hostel("2", "Moonlight")))

    @Test fun aGuestCanBrowseAndSearchWithoutAnAccount() = runTest {
        val vm = HostelListViewModel(two, FakeAuth(), FakeSaved())
        val state = vm.state.first { !it.loading }
        assertFalse(state.signedIn)
        assertEquals(2, state.hostels.size)
    }

    @Test fun aGuestTappingSaveSeesTheSignInPromptAndNothingIsWritten() = runTest {
        val saved = FakeSaved()
        val vm = HostelListViewModel(two, FakeAuth(), saved)
        vm.state.first { !it.loading }
        vm.onSaveClick("1")
        assertTrue(vm.state.first { it.showSignInPrompt }.showSignInPrompt)
        assertTrue(saved.idsFor("u1").isEmpty())
        vm.dismissPrompt()
        assertFalse(vm.state.first { !it.showSignInPrompt }.showSignInPrompt)
    }

    @Test fun aSignedInUserCanSaveAndUnsaveFromTheList() = runTest {
        val saved = FakeSaved()
        val vm = HostelListViewModel(two, FakeAuth("u1"), saved)
        vm.state.first { !it.loading }
        vm.onSaveClick("1")
        assertEquals(setOf("1"), vm.state.first { it.savedIds.isNotEmpty() }.savedIds)
        vm.onSaveClick("1")
        assertTrue(vm.state.first { it.savedIds.isEmpty() }.savedIds.isEmpty())
        assertFalse(vm.state.value.showSignInPrompt)
    }

    @Test fun aFailedSaveShowsAnErrorInsteadOfFailingSilently() = runTest {
        val saved = FakeSaved().apply { failWrite = "No connection" }
        val vm = HostelListViewModel(two, FakeAuth("u1"), saved)
        vm.state.first { !it.loading }
        vm.onSaveClick("1")
        assertTrue(vm.state.first { it.saveError != null }.savedIds.isEmpty())
    }

    @Test fun detailPromptsGuestsAndSavesForMembers() {
        val guest = HostelDetailViewModel(two, "1", FakeAuth(), FakeSaved(), FakeRecent())
        guest.onSaveClick()
        assertTrue(guest.state.value.showSignInPrompt)

        val saved = FakeSaved()
        val member = HostelDetailViewModel(two, "1", FakeAuth("u1"), saved, FakeRecent())
        member.onSaveClick()
        assertTrue(member.state.value.isSaved)
        assertEquals(setOf("1"), saved.idsFor("u1"))
    }

    @Test fun openingAHostelIsRememberedOnlyForSignedInUsers() {
        val recent = FakeRecent()
        HostelDetailViewModel(two, "1", FakeAuth(), FakeSaved(), recent)
        assertTrue(recent.ids("u1").isEmpty())
        HostelDetailViewModel(two, "2", FakeAuth("u1"), FakeSaved(), recent)
        HostelDetailViewModel(two, "1", FakeAuth("u1"), FakeSaved(), recent)
        assertEquals(listOf("1", "2"), recent.ids("u1"))
    }

    @Test fun savedScreenListsOnlyHostelsStillListed() = runTest {
        val saved = FakeSaved().apply {
            setSaved("u1", "2", true)
            setSaved("u1", "removed", true)
        }
        val vm = SavedViewModel(two, FakeAuth("u1"), saved)
        assertEquals(listOf("2"), vm.state.first { !it.loading }.hostels.map { it.id })
        vm.unsave("2")
        assertTrue(vm.state.first { it.hostels.isEmpty() }.hostels.isEmpty())
    }

    @Test fun homeShowsRecentHostelsAndAProfileNudge() {
        val recent = FakeRecent().apply { record("u1", "2"); record("u1", "gone") }
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1") }
        val vm = HomeViewModel(FakeAuth("u1"), profiles, two, recent)
        assertEquals(listOf("2"), vm.state.value.recent.map { it.id })

        val guest = HomeViewModel(FakeAuth(), FakeProfiles(), two, recent)
        assertTrue(guest.state.value.recent.isEmpty())
        assertFalse(guest.state.value.profileIncomplete)
    }
}

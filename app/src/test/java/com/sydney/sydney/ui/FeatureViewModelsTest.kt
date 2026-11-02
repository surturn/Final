package com.sydney.sydney.ui

import com.sydney.sydney.FakeAuth
import com.sydney.sydney.FakeHostels
import com.sydney.sydney.FakeProfiles
import com.sydney.sydney.FakeStudents
import com.sydney.sydney.MainDispatcherRule
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.data.repo.AppException
import com.sydney.sydney.hostel
import com.sydney.sydney.profile
import com.sydney.sydney.student
import com.sydney.sydney.ui.home.HomeViewModel
import com.sydney.sydney.ui.hostels.AddHostelViewModel
import com.sydney.sydney.ui.hostels.HostelDetailViewModel
import com.sydney.sydney.ui.hostels.HostelListViewModel
import com.sydney.sydney.ui.profile.ProfileViewModel
import com.sydney.sydney.ui.students.StudentsViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FeatureViewModelsTest {
    @get:Rule val main = MainDispatcherRule()

    // ---- home ----
    @Test fun homeLoadsTheProfileOfTheSignedInUser() {
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1") }
        val vm = HomeViewModel(FakeAuth("u1"), profiles)
        assertEquals("Amina Otieno", vm.state.value.profile?.displayName)
        assertEquals(false, vm.state.value.loading)
    }

    @Test fun homeClearsTheProfileOnSignOut() {
        val auth = FakeAuth("u1")
        val vm = HomeViewModel(auth, FakeProfiles().apply { store["u1"] = profile("u1") })
        vm.signOut()
        assertNull(vm.state.value.uid)
        assertNull(vm.state.value.profile)
    }

    @Test fun homeDoesNotShowTheOldProfileToANewAccount() {
        val auth = FakeAuth("u1")
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1") }
        val vm = HomeViewModel(auth, profiles)
        auth.setUser("u2")
        assertNull(vm.state.value.profile)
        assertEquals("u2", vm.state.value.uid)
    }

    @Test fun homeApplyForLandlordOnlyWorksFromNone() {
        val profiles = FakeProfiles().apply {
            store["u1"] = profile("u1")
            store["u2"] = profile("u2", LandlordStatus.APPROVED)
        }
        val first = HomeViewModel(FakeAuth("u1"), profiles)
        first.applyForLandlord()
        assertEquals(LandlordStatus.PENDING, first.state.value.profile?.landlordStatus)
        assertEquals(1, profiles.applied)

        val approved = HomeViewModel(FakeAuth("u2"), profiles)
        approved.applyForLandlord()
        assertEquals(1, profiles.applied)
    }

    @Test fun homeReportsALoadFailure() {
        val profiles = FakeProfiles().apply { failGet = "No connection" }
        val vm = HomeViewModel(FakeAuth("u1"), profiles)
        assertEquals("No connection", vm.state.value.error)
    }

    // ---- hostels ----
    @Test fun hostelListFiltersBySearch() = runTest {
        val vm = HostelListViewModel(FakeHostels(listOf(hostel("1", "Sunrise"), hostel("2", "Moonlight"))))
        assertEquals(2, vm.state.first { !it.loading }.hostels.size)
        vm.onQuery("moon")
        val filtered = vm.state.first { it.query == "moon" }
        assertEquals(listOf("2"), filtered.hostels.map { it.id })
        assertEquals(2, filtered.total)
    }

    @Test fun hostelListShowsErrorsAndRecoversOnRetry() = runTest {
        val repo = FakeHostels(listOf(hostel("1"))).apply { failWith = "No connection" }
        val vm = HostelListViewModel(repo)
        assertEquals("No connection", vm.state.first { it.error != null }.error)
        repo.failWith = null
        vm.retry()
        assertEquals(1, vm.state.first { it.error == null && !it.loading }.hostels.size)
    }

    @Test fun hostelDetailReportsAMissingListing() {
        val vm = HostelDetailViewModel(FakeHostels(listOf(hostel("1"))), "gone")
        assertNull(vm.state.value.hostel)
        assertEquals("This hostel is no longer listed", vm.state.value.error)
        assertEquals("1", HostelDetailViewModel(FakeHostels(listOf(hostel("1"))), "1").state.value.hostel?.id)
    }

    private fun addVm(status: LandlordStatus, hostels: FakeHostels = FakeHostels()): AddHostelViewModel {
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1", status, phone = "0712345678") }
        return AddHostelViewModel(FakeAuth("u1"), profiles, hostels)
    }

    @Test fun unapprovedUsersCannotPublish() {
        val hostels = FakeHostels()
        val vm = addVm(LandlordStatus.PENDING, hostels)
        vm.submit()
        assertNull(hostels.added)
    }

    @Test fun addHostelValidatesBeforeUploading() {
        val hostels = FakeHostels()
        val vm = addVm(LandlordStatus.APPROVED, hostels)
        vm.onPrice("0")
        vm.submit()
        val s = vm.state.value
        assertNotNull(s.nameError)
        assertNotNull(s.descriptionError)
        assertNotNull(s.priceError)
        assertNotNull(s.imageError)
        assertNull(hostels.added)
    }

    @Test fun addHostelPublishesWithAParsedPriceAndPrefilledPhone() {
        val hostels = FakeHostels()
        val vm = addVm(LandlordStatus.APPROVED, hostels)
        assertEquals("0712345678", vm.state.value.phone)
        vm.onName("Sunrise Hostel")
        vm.onDescription("Single rooms near the gate with water and wifi")
        vm.onPrice("6,500")
        vm.onImage("content://photo/1")
        vm.submit()
        assertTrue(vm.state.value.done)
        assertEquals(6500L, hostels.added?.priceKes)
        assertEquals("u1", hostels.addedOwner)
    }

    // ---- students ----
    @Test fun studentsAreFilteredLocally() = runTest {
        val vm = StudentsViewModel(FakeStudents(listOf(student("1", "Amina"), student("2", "Brian"))))
        assertEquals(2, vm.state.first { !it.loading }.students.size)
        vm.onQuery("bri")
        assertEquals(listOf("2"), vm.state.first { it.query == "bri" }.students.map { it.uid })
    }

    @Test fun studentsSurfaceErrors() = runTest {
        val vm = StudentsViewModel(FakeStudents().apply { failWith = "Permission" })
        assertEquals("Permission", vm.state.first { it.error != null }.error)
    }

    // ---- profile ----
    @Test fun profileLoadsAnExistingAccount() {
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1", phone = "0712345678"); photo = "https://img/p" }
        val vm = ProfileViewModel(FakeAuth("u1"), profiles)
        val s = vm.state.value
        assertTrue(s.exists)
        assertEquals("Amina Otieno", s.name)
        assertEquals("https://img/p", s.photoUrl)
    }

    @Test fun profileWithoutARecordPrefillsFromTheAccountAndCreatesOnSave() {
        val profiles = FakeProfiles()
        val vm = ProfileViewModel(FakeAuth("u1", "amina@example.com"), profiles) { 7L }
        assertEquals(false, vm.state.value.exists)
        assertEquals("amina", vm.state.value.name)
        vm.save()
        assertEquals(7L, profiles.created?.createdAt)
        assertTrue(vm.state.value.exists)
    }

    @Test fun profileRejectsABadPhoneBeforeSaving() {
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1") }
        val vm = ProfileViewModel(FakeAuth("u1"), profiles)
        vm.onPhone("12345")
        vm.save()
        assertNotNull(vm.state.value.phoneError)
        assertNull(profiles.updated)
    }

    @Test fun profileSaveSendsTheNewPhotoAndNormalizedPhone() {
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1") }
        val vm = ProfileViewModel(FakeAuth("u1"), profiles)
        vm.onPhone("0712 345 678")
        vm.onPhoto("content://photo/2")
        vm.save()
        assertEquals("0712345678", profiles.updated?.phone)
        assertEquals("content://photo/2", profiles.updatedPhoto)
        assertEquals("Profile saved", vm.state.value.message)
    }

    @Test fun deleteAccountNeedsThePasswordFirst() {
        val auth = FakeAuth("u1")
        val vm = ProfileViewModel(auth, FakeProfiles().apply { store["u1"] = profile("u1") })
        vm.deleteAccount("")
        assertEquals("Enter your password to confirm", vm.state.value.error)
        assertEquals(false, auth.accountDeleted)
    }

    @Test fun deleteAccountStopsAtAWrongPasswordAndKeepsTheData() {
        val auth = FakeAuth("u1").apply { reauthResult = Result.failure(AppException("Incorrect email or password")) }
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1") }
        val vm = ProfileViewModel(auth, profiles)
        vm.deleteAccount("wrong")
        assertEquals("Incorrect email or password", vm.state.value.error)
        assertNull(profiles.deletedUid)
        assertEquals(false, vm.state.value.deleted)
    }

    @Test fun deleteAccountKeepsTheSignInWhenDataRemovalFails() {
        val auth = FakeAuth("u1")
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1"); failDelete = "No connection" }
        val vm = ProfileViewModel(auth, profiles)
        vm.deleteAccount("secret123")
        assertEquals("No connection", vm.state.value.error)
        assertEquals(false, auth.accountDeleted)
    }

    @Test fun deleteAccountRemovesDataThenTheSignIn() {
        val auth = FakeAuth("u1")
        val profiles = FakeProfiles().apply { store["u1"] = profile("u1") }
        val vm = ProfileViewModel(auth, profiles)
        vm.deleteAccount("secret123")
        assertEquals("u1", profiles.deletedUid)
        assertTrue(auth.accountDeleted)
        assertTrue(vm.state.value.deleted)
    }
}

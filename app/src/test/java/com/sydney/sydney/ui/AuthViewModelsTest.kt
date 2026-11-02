package com.sydney.sydney.ui

import com.sydney.sydney.FakeAuth
import com.sydney.sydney.FakeProfiles
import com.sydney.sydney.MainDispatcherRule
import com.sydney.sydney.data.model.LandlordStatus
import com.sydney.sydney.data.repo.AppException
import com.sydney.sydney.ui.auth.LoginViewModel
import com.sydney.sydney.ui.auth.RegisterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AuthViewModelsTest {
    @get:Rule val main = MainDispatcherRule()

    @Test fun loginWithBadInputNeverCallsFirebase() {
        val auth = FakeAuth()
        val vm = LoginViewModel(auth)
        vm.onEmail("nope")
        vm.submit()
        assertNotNull(vm.state.value.emailError)
        assertNotNull(vm.state.value.passwordError)
        assertEquals(0, auth.signInCalls)
    }

    @Test fun loginSuccessMarksDone() {
        val auth = FakeAuth()
        val vm = LoginViewModel(auth)
        vm.onEmail("amina@example.com")
        vm.onPassword("secret123")
        vm.submit()
        assertTrue(vm.state.value.done)
        assertFalse(vm.state.value.loading)
    }

    @Test fun loginFailureShowsTheMessageAndAllowsRetry() {
        val auth = FakeAuth().apply { signInResult = Result.failure(AppException("Incorrect email or password")) }
        val vm = LoginViewModel(auth)
        vm.onEmail("amina@example.com")
        vm.onPassword("wrong")
        vm.submit()
        assertEquals("Incorrect email or password", vm.state.value.error)
        assertFalse(vm.state.value.done)
        assertFalse(vm.state.value.loading)
        vm.onPassword("right")
        assertNull(vm.state.value.error)
    }

    @Test fun registerValidatesEveryFieldBeforeCreatingAnAccount() {
        val auth = FakeAuth()
        val vm = RegisterViewModel(auth, FakeProfiles())
        vm.submit()
        val s = vm.state.value
        assertNotNull(s.nameError)
        assertNotNull(s.emailError)
        assertNotNull(s.passwordError)
        assertNotNull(s.confirmationError)
        assertNull(auth.currentUserId)
    }

    private fun filled(vm: RegisterViewModel, landlord: Boolean) {
        vm.onName("Amina Otieno")
        vm.onEmail("amina@example.com")
        vm.onPassword("hostel2024")
        vm.onConfirmation("hostel2024")
        vm.onLandlord(landlord)
    }

    @Test fun registerCreatesAStudentProfile() {
        val profiles = FakeProfiles()
        val vm = RegisterViewModel(FakeAuth(currentEmail = "amina@example.com"), profiles) { 42L }
        filled(vm, landlord = false)
        vm.submit()
        assertTrue(vm.state.value.done)
        val created = requireNotNull(profiles.created)
        assertEquals("Amina Otieno", created.displayName)
        assertEquals(LandlordStatus.NONE, created.landlordStatus)
        assertEquals(42L, created.createdAt)
    }

    @Test fun registerAsLandlordStartsAsPendingNeverApproved() {
        val profiles = FakeProfiles()
        val vm = RegisterViewModel(FakeAuth(), profiles)
        filled(vm, landlord = true)
        vm.submit()
        assertEquals(LandlordStatus.PENDING, profiles.created?.landlordStatus)
    }

    @Test fun registerStillFinishesWhenOnlyTheProfileWriteFails() {
        val profiles = FakeProfiles().apply { failCreate = "offline" }
        val vm = RegisterViewModel(FakeAuth(), profiles)
        filled(vm, landlord = false)
        vm.submit()
        assertTrue(vm.state.value.done)
    }

    @Test fun registerShowsAccountErrors() {
        val auth = FakeAuth().apply { signUpResult = Result.failure(AppException("An account with this email already exists")) }
        val vm = RegisterViewModel(auth, FakeProfiles())
        filled(vm, landlord = false)
        vm.submit()
        assertEquals("An account with this email already exists", vm.state.value.error)
        assertFalse(vm.state.value.done)
    }
}

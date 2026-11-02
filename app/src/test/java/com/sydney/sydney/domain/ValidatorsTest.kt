package com.sydney.sydney.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {
    @Test fun emailAcceptsNormalAddressesAndIgnoresOuterSpaces() {
        assertNull(Validators.email("amina@example.com"))
        assertNull(Validators.email("  amina.o+test@mail.co.ke "))
    }

    @Test fun emailRejectsBlankAndMalformed() {
        assertNotNull(Validators.email(""))
        assertNotNull(Validators.email("amina"))
        assertNotNull(Validators.email("amina@"))
        assertNotNull(Validators.email("amina@example"))
    }

    @Test fun passwordNeedsLengthLetterAndDigit() {
        assertNull(Validators.password("hostel2024"))
        assertNotNull(Validators.password("short1"))
        assertNotNull(Validators.password("onlyletters"))
        assertNotNull(Validators.password("12345678"))
        assertNotNull(Validators.password(""))
    }

    @Test fun confirmationMustMatch() {
        assertNull(Validators.passwordConfirmation("abc12345", "abc12345"))
        assertNotNull(Validators.passwordConfirmation("abc12345", "abc12346"))
        assertNotNull(Validators.passwordConfirmation("abc12345", ""))
    }

    @Test fun phoneAcceptsKenyanMobileFormats() {
        listOf("0712345678", "0112345678", "+254712345678", "254712345678", "0712 345 678", "0712-345-678")
            .forEach { assertNull(it, Validators.phone(it)) }
    }

    @Test fun phoneIsOptionalButMustBeValidWhenGiven() {
        assertNull(Validators.phone(""))
        assertNotNull(Validators.phone("12345"))
        assertNotNull(Validators.phone("0212345678"))
        assertNotNull(Validators.phone("07123456789"))
    }

    @Test fun requiredPhoneRejectsBlank() {
        assertNotNull(Validators.requiredPhone(""))
        assertNull(Validators.requiredPhone("0712345678"))
    }

    @Test fun normalizePhoneStripsSpacesAndDashes() {
        assertEquals("0712345678", Validators.normalizePhone(" 0712 345-678 "))
    }

    @Test fun parsePriceHandlesSeparatorsAndBounds() {
        assertEquals(6500L, Validators.parsePrice("6500"))
        assertEquals(6500L, Validators.parsePrice("6,500"))
        assertEquals(1_000_000L, Validators.parsePrice("1000000"))
        assertNull(Validators.parsePrice("0"))
        assertNull(Validators.parsePrice("1000001"))
        assertNull(Validators.parsePrice("-5"))
        assertNull(Validators.parsePrice("65.50"))
        assertNull(Validators.parsePrice("abc"))
        assertNull(Validators.parsePrice(""))
        assertNull(Validators.parsePrice("99999999999999999999"))
    }

    @Test fun hostelFieldsFollowTheSecurityRuleLimits() {
        assertNotNull(Validators.hostelName("A"))
        assertNull(Validators.hostelName("Sunrise"))
        assertNotNull(Validators.hostelName("x".repeat(101)))
        assertNotNull(Validators.hostelDescription("too short"))
        assertNull(Validators.hostelDescription("Ten chars!"))
        assertNotNull(Validators.hostelDescription("x".repeat(2001)))
    }

    @Test fun profileFieldsFollowTheSecurityRuleLimits() {
        assertNotNull(Validators.displayName("A"))
        assertNull(Validators.displayName("Amina"))
        assertNotNull(Validators.displayName("x".repeat(81)))
        assertNotNull(Validators.campus("x".repeat(81)))
        assertNotNull(Validators.location("x".repeat(121)))
    }

    @Test fun formatPriceIsLocaleIndependent() {
        assertEquals("KES 6,500 / month", Validators.formatPrice(6500))
        assertEquals("KES 1,000,000 / month", Validators.formatPrice(1_000_000))
    }
}

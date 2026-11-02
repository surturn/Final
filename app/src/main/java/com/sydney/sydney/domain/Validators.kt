package com.sydney.sydney.domain

import java.util.Locale

/**
 * Input checks shared by the screens. Every function returns an error message to show next to
 * the field, or null when the value is acceptable. Pure Kotlin so it is covered by unit tests.
 */
object Validators {
    private val emailPattern = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    // Kenyan mobile numbers: 07xx xxx xxx, 01xx xxx xxx, +2547.., +2541.. or 2547..
    private val phonePattern = Regex("^(\\+?254|0)[17][0-9]{8}$")

    const val MIN_PASSWORD_LENGTH = 8
    const val MAX_PRICE_KES = 1_000_000L

    fun email(value: String): String? = when {
        value.isBlank() -> "Email is required"
        !emailPattern.matches(value.trim()) -> "Enter a valid email address"
        else -> null
    }

    fun password(value: String): String? = when {
        value.isBlank() -> "Password is required"
        value.length < MIN_PASSWORD_LENGTH -> "Use at least $MIN_PASSWORD_LENGTH characters"
        value.none { it.isLetter() } || value.none { it.isDigit() } ->
            "Use letters and at least one number"
        else -> null
    }

    fun passwordConfirmation(password: String, confirmation: String): String? = when {
        confirmation.isBlank() -> "Confirm your password"
        password != confirmation -> "Passwords do not match"
        else -> null
    }

    fun displayName(value: String): String? = when {
        value.isBlank() -> "Name is required"
        value.trim().length < 2 -> "Name is too short"
        value.trim().length > 80 -> "Name is too long"
        else -> null
    }

    /** Phone is optional, but when given it must be a Kenyan mobile number. */
    fun phone(value: String): String? =
        if (value.isBlank() || phonePattern.matches(normalizePhone(value))) null
        else "Use a Kenyan number like 0712345678"

    /** Same check as [phone] but the number is required (a landlord's contact line). */
    fun requiredPhone(value: String): String? =
        if (value.isBlank()) "A contact phone number is required" else phone(value)

    /** Strips spaces and dashes so the stored value matches what the security rules measure. */
    fun normalizePhone(value: String): String =
        value.trim().replace(" ", "").replace("-", "")

    fun campus(value: String): String? =
        if (value.trim().length > 80) "Campus name is too long" else null

    fun location(value: String): String? =
        if (value.trim().length > 120) "Location is too long" else null

    fun hostelName(value: String): String? = when {
        value.isBlank() -> "Hostel name is required"
        value.trim().length < 2 -> "Hostel name is too short"
        value.trim().length > 100 -> "Hostel name is too long"
        else -> null
    }

    fun hostelDescription(value: String): String? = when {
        value.isBlank() -> "Description is required"
        value.trim().length < 10 -> "Describe the hostel in at least 10 characters"
        value.trim().length > 2000 -> "Description is too long"
        else -> null
    }

    /** Parses a monthly price in shillings such as "6500" or "6,500". Returns null if invalid. */
    fun parsePrice(value: String): Long? {
        val cleaned = value.trim().replace(",", "").replace(" ", "")
        if (cleaned.isEmpty() || !cleaned.all { it.isDigit() } || cleaned.length > 9) return null
        val price = cleaned.toLong()
        return if (price in 1..MAX_PRICE_KES) price else null
    }

    fun price(value: String): String? =
        if (parsePrice(value) != null) null else "Enter a monthly price between 1 and 1,000,000"

    fun formatPrice(priceKes: Long): String =
        String.format(Locale.US, "KES %,d / month", priceKes)
}

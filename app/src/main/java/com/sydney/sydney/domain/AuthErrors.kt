package com.sydney.sydney.domain

/** Turns Firebase Auth error codes into messages people can act on. */
object AuthErrors {
    fun message(code: String?): String = when (code) {
        "ERROR_INVALID_CREDENTIAL",
        "ERROR_WRONG_PASSWORD",
        "ERROR_USER_NOT_FOUND",
        "ERROR_INVALID_LOGIN_CREDENTIALS" -> "Incorrect email or password"
        "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists"
        "ERROR_WEAK_PASSWORD" -> "Choose a stronger password"
        "ERROR_INVALID_EMAIL" -> "Enter a valid email address"
        "ERROR_USER_DISABLED" -> "This account has been disabled"
        "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Try again in a few minutes"
        "ERROR_NETWORK_REQUEST_FAILED" -> "No connection. Check your internet and try again"
        else -> "Something went wrong. Please try again"
    }
}

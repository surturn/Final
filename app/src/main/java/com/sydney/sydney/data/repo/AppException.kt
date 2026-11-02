package com.sydney.sydney.data.repo

/** An error whose message is safe to show to the user as is. */
class AppException(message: String, cause: Throwable? = null) : Exception(message, cause)

object Messages {
    const val NETWORK = "No connection. Check your internet and try again"
    const val GENERIC = "Something went wrong. Please try again"
    const val DENIED = "You do not have permission to do that"
    const val SIGN_IN_AGAIN = "Please sign in again"
}

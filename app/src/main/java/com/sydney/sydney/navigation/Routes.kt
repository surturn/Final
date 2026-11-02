package com.sydney.sydney.navigation

const val ROUTE_SPLASH = "splash"
const val ROUTE_HOME = "home"
const val ROUTE_LOGIN = "login"
const val ROUTE_REGISTER = "register"
const val ROUTE_HOSTELS = "hostels"
const val ROUTE_HOSTEL = "hostel"
const val ROUTE_ADD_HOSTEL = "add_hostel"
const val ROUTE_STUDENTS = "students"
const val ROUTE_PROFILE = "profile"
const val ROUTE_SAVED = "saved"

const val ARG_HOSTEL_ID = "hostelId"

/** Screens that need a signed-in user. Leaving the session on one of these returns to login. */
val SIGNED_IN_ROUTES = setOf(ROUTE_STUDENTS, ROUTE_PROFILE, ROUTE_ADD_HOSTEL, ROUTE_SAVED)

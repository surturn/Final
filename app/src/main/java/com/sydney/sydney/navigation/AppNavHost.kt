package com.sydney.sydney.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sydney.sydney.container
import com.sydney.sydney.ui.SplashScreen
import com.sydney.sydney.ui.auth.LoginScreen
import com.sydney.sydney.ui.auth.RegisterScreen
import com.sydney.sydney.ui.home.HomeActions
import com.sydney.sydney.ui.home.HomeScreen
import com.sydney.sydney.ui.hostels.AddHostelScreen
import com.sydney.sydney.ui.hostels.HostelDetailScreen
import com.sydney.sydney.ui.hostels.HostelListScreen
import com.sydney.sydney.ui.profile.ProfileScreen
import com.sydney.sydney.ui.students.StudentsScreen

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val auth = LocalContext.current.container.auth
    val uid by auth.authState.collectAsStateWithLifecycle(initialValue = auth.currentUserId)
    val route = navController.currentBackStackEntryAsState().value?.destination?.route

    // Signing out, or an expired session, must never leave a private screen showing.
    LaunchedEffect(uid, route) {
        if (uid == null && route in SIGNED_IN_ROUTES) {
            navController.navigate(ROUTE_LOGIN) { popUpTo(ROUTE_HOME) }
        }
    }

    val backToHome: () -> Unit = { navController.popBackStack(ROUTE_HOME, inclusive = false) }

    NavHost(navController = navController, startDestination = ROUTE_SPLASH) {
        composable(ROUTE_SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(ROUTE_HOME) { popUpTo(ROUTE_SPLASH) { inclusive = true } }
            })
        }
        composable(ROUTE_HOME) {
            HomeScreen(
                HomeActions(
                    onLogin = { navController.navigate(ROUTE_LOGIN) },
                    onRegister = { navController.navigate(ROUTE_REGISTER) },
                    onHostels = { navController.navigate(ROUTE_HOSTELS) },
                    onStudents = { navController.navigate(ROUTE_STUDENTS) },
                    onProfile = { navController.navigate(ROUTE_PROFILE) },
                    onAddHostel = { navController.navigate(ROUTE_ADD_HOSTEL) },
                ),
            )
        }
        composable(ROUTE_LOGIN) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onRegister = { navController.navigate(ROUTE_REGISTER) { popUpTo(ROUTE_LOGIN) { inclusive = true } } },
                onSuccess = backToHome,
            )
        }
        composable(ROUTE_REGISTER) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onLogin = { navController.navigate(ROUTE_LOGIN) { popUpTo(ROUTE_REGISTER) { inclusive = true } } },
                onSuccess = backToHome,
            )
        }
        composable(ROUTE_HOSTELS) {
            HostelListScreen(
                onBack = { navController.popBackStack() },
                onOpen = { id -> navController.navigate("$ROUTE_HOSTEL/$id") },
            )
        }
        composable("$ROUTE_HOSTEL/{$ARG_HOSTEL_ID}") { entry ->
            HostelDetailScreen(
                hostelId = entry.arguments?.getString(ARG_HOSTEL_ID).orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }
        composable(ROUTE_ADD_HOSTEL) {
            AddHostelScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.navigate(ROUTE_HOSTELS) { popUpTo(ROUTE_ADD_HOSTEL) { inclusive = true } } },
            )
        }
        composable(ROUTE_STUDENTS) { StudentsScreen(onBack = { navController.popBackStack() }) }
        composable(ROUTE_PROFILE) {
            ProfileScreen(onBack = { navController.popBackStack() }, onDeleted = backToHome)
        }
    }
}

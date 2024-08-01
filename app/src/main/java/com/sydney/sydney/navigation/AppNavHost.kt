package com.sydney.sydney.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sydney.sydney.ui.theme.SplashScreen
import com.sydney.sydney.ui.theme.about.AboutScreen
import com.sydney.sydney.ui.theme.auth.LoginScreen
import com.sydney.sydney.ui.theme.auth.SignUpScreen
import com.sydney.sydney.ui.theme.dashboard.DashboardScreen
import com.sydney.sydney.ui.theme.dashboard.ProfileScreen
import com.sydney.sydney.ui.theme.home.HomeScreen
import com.sydney.sydney.ui.theme.live.AddHostelScreen
import com.sydney.sydney.ui.theme.live.HostelDetailScreen
import com.sydney.sydney.ui.theme.live.HostelListScreen
import com.sydney.sydney.ui.theme.students.AddStudents
import com.sydney.sydney.ui.theme.students.Search
import com.sydney.sydney.ui.theme.students.Students

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = ROUTE_SPLASH
){
    NavHost(
        navController = navController,
        startDestination =startDestination )
    {


        composable(ROUTE_HOME) {
            HomeScreen(navController)
        }


        composable(ROUTE_ABOUT) {
            AboutScreen(navController)
        }


        composable(ROUTE_ADD_STUDENTS) {
            AddStudents(navController)
        }

        composable(ROUTE_SPLASH) {
            SplashScreen(navController)
        }

        composable(ROUTE_VIEW_STUDENTS) {
            Students(navController = navController, viewModel = viewModel() )
        }

        composable(ROUTE_SEARCH) {
            Search(navController)
        }

        composable(ROUTE_DASHBOARD) {
            DashboardScreen(navController)
        }

        composable(ROUTE_REGISTER) {
            SignUpScreen(navController = navController) {

            }
        }

        composable(ROUTE_LOGIN) {
            LoginScreen(navController = navController){}
        }

        composable(ROUTE_ADD_HOSTELS) {
            AddHostelScreen(navController = navController){}
        }

        composable(ROUTE_VIEW_HOSTELS) {
            HostelListScreen(navController = navController, hostels = listOf() )
        }
        composable("$ROUTE_PROFILE/{studentKey}") { backStackEntry ->
            val studentKey = backStackEntry.arguments?.getString("studentKey")
            if (studentKey != null) {
                ProfileScreen(navController, studentKey)
            }
        }





        composable("hostelDetail/{hostelId}") { backStackEntry ->
            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
            HostelDetailScreen(navController, hostelId)
        }










    }

}
package com.mamogkat.mmcmcurriculumtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mamogkat.mmcmcurriculumtracker.ui.screens.*
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel

sealed class Screen(val route: String) {
    object  Login: Screen("login")
    object Register: Screen("register_ui")
    object  ForgotPassword: Screen("forgot_password_screen")
    object ChooseCurriculum: Screen("choose_curriculum")
    object CurriculumOverview: Screen("curriculum_overview")
    object NextCourses: Screen("next_courses")
    object Admin: Screen("admin_page")
    object AboutDevelopers: Screen("about_developers")
    object UserAccount: Screen("user_account")
    object UploadCourses: Screen("upload_courses")
}


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterUI(navController)}
        composable(Screen.ForgotPassword.route) { ForgotPassword(navController) }
        composable(Screen.ChooseCurriculum.route) { ChooseCurriculumScreen(navController) }
        composable(Screen.CurriculumOverview.route) { CurriculumOverviewScreen(navController) }
        composable(Screen.NextCourses.route) { NextCoursesScreen(navController) }
        composable(Screen.Admin.route) { AdminPage(navController)}
        composable(Screen.AboutDevelopers.route) { AboutDevelopersScreen(navController) }
        composable(Screen.UserAccount.route) { UserProfileScreen("student","studen@mcm.edu.ph",navController) }
        composable(Screen.UploadCourses.route) { UploadCoursesScreen(navController)  }
    }
}
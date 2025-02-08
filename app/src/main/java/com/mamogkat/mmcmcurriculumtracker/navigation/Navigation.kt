package com.mamogkat.mmcmcurriculumtracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminPage
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.UploadCoursesScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.ForgotPassword
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoginScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.RegisterUI
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.*
import com.mamogkat.mmcmcurriculumtracker.ui.studentscreens.StudentMainScreen

sealed class Screen(val route: String) {
    object  Login: Screen("login")
    object Register: Screen("register_ui")
    object  ForgotPassword: Screen("forgot_password_screen")
    object ChooseCurriculum: Screen("choose_curriculum")
    object Admin: Screen("admin_page")
    object UploadCourses: Screen("upload_courses")
    object Student: Screen("student_main")
}


@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterUI(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPassword(navController) }
        composable(Screen.ChooseCurriculum.route) { ChooseCurriculumScreen(navController) }
        composable(Screen.Admin.route) { AdminPage(navController) }
        composable(Screen.UploadCourses.route) { UploadCoursesScreen(navController)  }
        composable(Screen.Student.route) { StudentMainScreen(navController)  }
    }
}
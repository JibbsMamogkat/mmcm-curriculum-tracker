package com.mamogkat.mmcmcurriculumtracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminHomePage
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.StudentMasterListScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.UploadCoursesScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.ForgotPassword
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoginScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.RegisterUI
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.*
import com.mamogkat.mmcmcurriculumtracker.ui.studentscreens.StudentMainScreen
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel

sealed class Screen(val route: String) {
    object  Login: Screen("login")
    object Register: Screen("register_ui")
    object  ForgotPassword: Screen("forgot_password_screen")
    object ChooseCurriculum: Screen("choose_curriculum")
    object AdminHomePage: Screen("admin_home_page")
    object UploadCourses: Screen("upload_courses")
    object Student: Screen("student_main")
    object StudentMasterList: Screen("student_master_list")
}


@Composable
fun AppNavHost(navController: NavHostController, adminViewModel: AdminViewModel) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterUI(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPassword(navController) }
        composable(Screen.ChooseCurriculum.route) { ChooseCurriculumScreen(navController) }
        composable(Screen.AdminHomePage.route) { AdminHomePage(navController, adminViewModel) }
        composable(Screen.UploadCourses.route) { UploadCoursesScreen(navController)  }
        composable(Screen.Student.route) { StudentMainScreen(navController)  }
        composable(Screen.StudentMasterList.route) { StudentMasterListScreen(adminViewModel, navController)  }
    }
}
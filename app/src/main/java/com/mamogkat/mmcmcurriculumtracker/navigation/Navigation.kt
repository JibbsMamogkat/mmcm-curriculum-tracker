package com.mamogkat.mmcmcurriculumtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminHomePage
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.StudentMasterListScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.ManageCurriculumsPage
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.ForgotPassword
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoginScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.RegisterUI
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.VerifyOtpScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.*
import com.mamogkat.mmcmcurriculumtracker.ui.studentscreens.StudentMainScreen
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object  Login: Screen("login")
    object Register: Screen("register_ui")
    object  ForgotPassword: Screen("forgot_password_screen")
    object ChooseCurriculum: Screen("choose_curriculum")
    object AdminHomePage: Screen("admin_home_page")
    object Student: Screen("student_main")
    object StudentMasterList: Screen("student_master_list")
    object ManageCurriculumsPage: Screen("manage_curriculum_page")
}


@Composable
fun AppNavHost(navController: NavHostController, adminViewModel: AdminViewModel) {
    // duff added - feb 15
    val authViewModel: AuthViewModel = viewModel()
    LaunchedEffect(Unit) {
        authViewModel.checkUserCurriculum { hasCurriculum ->
            if (hasCurriculum) {
                navController.navigate("student_main") {
                    popUpTo("choose_curriculum") { inclusive = true } // Prevent going back
                }
            }
        }
    }
    // --------------------------------------------------------------
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterUI(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPassword(navController) }
        composable(Screen.ChooseCurriculum.route) { ChooseCurriculumScreen(navController) }
        composable(Screen.AdminHomePage.route) { AdminHomePage(navController, adminViewModel) }
        composable(Screen.Student.route) { StudentMainScreen(navController)  }
        composable(Screen.StudentMasterList.route) { StudentMasterListScreen(adminViewModel, navController)}
        composable(Screen.ManageCurriculumsPage.route) { ManageCurriculumsPage(navController) }
    }
}



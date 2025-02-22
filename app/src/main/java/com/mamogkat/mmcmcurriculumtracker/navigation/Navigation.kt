package com.mamogkat.mmcmcurriculumtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminHomePage
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.StudentMasterListScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.ManageCurriculumsPage
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminCurriculumOverviewScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminNextAvailableCoursesScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.ChangePasswordScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.ErrorScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.ForgotPassword
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoadingScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoginScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.RegisterUI
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.SplashScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.VerifyForgotOtpScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.VerifyOtpScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.*
import com.mamogkat.mmcmcurriculumtracker.ui.studentscreens.StudentMainScreen
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel

sealed class Screen(val route: String) {
    object  Login: Screen("login")
    object Register: Screen("register_ui")
    object  ForgotPassword: Screen("forgot_password_screen")
    object ChooseCurriculum: Screen("choose_curriculum")
    object AdminHomePage: Screen("admin_home_page")
    object Student: Screen("student_main")
    object StudentMasterList: Screen("student_master_list")
    object ManageCurriculumsPage: Screen("manage_curriculum_page")
    object AdminCurriculumOverviewScreen: Screen("admin_curriculum_overview_screen")
    object AdminNextAvailableCoursesScreen: Screen("admin_next_available_courses_screen")
}


@Composable
fun AppNavHost(navController: NavHostController, adminViewModel: AdminViewModel, curriculumViewModel: CurriculumViewModel, authViewModel: AuthViewModel) {
    // duff added - feb 15
    // --------------------------------------------------------------
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen() }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterUI(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPassword(navController, authViewModel) }
        composable(Screen.ChooseCurriculum.route) { ChooseCurriculumScreen(navController) }
        composable(Screen.AdminHomePage.route) { AdminHomePage(navController, adminViewModel) }
        composable(Screen.Student.route) { StudentMainScreen(navController)  }
        composable(Screen.StudentMasterList.route) { StudentMasterListScreen(adminViewModel, navController)}
        composable(Screen.ManageCurriculumsPage.route) { ManageCurriculumsPage(navController) }
        composable(
            route = "admin_curriculum_overview/{studentId}",  // ✅ Define studentId as a route argument
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: return@composable
            AdminCurriculumOverviewScreen(navController, studentId, curriculumViewModel)  // ✅ Pass studentId
        }
        composable(
            route = "admin_next_available_courses_screen/{studentId}",  // ✅ Define studentId as a route argument
            arguments = listOf(navArgument("studentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: return@composable
            AdminNextAvailableCoursesScreen(
                studentId,
                navController,
                curriculumViewModel
            )// ✅ Pass studentId
        }
        composable("verify_otp/{email}/{password}/{role}/{program}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val password = backStackEntry.arguments?.getString("password") ?: ""
            val role = backStackEntry.arguments?.getString("role") ?: ""
            val program = backStackEntry.arguments?.getString("program") ?: ""
            VerifyOtpScreen(email, password, role, program, navController, authViewModel)
        }
        // duff for OTP loading:
        composable("loadingOTP") { LoadingScreen() }
        composable("error") { ErrorScreen(message = "Failed to send OTP. Please try again. If the issue persists, contact Jameel or Duff. Note: OTP requests may take longer during periods of high demand.") {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true } // Removes 'error' from back stack
                launchSingleTop = true
            }
        }}
        // ------------------------------------------------
        // for Forgot Password:
        composable("verify_forgot_password_otp/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyForgotOtpScreen(email = email, navController = navController, authViewModel = authViewModel)
        }

        // Change Password Screen
        composable("change_password/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ChangePasswordScreen(viewModel = authViewModel, navController = navController, email = email)
        }
        //---------------------------------
    }
}



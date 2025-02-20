package com.mamogkat.mmcmcurriculumtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.ForgotPassword
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoginScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.RegisterUI
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
fun AppNavHost(navController: NavHostController, adminViewModel: AdminViewModel, curriculumViewModel: CurriculumViewModel) {
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
    }
}



package com.mamogkat.mmcmcurriculumtracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mamogkat.mmcmcurriculumtracker.ui.screens.ChooseCurriculumScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.CurriculumOverviewScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.LoginScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.NextCoursesScreen

sealed class Screen(val route: String) {
    object  Login: Screen("login")
    object ChooseCurriculum: Screen("choose_curriculum")
    object CurriculumOverview: Screen("curriculum_overview")
    object NextCourses: Screen("next_courses")
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.ChooseCurriculum.route) { ChooseCurriculumScreen(navController) }
        composable(Screen.CurriculumOverview.route) { CurriculumOverviewScreen(navController) }
        composable(Screen.NextCourses.route) { NextCoursesScreen(navController) }
    }
}
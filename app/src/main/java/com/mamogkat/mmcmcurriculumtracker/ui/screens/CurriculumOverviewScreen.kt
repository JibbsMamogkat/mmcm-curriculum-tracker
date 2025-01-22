package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun CurriculumOverviewScreen(navController: NavController) {
    Text(text = "Curriculum Overview Screen")
    Button(onClick = {
        navController.navigate("next_courses")
    }) {
        Text(text = "Go to Next Courses")
    }
}
package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.navigation.Screen
import com.mamogkat.mmcmcurriculumtracker.ui.year.curriculumData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDevelopersScreen(navController: NavController) {
    ReusableScaffold(
        navController = navController,
        topBarTitle = "About the Developers"
    ){
        paddingValues ->

    }
}



@Preview
@Composable
fun AboutDevelopersScreenPreview() {
    AboutDevelopersScreen(rememberNavController())
}

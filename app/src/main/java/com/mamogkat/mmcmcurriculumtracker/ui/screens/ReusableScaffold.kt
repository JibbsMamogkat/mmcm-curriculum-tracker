package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReusableScaffold(
    navController: NavController,
    topBarTitle: String,
    content: @Composable (PaddingValues) -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.mmcm_blue),
                    titleContentColor = colorResource(id = R.color.mmcm_white)
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.mmcm_white)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(
                backgroundColor = colorResource(id = R.color.mmcm_blue),
                contentColor = colorResource(id = R.color.mmcm_white)
            ) {
                BottomNavigationItem(
                    icon = {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Home",
                            tint = colorResource(id = R.color.mmcm_white)
                        )
                    },
                    label = {
                        Text(
                            "Curriculum",
                            color = colorResource(id = R.color.mmcm_white),
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("curriculum_overview") }
                )
                BottomNavigationItem(
                    icon = {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Tracker",
                            tint = colorResource(id = R.color.mmcm_white)
                        )
                    },
                    label = {
                        Text(
                            "Tracker",
                            color = colorResource(id = R.color.mmcm_white),
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("next_courses") }
                )
                BottomNavigationItem(
                    icon = {
                        Icon(
                            Icons.Default.AccountBox,
                            contentDescription = "Settings",
                            tint = colorResource(id = R.color.mmcm_white)
                        )
                    },
                    label = {
                        Text(
                            "Account",
                            color = colorResource(id = R.color.mmcm_white),
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { navController.navigate("user_account") }
                )
                BottomNavigationItem(
                    icon = {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "About",
                            tint = colorResource(id = R.color.mmcm_white)
                        )
                    },
                    label = {
                        Text(
                            "About",
                            color = colorResource(id = R.color.mmcm_white),
                            fontSize = 12.sp
                        )
                    },
                    selected = false,
                    onClick = { navController.navigate("about_developers") }
                )
            }
        },
        content = content
    )
}

@Preview
@Composable
fun PreviewReusableScaffold() {
    ReusableScaffold(rememberNavController(), topBarTitle = "Title") { paddingValues ->
        // Content
    }
}

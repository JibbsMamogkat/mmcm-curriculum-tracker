package com.mamogkat.mmcmcurriculumtracker.ui.studentscreens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.ui.screens.admin.AdminNextAvailableCoursesScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.AboutDevelopersScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.ChooseCurriculumScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.CurriculumOverviewScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.NextCoursesScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.StudentHomeScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.UserProfileScreen
import kotlinx.coroutines.delay


@Composable
fun StudentMainScreen(navController: NavController) {
    val selectedScreen = remember { mutableStateOf("Home")}
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val studentId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ✅ Reset backPressedOnce after 2 seconds using LaunchedEffect outside BackHandler
    LaunchedEffect(backPressedOnce) {
        if (backPressedOnce) {
            delay(2000L)
            backPressedOnce = false
        }
    }

    // Back button behavior
    BackHandler {
        if (backPressedOnce) {
            // Exit the app
            (context as? Activity)?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Tap again to exit", Toast.LENGTH_SHORT).show()
        }
    }
    Scaffold (
        bottomBar = {
            NavigationBar (
                containerColor = colorResource(id = R.color.mmcm_blue),
                contentColor = colorResource(id = R.color.mmcm_white),
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Curriculum") },
                    label = { Text("Curriculum") },
                    selected = selectedScreen.value == "Curriculum",
                    onClick = { selectedScreen.value = "Curriculum" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.mmcm_blue),
                        unselectedIconColor = colorResource(id = R.color.white),
                        selectedTextColor = colorResource(id = R.color.mmcm_red),
                        unselectedTextColor = colorResource(id = R.color.white)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Next Courses") },
                    label = { Text("Next Courses", fontSize = 10.sp) },
                    selected = selectedScreen.value == "Next Courses",
                    onClick = { selectedScreen.value = "Next Courses" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.mmcm_blue),
                        unselectedIconColor = colorResource(id = R.color.white),
                        selectedTextColor = colorResource(id = R.color.mmcm_red),
                        unselectedTextColor = colorResource(id = R.color.white)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = selectedScreen.value == "Home",
                    onClick = { selectedScreen.value = "Home" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.mmcm_blue),
                        unselectedIconColor = colorResource(id = R.color.white),
                        selectedTextColor = colorResource(id = R.color.mmcm_red),
                        unselectedTextColor = colorResource(id = R.color.white)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = "Account") },
                    label = { Text("Account", fontSize = 10.sp) },
                    selected = selectedScreen.value == "Account",
                    onClick = { selectedScreen.value = "Account" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.mmcm_blue),
                        unselectedIconColor = colorResource(id = R.color.white),
                        selectedTextColor = colorResource(id = R.color.mmcm_red),
                        unselectedTextColor = colorResource(id = R.color.white)
                    )
                )
                NavigationBarItem(
                    icon = {Icon(Icons.Default.Info, contentDescription = "DevInfo") },
                    label = { Text("Info", fontSize = 10.sp) },
                    selected = selectedScreen.value == "Info",
                    onClick = { selectedScreen.value = "Info"},
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colorResource(id = R.color.mmcm_blue),
                        unselectedIconColor = colorResource(id = R.color.white),
                        selectedTextColor = colorResource(id = R.color.mmcm_red),
                        unselectedTextColor = colorResource(id = R.color.white)
                    )
                )
            }
        }
    ) {
            paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedScreen.value) {
                "Home" -> StudentHomeScreen()
                "Curriculum" -> CurriculumOverviewScreen()
                "Next Courses" -> NextCoursesScreen(studentId, viewModel())
                "Account" -> UserProfileScreen(navController = navController,authViewModel = viewModel())
                "Info" -> AboutDevelopersScreen()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewStudentMainScreen() {
    StudentMainScreen( navController = rememberNavController())
}
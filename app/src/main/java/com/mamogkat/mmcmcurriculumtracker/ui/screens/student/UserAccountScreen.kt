package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mamogkat.mmcmcurriculumtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(username: String, email: String, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
            title = { Text("User Profile") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.mmcm_blue),
                titleContentColor = colorResource(id = R.color.mmcm_white)
            ),
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colorResource(id = R.color.mmcm_white))
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
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Home", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Curriculum", color = colorResource(id = R.color.mmcm_white), style = TextStyle(fontSize = 10.sp)) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("curriculum_overview") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Tracker", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Tracker", color = colorResource(id = R.color.mmcm_white), fontSize = 12.sp) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("next_courses") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = "Settings", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Account", color = colorResource(id = R.color.mmcm_white), fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("user_account") }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "About", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("About", color = colorResource(id = R.color.mmcm_white), fontSize = 12.sp) },
                    selected = false,
                    onClick = { navController.navigate("about_developers") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Icon
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            // Username
            Text(
                text = "Username: $username",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Email
            Text(
                text = "Email: $email",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Logout Button
            Button(
                onClick = {navController.navigate("login")},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error // Logout color
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
            ) {
                Text(
                    text = "Log Out",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPage(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.mmcm_blue), // MMCM Blue
                    titleContentColor = colorResource(id = R.color.mmcm_white) // MMCM White
                ),
            )
        },
        bottomBar = {
            BottomNavigation(
                backgroundColor = colorResource(id = R.color.mmcm_blue),
                contentColor = colorResource(id = R.color.mmcm_white)
            ) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Home", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("EXIT", color = colorResource(id = R.color.mmcm_white), fontSize = 12.sp) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("login") }
                )

                BottomNavigationItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Upload", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("UPLOAD", color = colorResource(id = R.color.mmcm_white), fontSize = 12.sp) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { navController.navigate("upload_courses") }
                )

            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Registered Users",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Placeholder for User List
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(10) { index ->
                    UserItem(
                        username = "User$index",
                        email = "user$index@example.com"
                    )
                }
            }
        }
    }
}

@Composable
fun UserItem(username: String, email: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Username: $username",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Email: $email",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview
@Composable
fun AdminPagePreview() {
    AdminPage(rememberNavController())
}
package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomePage(navController: NavController, viewModel: AdminViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminNavigationDrawer(navController, drawerState)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Admin Dashboard", ) },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Welcome, Admin",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

            }
        }
    }
}


@Composable
fun AdminNavigationDrawer(navController: NavController, drawerState: DrawerState) {
    val coroutineScope = rememberCoroutineScope()

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text("Admin Panel", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))


            NavigationDrawerSection("Navigation") {
                DrawerItem("Student Master List", Icons.Default.Person) {
                    navController.navigate("student_master_list")
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerItem("Next Available Courses",
                           Icons.AutoMirrored.Filled.List
                ) {
                    navController.navigate("next_available_courses")
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerItem("Manage Curriculums", Icons.Default.Menu) {
                    navController.navigate("ManageCurriculumsPage")
                    coroutineScope.launch { drawerState.close() }
                }
            }


            NavigationDrawerSection("Settings") {
                DrawerItem("Admin Settings", Icons.Default.Settings) {
                    navController.navigate("AdminSettingsPage")
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerItem("Logout", Icons.Default.ExitToApp) {
                    navController.navigate("login")
                    coroutineScope.launch { drawerState.close() }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = false,
        icon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.padding(12.dp)
    )
}

@Composable
fun NavigationDrawerSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(16.dp)
    )
    Column(content = content)
}

@Preview
@Composable
fun AdminHomePagePreview() {
    val navController = rememberNavController()
    val viewModel = AdminViewModel()
    AdminHomePage(navController, viewModel)
}
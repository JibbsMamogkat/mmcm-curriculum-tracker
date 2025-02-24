package com.mamogkat.mmcmcurriculumtracker.ui.screens.admin


import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.ColorRes
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AdminViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import com.google.firebase.auth.FirebaseAuth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomePage(navController: NavController, viewModel: AdminViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentUserID = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val adminEmail = viewModel.adminEmail.observeAsState("Admin")

    // Fetch email when the screen loads
    LaunchedEffect(currentUserID) {
        if (currentUserID.isNotEmpty()) {
            viewModel.fetchAdminEmail(currentUserID)
        } else {
            Log.e("AdminEmail", "No logged-in user found.")
        }
    }
    LaunchedEffect(adminEmail.value) {
        Log.d("AdminEmail", "Updated Admin Email: ${adminEmail.value}")
    }

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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(id = R.color.mmcm_blue),
                        titleContentColor = colorResource(id = R.color.mmcm_white)
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colorResource(id = R.color.mmcm_white))
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
                    text = "Welcome, ${adminEmail.value}",
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
        Row(
            modifier = Modifier
                .background(colorResource(id = R.color.mmcm_blue))
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
             Image(
                painter = painterResource(id = R.drawable.mmcm_logo),
                contentDescription = "MMCM Logo",
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 24.dp)

            )
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
            Text(
                text = "Admin Panel",
                color = colorResource(id = R.color.mmcm_white),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
            )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            NavigationDrawerSection("Navigation") {
                DrawerItem("Home", Icons.Default.Home)  {
                    navController.navigate("admin_home_page")
                    coroutineScope.launch { drawerState.close() }
                }

                DrawerItem("Student Master List", Icons.Default.Person) {
                    navController.navigate("student_master_list")
                    coroutineScope.launch { drawerState.close() }
                }
                DrawerItem("Manage Curriculums", Icons.Default.Menu) {

                    Log.d("AdminHomePage", "Navigating to ManageCurriculumsPage")
                    navController.navigate("manage_curriculum_page")

                    Log.d("AdminHomePage", "Successfully Navigated to ManageCurriculumsPage")
                    coroutineScope.launch { drawerState.close() }
                }
            }


            NavigationDrawerSection("Settings") {
                DrawerItem("Admin Settings", Icons.Default.Settings) {
                    navController.navigate("AdminSettingsPage")
                    coroutineScope.launch { drawerState.close() }
                }
                // DUFF ADDED for LOGOUT
                val authViewModel: AuthViewModel = viewModel()
                DrawerItem("Logout", Icons.Default.ExitToApp) {
                    authViewModel.logoutUser(navController)
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
        label = { Text(label, style = MaterialTheme.typography.titleMedium) },
        selected = false,
        icon = { Icon(icon, tint = colorResource(R.color.mmcm_blue), contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.padding(12.dp)
    )
}

@Composable
fun NavigationDrawerSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        color = colorResource(id = R.color.mmcm_red),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(16.dp)
    )
    Column(content = content)
}

@Preview
@Composable
fun AdminNavigationDrawerPreview() {
    AdminNavigationDrawer(rememberNavController(), rememberDrawerState(DrawerValue.Closed))
}
package com.mamogkat.mmcmcurriculumtracker.ui.studentscreens

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoadingScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.RunningGif
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.AboutDevelopersScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.ChooseCurriculumScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.CurriculumOverviewScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.NextCoursesScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.UserProfileScreen
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.WaitingForApprovalScreen
import com.mamogkat.mmcmcurriculumtracker.viewmodel.StudentViewModel
import kotlinx.coroutines.delay
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mamogkat.mmcmcurriculumtracker.ui.screens.student.HomePageScreen
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen(navController: NavController) {
    val selectedScreen = remember { mutableStateOf("Home") }
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val studentId = FirebaseAuth.getInstance().currentUser ?.uid ?: ""
    val studentViewModel: StudentViewModel = viewModel()
    val isRefreshing by studentViewModel.isRefreshing.collectAsState()
    val approvalStatus by studentViewModel.approvalStatus.collectAsState()
    val authViewModel: AuthViewModel = viewModel()
    val name by authViewModel.studentName.collectAsState()
    val email = FirebaseAuth.getInstance().currentUser?.email
    // 🔥 Real-time listener for approval status of current user
    LaunchedEffect(Unit) {
        studentViewModel.observeCurrentUserApprovalStatus()
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
            (context as? Activity)?.finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Tap again to exit", Toast.LENGTH_SHORT).show()
        }
    }



    // Modal Navigation Drawer
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val alpha by animateFloatAsState(
        targetValue = if (drawerState.isClosed) 1f else 0f, // Fade in when drawer is closed, fade out when open
        animationSpec = tween(durationMillis = 300) // 300ms animation duration
    )
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val drawerWidth = if (isLandscape) 280.dp else configuration.screenWidthDp.dp * 0.8f

    LaunchedEffect(configuration) {
        coroutineScope.launch { drawerState.close() } // Close drawer on rotation
    }


    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(drawerWidth)
                    .verticalScroll(rememberScrollState())
                    .background(colorResource(id = R.color.mmcm_white)),
                drawerShape = RectangleShape // Removes rounded corners
            )  {
                // Left side: Logo and Title
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(id = R.color.mmcm_blue)) // Set the background color
                            .padding(8.dp) // Optional padding to adjust the layout inside the row
                ) {
                    // Replace with your logo
                    IconButton(onClick = { coroutineScope.launch { drawerState.close() } }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Drawer",
                            modifier = Modifier.size(30.dp),
                            tint = colorResource(R.color.mmcm_white)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    when (name) {
                        null -> Text(
                            text = email ?: "",
                            color = colorResource(R.color.mmcm_white),
                            modifier = Modifier.weight(1f), // Allow text to take available space
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        "user" -> Spacer(modifier = Modifier.height(24.dp))
                        else -> Text(
                            text = name ?: "",
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp).weight(1f),
                            color = colorResource(R.color.mmcm_white),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                                .wrapContentWidth()
                    ) {
                        Text("Status", color = colorResource(R.color.mmcm_white))
                        when (approvalStatus) {
                            "pending" -> Text("PENDING", color = colorResource(R.color.mmcm_red))
                            "approved" -> Text("APPROVED", color = colorResource(R.color.teal_700))
                            else -> CircularProgressIndicator(color = colorResource(id = R.color.mmcm_red))
                        }
                    }

                }
                Spacer(modifier = Modifier.height(20.dp))

                // Right side: Drawer items
                DrawerItem("Home", selectedScreen, drawerState, coroutineScope)
                Divider(modifier = Modifier.padding(vertical = 8.dp))  // Divider between items

                DrawerItem("Next Courses", selectedScreen, drawerState, coroutineScope)
                Divider(modifier = Modifier.padding(vertical = 8.dp))  // Divider between items

                DrawerItem("Curriculum", selectedScreen, drawerState, coroutineScope)
                Divider(modifier = Modifier.padding(vertical = 8.dp))  // Divider between items

                DrawerItem("Account", selectedScreen, drawerState, coroutineScope)
                Divider(modifier = Modifier.padding(vertical = 8.dp))  // Divider between items

                DrawerItem("Info", selectedScreen, drawerState, coroutineScope)
                Divider(modifier = Modifier.padding(vertical = 8.dp))  // Divider between items

                Spacer(modifier = Modifier.weight(1f))  // This spacer pushes the "Log Out" to the bottom

                // Log Out Button
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DrawerItem("Log Out", selectedScreen, drawerState, coroutineScope) {
                    Log.d("StudentScreeen", "Success logout")
                    authViewModel.logoutUser(navController)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorResource(id = R.color.mmcm_white)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "MMCM Curriculum Tracker",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,  // Default heading style
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,  // Font size for the title
                                color = colorResource(id = R.color.mmcm_blue),
                            )
                        } },
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch { drawerState.open() } // Open the drawer
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = colorResource(R.color.mmcm_blue))
                            }
                        },
                        actions = {
                            // Add logo at the end
                            Image(
                                painter = painterResource(id = R.drawable.mmcm_logo),
                                contentDescription = "MMCM Logo",
                                modifier = Modifier
                                    .size(50.dp)
                            )
                        }
                    )
                },
                bottomBar = {
                    BottomNavigation(
                        modifier = Modifier.alpha(alpha) // Apply the fade effect
                    ) {
                        BottomNavigationBar(selectedScreen)
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (selectedScreen.value) {
                        "Home" -> HomePageScreen(
                            onNavigate = { selectedScreen.value = it},
                            innerPadding = paddingValues
                        )
                        "Curriculum" -> SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                            onRefresh = { studentViewModel.refreshData(studentId) }
                        ) {
                            when (approvalStatus) {
                                "pending" -> WaitingForApprovalScreen(context)
                                "approved" -> CurriculumOverviewScreen(studentId)
                                else -> LoadingScreen()
                            }
                        }
                        "Next Courses" -> SwipeRefresh(
                            state = rememberSwipeRefreshState(isRefreshing = isRefreshing),
                            onRefresh = { studentViewModel.refreshData(studentId) }
                        ) {
                            when (approvalStatus) {
                                "pending" -> WaitingForApprovalScreen(context)
                                "approved" -> NextCoursesScreen(studentId, viewModel())
                                else -> LoadingScreen()
                            }
                        }
                        "Account" -> UserProfileScreen(navController = navController, authViewModel = viewModel())
                        "Info" -> AboutDevelopersScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedScreen: MutableState<String>) {
    NavigationBar(
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
            label = { Text("Courses") },
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
            label = { Text("Account") },
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
            icon = { Icon(Icons.Default.Info, contentDescription = "DevInfo") },
            label = { Text("Info") },
            selected = selectedScreen.value == "Info",
            onClick = { selectedScreen.value = "Info" },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = colorResource(id = R.color.mmcm_blue),
                unselectedIconColor = colorResource(id = R.color.white),
                selectedTextColor = colorResource(id = R.color.mmcm_red),
                unselectedTextColor = colorResource(id = R.color.white)
            )
        )
    }
}

@Composable
fun DrawerItem(
    label: String,
    selectedScreen: MutableState<String>,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)  // Ensuring the button is sufficiently tall
            .clickable {
                selectedScreen.value = label
                onClick?.invoke()
                // Close the drawer
                coroutineScope.launch { drawerState.close() }
            }
            .padding(horizontal = 16.dp), // Optional horizontal padding
        contentAlignment = Alignment.CenterStart // Align text to the start of the Box
    ) {
        Text(
            text = label,
            color = colorResource(R.color.mmcm_blue),
            fontSize = 18.sp,
            modifier = Modifier
                .padding(start = 16.dp) // Adds padding from the left for better layout // Ensures the Text takes full size of Box
        )
    }
}

@Preview
@Composable
private fun PreviewStudentMainScreen() {
    StudentMainScreen( navController = rememberNavController())
}
package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.TimerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseCurriculumScreen(navController: NavController, curriculumViewModel: CurriculumViewModel = viewModel()) {
    var showExitDialog by remember { mutableStateOf(false) }
    val authViewModel: AuthViewModel = viewModel()
    // Intercept the system back button
    BackHandler {
        showExitDialog = true
    }
    // ---------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Curriculum") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.mmcm_blue), // mmcm_blue
                    titleContentColor = colorResource(id = R.color.mmcm_white) // mmcm_white
                )
            )
        },
        bottomBar = {
            BottomNavigation(
                backgroundColor = colorResource(id = R.color.mmcm_blue), // mmcm_blue
                contentColor = colorResource(id = R.color.mmcm_white), // mmcm_silver
            ) {
                BottomNavigationItem(
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Home",
                            tint = colorResource(id = R.color.mmcm_white)
                        )
                    },
                    label = { Text("Home", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { /* Handle Home Navigation */ }
                )
                BottomNavigationItem(
                    icon = {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colorResource(id = R.color.mmcm_white)
                        )
                    },
                    label = { Text("Settings", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    onClick = { /* Handle Settings Navigation */ }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            var firstName by remember { mutableStateOf("") }
            var lastName by remember { mutableStateOf("") }
            var nameError by remember { mutableStateOf<String?>(null) }

            // Back confirmation dialog
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Are you sure you want to exit?") },
                    text = { Text("If you go back, you will have to login again.") },
                    confirmButton = {
                        IconButton(onClick = {
                            showExitDialog = false
                            Toast.makeText(context, "You need to login again.", Toast.LENGTH_SHORT)
                                .show()
                            authViewModel.clearState()
                            authViewModel.logoutUser(navController)
                        }) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Confirm",
                                tint = Color.Green
                            )
                        }
                    },
                    dismissButton = {
                        IconButton(onClick = { showExitDialog = false }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Cancel",
                                tint = Color.Red
                            )
                        }
                    }
                )
            }

            // Dropdown and Next Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.mmcm_logo),
                    contentDescription = "MMCM Logo",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(bottom = 24.dp)
                )

                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    isError = nameError != null && firstName.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    isError = nameError != null && lastName.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Pass the combined name to CurriculumDropdown
                CurriculumDropdown(
                    navController = navController,
                    combinedName = "$firstName $lastName".trim(),
                    onNameError = { nameError = it } // Lambda to update the error state
                )
            }
        }
    }
}

@Composable
fun CurriculumDropdown(
    navController: NavController,
    combinedName: String,
    onNameError: (String?) -> Unit,
    viewModel: CurriculumViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val studentProgram by viewModel.studentProgram.observeAsState()

    val curriculumMap = mapOf(
        "BS Computer Engineering" to mapOf(
            "BS Computer Engineering 2022-2023" to "1",
            "BS Computer Engineering 2021-2022" to "2"
        ),
        "BS Electronics and Communications Engineering" to mapOf(
            "BS Electronics and Communications Engineering 2022-2023" to "3"
        ),
        "BS Electrical Engineering" to mapOf(
            "BS Electrical Engineering 2022-2023" to "4"
        )
    )

    // Show only curriculums based on the selected program
    val curriculumList = curriculumMap[studentProgram]?.keys?.toList() ?: emptyList()

    var selectedCurriculum by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var curriculumError by remember { mutableStateOf<String?>(null) }

    Text(
        text = "Select a Curriculum",
        color = colorResource(id = R.color.mmcm_red),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        fontStyle = MaterialTheme.typography.headlineLarge.fontStyle,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedCurriculum,
            onValueChange = {},
            readOnly = true,
            label = { Text(if (curriculumError == null) "Curriculum" else curriculumError ?: "") },
            isError = curriculumError != null,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            curriculumList.forEach { curriculum ->
                DropdownMenuItem(
                    text = { Text(text = curriculum) },
                    onClick = {
                        selectedCurriculum = curriculum
                        curriculumError = null
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = {
            if (combinedName.isNotBlank() && selectedCurriculum.isNotEmpty()) {
                curriculumError = null
                onNameError(null)

                // Retrieve the numeric value from the map
                val curriculumNumber = curriculumMap[studentProgram]?.get(selectedCurriculum)
                if (curriculumNumber != null) {
                    viewModel.updateCurriculumInFirestore(combinedName, curriculumNumber) {
                        navController.navigate("student_main") {
                            popUpTo("choose_curriculum") { inclusive = true }
                        }
                    }
                }
            } else {
                if (combinedName.isBlank()) onNameError("Name must not be empty")
                if (selectedCurriculum.isEmpty()) curriculumError = "Curriculum must not be empty"
            }
        },
        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.mmcm_blue)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Next")
    }
}


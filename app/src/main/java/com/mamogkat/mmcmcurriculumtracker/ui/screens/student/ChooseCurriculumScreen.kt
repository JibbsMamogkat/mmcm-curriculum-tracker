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
                colors =  TopAppBarDefaults.topAppBarColors(
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
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Home", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    selectedContentColor = colorResource(id = R.color.mmcm_red),
                    unselectedContentColor = colorResource(id = R.color.mmcm_white),
                    onClick = { /* Handle Home Navigation */ }
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", tint = colorResource(id = R.color.mmcm_white)) },
                    label = { Text("Settings", color = colorResource(id = R.color.mmcm_white)) },
                    selected = false,
                    onClick = { /* Handle Settings Navigation */ }
                )
            }
        }
    ) { innerPadding ->
        // Main content with padding from Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current  // Store context outside

            // Back confirmation dialog
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Are you sure you want to exit?") },
                    text = { Text("If you go back, you will have to login again.") },
                    confirmButton = {
                        androidx.compose.material3.IconButton(onClick = {
                            showExitDialog = false
                            Toast.makeText(
                                context,
                                "You need to login again.",
                                Toast.LENGTH_SHORT
                            ).show()
                            authViewModel.clearState()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Confirm",
                                tint = Color.Green
                            )
                        }
                    },
                    dismissButton = {
                        androidx.compose.material3.IconButton(onClick = {
                            showExitDialog = false
                        }) {
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

                Spacer(modifier = Modifier.height(24.dp))

                CurriculumDropdown(navController)
            }

        }
    }
}

@Composable
fun CurriculumDropdown(navController: NavController, viewModel: CurriculumViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val studentProgram by viewModel.studentProgram.observeAsState()
    val allCurriculums = mapOf(
        "BS Computer Engineering" to listOf("BS Computer Engineering 2022-2023", "BS Computer Engineering 2021-2022"),
        "BS Electronics and Communications Engineering" to listOf("BS Electronics and Communications Engineering 2022-2023"),
        "BS Electrical Engineering" to listOf("BS Electronics and Communications Engineering 2022-2023")
    )
    val curriculumList = allCurriculums[studentProgram] ?: emptyList()
    var selectedCurriculum by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedTerm by remember { mutableStateOf(1) } // Default term is 1
    var curriculumError by remember { mutableStateOf<String?>(null) } // ✅ For showing error

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
            onValueChange = { },
            readOnly = true,
            label = { Text(if (curriculumError == null) "Curriculum" else curriculumError ?: "") }, // ✅ Show error text in label
            isError = curriculumError != null, // ✅ Set red border if error
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            curriculumList.forEach { curriculum ->
                DropdownMenuItem(
                    text = { Text(text = curriculum) },
                    onClick = {
                        selectedCurriculum = curriculum
                        curriculumError = null // ✅ Clear error when selected
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text("Select the Term You Are Enrolling In", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.mmcm_red))

    Spacer(modifier = Modifier.height(16.dp))

    // Radio button group for selecting term
    val terms = listOf(1, 2, 3)
    terms.forEach { term ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { selectedTerm = term }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = (selectedTerm == term),
                onClick = { selectedTerm = term }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Term $term", fontSize = 18.sp)
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = {
            if (selectedCurriculum.isNotEmpty()) {
                curriculumError = null // ✅ Clear error on success
                viewModel.updateCurriculumInFirestore(selectedCurriculum, selectedTerm) {
                    navController.navigate("student_main") {
                        popUpTo("choose_curriculum") { inclusive = true }
                    }
                }
            } else {
                curriculumError = "Curriculum must not be empty" // ✅ Show error if empty
            }
        },
        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.mmcm_blue)),
        modifier = Modifier.fillMaxWidth(),
        enabled = selectedTerm in terms
    ) {
        Text(text = "Next")
    }
}


@Preview
@Composable
fun ChooseCurriculumScreenPreview() {
    ChooseCurriculumScreen(rememberNavController())
}
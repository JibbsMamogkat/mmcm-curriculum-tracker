package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseCurriculumScreen(navController: NavController) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
    val curriculumList = listOf("BS Computer Engineering 2022-2023", "BS Electronics and Communications Engineering 2022-2023", "BS Computer Engineering 2021-2022")
    var selectedCurriculum by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedTerm by remember { mutableStateOf(1) } // Default term is 1

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
            label = { Text("Curriculum") },
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
                    text = {Text(text = curriculum )},
                    onClick = {
                    selectedCurriculum = curriculum
                    expanded = false
                })
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


    Spacer(modifier = Modifier.height(24.dp))


    Button(
        onClick = {
            if (selectedCurriculum.isNotEmpty()) {
                viewModel.setEnrolledTerm(selectedTerm)
                viewModel.setCurriculum(selectedCurriculum)
                navController.navigate("student_main")
            } else {
                // Handle empty selection (e.g., Snackbar or Toast)
            }
        },
        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.mmcm_blue)),
        modifier = Modifier.fillMaxWidth(),
        enabled = selectedTerm in terms // ensure a term is selected before proceeding
    ) {
        Text(text = "Next")
    }
}

@Preview
@Composable
fun ChooseCurriculumScreenPreview() {
    ChooseCurriculumScreen(rememberNavController())
}
package com.mamogkat.mmcmcurriculumtracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.Snackbar
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
fun CurriculumDropdown(navController: NavController) {
    val curriculumList = listOf("BS Computer Engineering 2022-2023", "BS Electronics and Communications Engineering 2022-2023", "BS Computer Engineering 2021-2022")
    var selectedCurriculum by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

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

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = {
            if (selectedCurriculum.isNotEmpty()) {
                navController.navigate("curriculum_overview")
            } else {
                // Handle empty selection (e.g., Snackbar or Toast)
            }
        },
        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.mmcm_blue)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Next")
    }
}

@Preview
@Composable
fun ChooseCurriculumScreenPreview() {
    ChooseCurriculumScreen(rememberNavController())
}
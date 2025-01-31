package com.mamogkat.mmcmcurriculumtracker.ui.screens


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.RadioButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.ui.theme.Pink80
import com.mamogkat.mmcmcurriculumtracker.ui.theme.WhiteColor

@Composable
fun RegisterUI(navController: NavController?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.mmcm_white)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.mmcm_logo),
                contentDescription = "MMCM Logo",
                modifier = Modifier
                    .size(150.dp)
            )

            // Title
            Text(
                text = "Register for MMCM Tracker",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = MaterialTheme.typography.headlineLarge.fontStyle,
                color = colorResource(id = R.color.mmcm_red),
                modifier = Modifier.offset(y = (-20).dp)

            )

            // Username
            var username by remember { mutableStateOf("") }
            // Email
            var email by remember { mutableStateOf("") }
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        if (it.length <= 10) {
                            username = it
                        }
                    },
                    label = { Text("Username") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .background(WhiteColor, RoundedCornerShape(8.dp))
                        .border(1.dp, WhiteColor, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("MMCM Email") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .background(WhiteColor, RoundedCornerShape(8.dp))
                        .border(1.dp, WhiteColor, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                )
            }
            // Password
            var password by remember { mutableStateOf("") }

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteColor, RoundedCornerShape(8.dp))
                    .border(1.dp, WhiteColor, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            )

            // Confirm password
            var confirmPassword by remember { mutableStateOf("") }
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhiteColor, RoundedCornerShape(8.dp))
                    .border(1.dp, WhiteColor, RoundedCornerShape(8.dp))
                    .padding(2.dp)
            )
            // Admin or Student hehe
            var selectedRole by remember { mutableStateOf("Student") }
            ProgramDropdown(selectedRole)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Text("Register as:", style = MaterialTheme.typography.bodyMedium)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole == "Admin",
                        onClick = { selectedRole = "Admin" }
                    )
                    Text("Admin", modifier = Modifier.clickable { selectedRole = "Admin" })
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedRole == "Student",
                        onClick = { selectedRole = "Student" }
                    )
                    Text("Student", modifier = Modifier.clickable { selectedRole = "Student" })
                }
            }
            // Register
            Button(
                onClick = {
                    navController?.navigate("login")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.mmcm_blue)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Register",
                    color = colorResource(id = R.color.mmcm_white),
                    fontSize = 16.sp
                )
            }
            // Back to Login
            Button(
                onClick = {
                    navController?.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.mmcm_silver)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Back to Login",
                    color = colorResource(id = R.color.mmcm_black),
                    fontSize = 16.sp
                )
            }
        }
    }
}


@Composable
fun ProgramDropdown(selectedRole: String) {
    val programList = listOf("BS Computer Engineering", "BS Electronics and Communications Engineering", "BS Electrical Engineering")
    var selectedProgramList by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedProgramList,
            onValueChange = { },
            readOnly = true,
            enabled = selectedRole == "Student",
            label = { Text("Program") },
            trailingIcon = {
                if(selectedRole == "Student") {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = selectedRole == "Student") { expanded = !expanded }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            programList.forEach { curriculum ->
                DropdownMenuItem(
                    text = {Text(text = curriculum )},
                    onClick = {
                        selectedProgramList = curriculum
                        expanded = false
                    })
            }
        }
    }
}

@Preview
@Composable
fun RegisterUIPreview() {
    RegisterUI(rememberNavController())
}
package com.mamogkat.mmcmcurriculumtracker.ui.screens.auth


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.ui.theme.WhiteColor
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUI(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.errorMessage
    val isSuccess by authViewModel.isSuccess
    var selectedProgram by remember { mutableStateOf("") }

    // Email
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    // Password
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Confirm Password
    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.mmcm_white)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.mmcm_logo),
                contentDescription = "MMCM Logo",
                modifier = Modifier.size(150.dp)
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

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {

                    if (emailError != null) {
                        Text(emailError!!, color = colorResource(id = R.color.mmcm_red))
                    } else if (errorMessage == "Email already registered.") {
                        Text(errorMessage!!, color = colorResource(id = R.color.mmcm_red))
                    } else {
                        Text("MMCM Email", color = colorResource(R.color.mmcm_black))
                    }
                },
                isError = emailError != null || errorMessage == "Email already registered.",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = TextStyle(color = colorResource(id = R.color.mmcm_black)),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            var passwordVisible by remember { mutableStateOf(false) }
            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    if (passwordError != null) {
                        Text(passwordError!!, color = colorResource(id = R.color.mmcm_red))
                    } else {
                        Text("Password", color = colorResource(R.color.mmcm_black))
                    }
                },
                isError = passwordError != null,
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = TextStyle(color = colorResource(id = R.color.mmcm_black)),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                            ),
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            var confirmPasswordVisible by remember { mutableStateOf(false) }
            // Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = {
                    if (confirmPasswordError != null) {
                        Text(confirmPasswordError!!, color = colorResource(id = R.color.mmcm_red))
                    } else {
                        Text("Confirm Password", color = colorResource(R.color.mmcm_black))
                    }
                },
                isError = confirmPasswordError != null,
                textStyle = TextStyle(color = colorResource(id = R.color.mmcm_black)),
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                            ),
                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            // Admin or Student
            var programError by remember { mutableStateOf<String?>(null) }
            var selectedRole by remember { mutableStateOf("Student") }
            ProgramDropdown(selectedRole, programError) { selectedProgram = it }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(4.dp)
            ) {
                Text("Register as:", style = MaterialTheme.typography.bodyMedium, color = colorResource(R.color.mmcm_black))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "Admin",
                        onClick = { selectedRole = "Admin" }
                    )
                    Text("Admin", color = colorResource(R.color.mmcm_black), modifier = Modifier.clickable { selectedRole = "Admin" })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "Student",
                        onClick = { selectedRole = "Student" }
                    )
                    Text("Student", color = colorResource(R.color.mmcm_black), modifier = Modifier.clickable { selectedRole = "Student" })
                }
            }

            // General Error Message
            if (errorMessage != null && errorMessage != "Email already registered.") {
                Text(
                    text = errorMessage!!,
                    color = colorResource(id = R.color.mmcm_red),
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Register Button
            Button(
                onClick = {
                    // Reset Errors
                    emailError = null
                    passwordError = null
                    confirmPasswordError = null
                    programError = null

                    // Validation
                    if (email.isBlank()) emailError = "Email is required"
                    if (password.length < 6) passwordError = "Password must be at least 6 characters"
                    if (confirmPassword != password) confirmPasswordError = "Passwords do not match"
                    if (selectedRole == "Student" && selectedProgram.isBlank()) programError = "Program is required"

                    // Register if no errors
                    if (emailError == null && passwordError == null && confirmPasswordError == null && programError == null) {
                        authViewModel.registerUser(email, password, selectedRole, selectedProgram, navController)
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.mmcm_blue)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    if (isLoading) "Registering..." else "Register",
                    color = colorResource(id = R.color.mmcm_white),
                    fontSize = 16.sp
                )
            }
            // Back to Login
            Button(
                onClick = {
                    authViewModel.clearState()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.mmcm_silver)),
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
fun ProgramDropdown(selectedRole: String, programError: String?, onProgramSelected: (String) -> Unit) {
    val programList = listOf(
        "BS Computer Engineering",
        "BS Electronics and Communications Engineering",
        "BS Electrical Engineering"
    )
    var selectedProgramList by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedProgramList,
            onValueChange = { },
            readOnly = true,
            enabled = selectedRole == "Student",
            label = {
                if (programError != null) {
                    Text(programError, color = colorResource(id = R.color.mmcm_red)) // ✅ Display Error
                } else {
                    Text("Program", color = colorResource(R.color.mmcm_black))
                }
            },
            trailingIcon = {
                if (selectedRole == "Student") {
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
            programList.forEach { program ->
                DropdownMenuItem(
                    text = { Text(text = program, color = colorResource(R.color.mmcm_black)) },
                    onClick = {
                        selectedProgramList = program
                        expanded = false
                        onProgramSelected(program)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun RegisterUIPreview() {
    RegisterUI(rememberNavController())
}
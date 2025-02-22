package com.mamogkat.mmcmcurriculumtracker.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController?, authViewModel: AuthViewModel = viewModel()) {
    val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.errorMessage
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.mmcm_white)),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
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
                // Title
                Text(
                    text = "Welcome to MMCM Tracker",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = MaterialTheme.typography.headlineLarge.fontStyle,
                    color = colorResource(id = R.color.mmcm_red),
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Email Field
                var email by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { if(errorMessage == "Email is not registered.") {
                        Text(errorMessage!!, color = colorResource(id = R.color.mmcm_red))
                    } else if (emailError != null) {
                        Text(emailError!!, color = colorResource(R.color.mmcm_black))
                    }
                    else {
                        Text("Email", color = colorResource(R.color.mmcm_black))
                    }
                            },
                    isError = errorMessage == "Email is not registered.",
                    textStyle = TextStyle(color = colorResource(id = R.color.mmcm_black)),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Password Field
                var password by remember { mutableStateOf("") }
                var passwordVisible by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        if (errorMessage == "Incorrect password. Please try again." || errorMessage == "User account not found. Please register.") {
                            Text(errorMessage!!, color = colorResource(id = R.color.mmcm_red))
                        } else if (passwordError != null) {
                            Text(passwordError!!, color = colorResource(R.color.mmcm_black))
                        }
                        else {
                            Text("Password", color = colorResource(R.color.mmcm_black))
                        }
                    },
                    isError = errorMessage == "Incorrect password. Please try again." || errorMessage == "User account not found. Please register.",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Display general error message
                if (!errorMessage.isNullOrEmpty() && errorMessage != "Email is not registered." && errorMessage != "Incorrect password. Please try again." && errorMessage != "User account not found. Please register.") {
                    Text(
                        text = errorMessage ?: "An error occured",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Login Button
                Button(
                    onClick = {
                        // Reset Errors
                        emailError = null
                        passwordError = null
                        if (email.isBlank()) {
                            emailError = "Email is required"
                        }
                        if (password.isBlank()) {
                            passwordError = "Input your password"
                        }
                        if (emailError == null && passwordError == null) {
                            authViewModel.loginUser(email, password, navController!!)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.mmcm_blue)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Login",
                        color = colorResource(id = R.color.mmcm_white),
                        fontSize = 16.sp
                    )
                }

                // Forgot Password Link
                Spacer(modifier = Modifier.height(7.dp))
                Text(
                    text = "Forgot Password?",
                    color = colorResource(id = R.color.mmcm_blue),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        navController?.navigate("forgot_password_screen")
                    }
                )

                // Sign Up Link
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { navController?.navigate("register_ui") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.mmcm_silver)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        color = colorResource(id = R.color.mmcm_black),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(navController = null)
}
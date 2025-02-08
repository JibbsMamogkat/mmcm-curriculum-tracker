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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
                    label = { Text("Email") },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = colorResource(id = R.color.mmcm_white),
                        focusedIndicatorColor = colorResource(id = R.color.mmcm_blue),
                        unfocusedIndicatorColor = colorResource(id = R.color.mmcm_silver),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Password Field
                var password by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = colorResource(id = R.color.mmcm_white),
                        focusedIndicatorColor = colorResource(id = R.color.mmcm_blue),
                        unfocusedIndicatorColor = colorResource(id = R.color.mmcm_silver),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Display error message if any
                if (!errorMessage.isNullOrEmpty()) {
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
                        if (email.isBlank() || password.isBlank()) {
                            authViewModel.setErrorMessage("Email and Password cannot be blank!")
                        } else {
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
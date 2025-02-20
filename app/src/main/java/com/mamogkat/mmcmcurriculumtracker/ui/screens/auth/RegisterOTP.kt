package com.mamogkat.mmcmcurriculumtracker.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.wear.compose.material3.Button
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel

@Composable
fun VerifyOtpScreen(email: String, password: String, role: String, program: String, navController: NavController, authViewModel: AuthViewModel) {
    var otp by remember { mutableStateOf("") }
    val isLoading by authViewModel.isLoading
    val errorMessage by authViewModel.errorMessage

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter OTP sent to $email")

        TextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("OTP") }
        )

        if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red)
        }

        Button(
            onClick = {
                authViewModel.verifyOtp(email, otp, password, role, program, navController)
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Verifying..." else "Verify OTP")
        }
    }
}

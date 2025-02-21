package com.mamogkat.mmcmcurriculumtracker.ui.screens.auth

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.TimerViewModel
import java.util.Locale

@Composable
fun VerifyOtpScreen(email: String, password: String, role: String, program: String, navController: NavController, authViewModel: AuthViewModel) {
    var otp by remember { mutableStateOf("") }
    val isLoading by remember { mutableStateOf(false) }
    val errorMessage by authViewModel.errorMessage

    var showExitDialog by remember { mutableStateOf(false) }
    // Intercept the system back button
    BackHandler {
        showExitDialog = true
    }

    val timerViewModel: TimerViewModel = viewModel()

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, secs)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current  // Store context outside

            // Back confirmation dialog
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Are you sure you want to exit?") },
                    text = { Text("If you go back, you will have to register again.") },
                    confirmButton = {
                        IconButton(onClick = {
                            showExitDialog = false
                            Toast.makeText(context, "You need to register again.", Toast.LENGTH_SHORT).show()
                            navController.navigate("register_ui") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.Filled.Check, contentDescription = "Confirm", tint = Color.Green)
                        }
                    },
                    dismissButton = {
                        IconButton(onClick = { showExitDialog = false }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = Color.Red)
                        }
                    }
                )
            }
            Image(
                painter = painterResource(id = R.drawable.mmcm_logo),
                contentDescription = "MMCM Logo",
                modifier = Modifier.size(150.dp)
            )
            Text(
                text = "MMCM Curriculum Tracker",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.mmcm_red)
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Enter the verification code sent to:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(id = R.color.mmcm_blue)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(id = R.color.mmcm_black)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val isError = errorMessage != null

            TextField(
                value = otp,
                onValueChange = { otp = it },
                label = {
                    Text(
                        text = if (isError) errorMessage!! else "OTP",
                        color = if (isError) colorResource(id = R.color.mmcm_red) else colorResource(id = R.color.mmcm_black)
                    )
                },
                textStyle = TextStyle(color = colorResource(id = R.color.mmcm_black)),
                modifier = Modifier.fillMaxWidth(0.7f),
                isError = isError,  // ✅ Highlights border in red when true
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,  // Remove background color when error
                    errorIndicatorColor = colorResource(id = R.color.mmcm_red), // ✅ Border color on error
                    focusedIndicatorColor = colorResource(id = R.color.mmcm_black),
                    unfocusedIndicatorColor = Color.Gray
                )
            )
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    authViewModel.verifyOtp(email, otp, password, role, program, navController)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.mmcm_blue),
                    contentColor = colorResource(id = R.color.mmcm_white)
                ),
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(text = if (isLoading) "Verifying..." else "Verify")
            }
            Spacer(modifier = Modifier.height(32.dp))

            if (!timerViewModel.isResending.value) {
                Text(
                    text = "Resend Verification Code",
                    color = colorResource(id = R.color.mmcm_blue),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        timerViewModel.startTimer()
                        authViewModel.sendOtp(email) { success ->
                            if (!success) {
                                authViewModel.setErrorMessage("Failed to resend OTP. Please try again.")
                            }
                        }
                    }
                )
            } else {
                Text(
                    text = "Resend again in: ${formatTime(timerViewModel.resendTime.value)}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Back to Login",
                color = colorResource(id = R.color.mmcm_blue),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
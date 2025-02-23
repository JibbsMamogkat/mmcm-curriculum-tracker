package com.mamogkat.mmcmcurriculumtracker.ui.screens.auth
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel

@Composable
fun ChangePasswordScreen(
    viewModel: AuthViewModel,
    navController: NavController,
    email: String
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    var showExitDialog by remember { mutableStateOf(false) }

    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    // Intercept the system back button
    BackHandler {
        showExitDialog = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    IconButton(onClick = {
                        showExitDialog = false
                        Toast.makeText(context, "You need to login again.", Toast.LENGTH_SHORT).show()
                        viewModel.clearState()
                        navController.navigate("login") {
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
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = colorResource(id = R.color.mmcm_red)
        )
        Text(
            text = "Change Password",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // New Password
        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = {
                if (newPasswordError != null) {
                    Text(newPasswordError!!, color = colorResource(id = R.color.mmcm_red))
                } else {
                    Text("New Password", color = colorResource(id = R.color.mmcm_black))
                }
            },
            isError = newPasswordError != null,
            singleLine = true,
            visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(color = colorResource(id = R.color.mmcm_black)),
            trailingIcon = {
                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (newPasswordVisible) R.drawable.ic_eye_open else R.drawable.ic_eye_closed
                        ),
                        contentDescription = if (newPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = {
                if (confirmPasswordError != null) {
                    Text(confirmPasswordError!!, color = colorResource(id = R.color.mmcm_red))
                } else {
                    Text("Confirm Password", color = colorResource(id = R.color.mmcm_black))
                }
            },
            isError = confirmPasswordError != null,
            singleLine = true,
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(color = colorResource(id = R.color.mmcm_black)),
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

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = Color.Red,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Change Password Button
        Button(
            onClick = {
                // Reset errors
                newPasswordError = null
                confirmPasswordError = null

                // Validation
                if (newPassword.length < 6) newPasswordError = "Password must be at least 6 characters"
                if (confirmPassword != newPassword) confirmPasswordError = "Passwords do not match"

                if (newPasswordError == null && confirmPasswordError == null) {
                    viewModel.changePassword(email, newPassword, navController)
                }
            },
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.mmcm_blue)),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Change Password", color = colorResource(id = R.color.mmcm_white), fontSize = 16.sp)
            }
        }
    }
}

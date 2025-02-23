package com.mamogkat.mmcmcurriculumtracker.ui.screens.auth
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    var showExitDialog by remember { mutableStateOf(false) }
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

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
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

        Button(
            onClick = {
                if (newPassword == confirmPassword) {
                    viewModel.changePassword(email, newPassword, navController)
                } else {
                    viewModel.setErrorMessage("Passwords do not match.")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Change Password")
            }
        }
    }
}

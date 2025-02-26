package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.viewmodel.AuthViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.CurriculumViewModel
import com.mamogkat.mmcmcurriculumtracker.viewmodel.StudentViewModel
import kotlinx.coroutines.tasks.await

@Composable
fun UserProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    val auth = Firebase.auth
    val email = auth.currentUser?.email ?: "No email available"
    val name by authViewModel.studentName.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) } // State for name change dialog
    val studentViewModel: StudentViewModel = viewModel()
    val approvalStatus by studentViewModel.approvalStatus.collectAsState()
    val lastNameChangeTime by authViewModel.lastNameChangeTime.collectAsState()

    // Check if 7 days have passed
    val canChangeName = lastNameChangeTime?.let {
        val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000 // 7 days in milliseconds
        val currentTime = System.currentTimeMillis()
        currentTime - it >= threeDaysInMillis
    } ?: true // If null, allow name change

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    colorResource(R.color.mmcm_blue),
                                    colorResource(R.color.mmcm_silver)
                                )
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name Section
                    when (name) {
                        null -> CircularProgressIndicator()
                        "" -> Text(
                            text = "No Name Available",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        else -> Text(
                            text = name ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.mmcm_black),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorResource(R.color.mmcm_silver),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .wrapContentWidth()
                    ) {
                        Text("Status", color = colorResource(R.color.mmcm_blue))
                        when (approvalStatus) {
                            "pending" -> Text("PENDING", color = colorResource(R.color.mmcm_red))
                            "approved" -> Text("APPROVED", color = colorResource(R.color.teal_700))
                            else -> CircularProgressIndicator(color = colorResource(id = R.color.mmcm_red))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔵 Change Name Button
                    Button(
                        onClick = { showNameDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canChangeName // Disable button if name change is not allowed
                    ) {
                        Text(
                            text = if (canChangeName) "Changing name" else "Wait 3 Days to change name again",
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔴 Change Password Button
                    Button(
                        onClick = { authViewModel.forgotPassword(email, navController) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Change Password", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(90.dp))

                    // 🚪 Log Out Button
                    Button(
                        onClick = { authViewModel.logoutUser(navController) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Log Out", color = Color.White)
                    }
                }
            }
        }
    }

    // 🔹 Change Name Dialog
    if (showNameDialog) {
        ChangeNameDialog(
            currentName = name ?: "",
            onDismiss = { showNameDialog = false },
            onSave = { newName ->
                authViewModel.updateStudentName(newName)
                showNameDialog = false
            }
        )
    }
}
@Composable
fun ChangeNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Name") },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it.trimStart().replace(Regex("\\s+"), " ") },
                    label = { Text("Enter new name") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(newName) }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}







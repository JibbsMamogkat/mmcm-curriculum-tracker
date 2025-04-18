package com.mamogkat.mmcmcurriculumtracker.ui.screens.auth

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mamogkat.mmcmcurriculumtracker.R
import androidx.compose.foundation.layout.size
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest

@Composable
fun RunningGif(context: Context) {
    val imageLoader = ImageLoader.Builder(context)
        .components { add(GifDecoder.Factory()) }
        .build()

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(R.drawable.running_avatar)
            .build(),
        imageLoader = imageLoader,
        contentDescription = "Running Avatar",
        modifier = Modifier.size(250.dp)
    )
}

@Composable
fun LoadingScreen() {
    val context = LocalContext.current

    BackHandler {
        Toast.makeText(context, "Please wait", Toast.LENGTH_SHORT).show()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = colorResource(id = R.color.mmcm_blue))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sending OTP, please wait...",
                fontSize = 18.sp,
                color =  colorResource(R.color.mmcm_blue),
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    val context = LocalContext.current

    // Disable the system back button and show toast
    BackHandler {
        Toast.makeText(context, "Press back to login", Toast.LENGTH_SHORT).show()
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA) // Light background similar to uploaded image
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(4.dp, colorResource(id = R.color.mmcm_red), CircleShape), // Purple border
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Error",
                    tint = colorResource(id = R.color.mmcm_red), // Purple X
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Text
            Text(
                text = "Error",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = colorResource(id = R.color.mmcm_red),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Retry Button
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.mmcm_blue),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
            ) {
                Text("Back to Login", fontSize = 16.sp)
            }
        }
    }
}
@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}


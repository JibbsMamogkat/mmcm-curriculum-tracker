package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import android.content.res.Configuration
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mamogkat.mmcmcurriculumtracker.R
import com.mamogkat.mmcmcurriculumtracker.ui.screens.auth.LoadingScreen
import com.mamogkat.mmcmcurriculumtracker.viewmodel.StudentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


@Composable
fun HomePageScreen(onNavigate: (String) -> Unit, innerPadding: PaddingValues) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    var screenHeight by remember { mutableStateOf(configuration.screenHeightDp.dp) }
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    // FOR ANIMATION BACKGROUND

    // ---------------------------------------------------------

    LaunchedEffect(configuration) {
        screenHeight = configuration.screenHeightDp.dp // Update height dynamically
    }

    // Only subtract bottom padding in portrait mode
    val adjustedHeight = if (isPortrait) {
        screenHeight - innerPadding.calculateTopPadding() - innerPadding.calculateBottomPadding()
    } else {
        screenHeight // Ignore bottom padding
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // First Box (Landing Section) - Full Screen Height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(adjustedHeight) // 100% of the screen height
                .background(color = colorResource(R.color.mmcm_blue)),
            contentAlignment = Alignment.Center
        ) {
            // Background texts mimicking shoes' placement
            Text(
                text = "EECE",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .rotate(-15f)
                    .offset(x = (-80).dp, y = (-100).dp)
            )

            Text(
                text = "CPE",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 90.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .rotate(10f)
                    .offset(x = (80).dp, y = (-80).dp)
            )

            Text(
                text = "ENGINEER",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 70.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .rotate(-5f)
                    .offset(x = (-50).dp, y = (120).dp)
            )

            Text(
                text = "CEA",
                color = Color.White.copy(alpha = 0.2f),
                fontSize = 85.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .rotate(15f)
                    .offset(x = (100).dp, y = (100).dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mmcm_logo), // Replace with actual logo resource
                    contentDescription = "MMCM Logo",
                    modifier = Modifier.size(80.dp)
                )
                Text(
                    text = "CPE144L",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "MMCM",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "CURRICULUM TRACKER",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val scrollTo = with(density) { adjustedHeight.toPx() }.toInt() // Convert Dp to Px
                            scrollState.animateScrollBy(scrollTo.toFloat(), animationSpec = tween(durationMillis = 800))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.mmcm_white),
                        contentColor = colorResource(R.color.mmcm_blue)
                    )
                ) {
                    Text("GET STARTED")
                }
            }
        }

// Second Box (Content Section) - Full Screen Height
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(adjustedHeight)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.mmcm_blue),
                            colorResource(R.color.mmcm_orange)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "WELCOME",
                    color = colorResource(R.color.mmcm_white),
                    fontSize = 24.sp, // Adjust size as needed
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "to",
                    color = colorResource(R.color.mmcm_white),
                    fontSize = 16.sp, // Smaller size
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "MMCM",
                    color = colorResource(R.color.mmcm_white),
                    fontSize = 30.sp, // Larger size
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "CURRICULUM TRACKER",
                    color = colorResource(R.color.mmcm_white),
                    fontSize = 24.sp, // Adjust size as needed
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Stay on top of your academic journey. Track completed courses, check curriculum progress, and explore available courses!",
                    color = colorResource(R.color.mmcm_white).copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeatureItem(icon = Icons.Default.Check, text = "Track Progress")
                    FeatureItem(icon = Icons.Default.Person, text = "Get Approved")
                    FeatureItem(icon = Icons.Default.Create, text = "View Curriculum")
                }

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedCircleButton(
                        text = "Curriculum",
                        icon = Icons.Default.Build,
                        onClick = { onNavigate("Curriculum") }
                    )
                    AnimatedCircleButton(
                        text = "Next Courses",
                        icon = Icons.Default.ArrowForward,
                        onClick = { onNavigate("Next Courses") }
                    )
                }
            }
        }
    }
}
@Composable
fun AnimatedCircleButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    val transition = rememberInfiniteTransition()

    // Blinking effect (opacity animation)
    val animatedAlpha by transition.animateFloat(
        initialValue = 0.6f, // Slightly faded
        targetValue = 1f, // Fully visible
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Glowing effect (soft shadow pulse)
    val animatedGlow by transition.animateFloat(
        initialValue = 4f,
        targetValue = 20f, // Expands and shrinks the glow effect
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(80.dp) // Circular button
            .clip(CircleShape)
            .background(colorResource(R.color.mmcm_blue).copy(alpha = animatedAlpha)) // Blinking effect
            .drawBehind {
                drawCircle(
                    color = Color(0xFF2575FC).copy(alpha = 0.3f), // Glow color
                    radius = animatedGlow.dp.toPx() // Smooth glow expansion
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}







@Composable
fun FeatureItem(icon: ImageVector, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = text,
            modifier = Modifier.size(32.dp),
            tint = colorResource(R.color.mmcm_white) // Set icon color to white
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text,
            fontSize = 14.sp,
            color = colorResource(R.color.mmcm_white) // Set text color to white
        )
    }
}

/*Text(
           text = "ECE",
           color = Color.White.copy(alpha = 0.2f),
           fontSize = 65.sp,
           fontWeight = FontWeight.Bold,
           modifier = Modifier
               .rotate(-60f)
               .offset(x = (180).dp, y = (-70).dp) // Positioned above the logo
       )

       Text(
           text = "EE",
           color = Color.White.copy(alpha = 0.2f),
           fontSize = 60.sp,
           fontWeight = FontWeight.Bold,
           modifier = Modifier
               .rotate(-15f)
               .offset(x = (250).dp, y = (-200).dp) // Uppermost right
       )
       Text(
           text = "EE",
           color = Color.White.copy(alpha = 0.2f),
           fontSize = 60.sp,
           fontWeight = FontWeight.Bold,
           modifier = Modifier
               .rotate(-15f)
               .offset(x = (-120).dp, y = (-250).dp) // Uppermost right
       )*/


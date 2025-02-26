package com.mamogkat.mmcmcurriculumtracker.ui.screens.student

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mamogkat.mmcmcurriculumtracker.R


@Composable
fun AvailabilityLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BlinkingDot(color = Color.Green)
            Text(
                text = "Available",
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BlinkingDot(color = colorResource(R.color.mmcm_orange))
            Text(
                text = "Might not be available",
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            BlinkingDot(color = Color.Cyan)
            Text(
                text = "Electives",
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}
@Composable
fun BlinkingDot(color: Color) {
    val glowAlpha = remember { Animatable(0.3f) }

    LaunchedEffect(Unit) {
        while (true) {
            glowAlpha.animateTo(0.8f, animationSpec = tween(500, easing = LinearEasing))
            glowAlpha.animateTo(0.3f, animationSpec = tween(500, easing = LinearEasing))
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(16.dp) // Ensure enough space for glow effect
    ) {
        // Outer glow effect (blinks in a circular shape)
        Box(
            modifier = Modifier
                .size(12.dp) // Glow size
                .background(color.copy(alpha = glowAlpha.value), shape = CircleShape) // Keep glow circular
                .blur(radius = 8.dp) // Soft glow effect
        )

        // Inner solid dot (steady)
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = CircleShape) // Solid inner dot
        )
    }
}



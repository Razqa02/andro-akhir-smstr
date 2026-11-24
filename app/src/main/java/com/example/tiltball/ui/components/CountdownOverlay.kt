package com.example.tiltball.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiltball.ui.theme.Primary

/** Overlay hitung mundur 3-2-1 modern di tengah arena. */
@Composable
fun CountdownOverlay(value: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (scaleIn(initialScale = 0.4f, animationSpec = tween(350, easing = FastOutSlowInEasing)) +
                        fadeIn(tween(350)))
                    .togetherWith(fadeOut(tween(180)))
            },
            label = "countdown"
        ) { count ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(2.dp, Primary.copy(alpha = 0.6f), CircleShape)
            ) {
                Text(
                    text = if (count > 0) count.toString() else "GO!",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = if (count > 0) 72.sp else 48.sp,
                        letterSpacing = 2.sp
                    ),
                    fontWeight = FontWeight.Black,
                    color = Primary
                )
            }
        }
    }
}

package com.example.tiltball.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiltball.ui.theme.*

/**
 * Layar Game Over: Waktu habis. Desain bersih, memotivasi, dan cepat untuk retry.
 */
@Composable
fun GameOverScreen(
    level: Int,
    onReplay: () -> Unit,
    onMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Ikon Waktu Habis dengan Glow Merah
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Error.copy(alpha = 0.15f))
                    .shadow(16.dp, CircleShape, ambientColor = Error, spotColor = Error)
            ) {
                Icon(
                    Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "WAKTU HABIS!",
                style = MaterialTheme.typography.headlineMedium.copy(letterSpacing = 1.sp),
                fontWeight = FontWeight.Black,
                color = Error
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Batas waktu Level $level telah terlewati.",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant
            )

            Text(
                text = "Jangan menyerah, coba lagi dengan kemiringan lebih lincah!",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(36.dp))

            // Tombol Coba Lagi
            Button(
                onClick = onReplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Primary, spotColor = Primary),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = OnPrimary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "COBA LAGI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnPrimary
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Text(
                    text = "Kembali ke Menu Utama",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
            }
        }
    }
}

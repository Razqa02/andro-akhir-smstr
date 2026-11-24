package com.example.tiltball.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tiltball.ui.theme.*

/** Layar panduan cara bermain game Tilt Ball yang rapi dan mudah dipahami. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cara Bermain",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground,
                    navigationIconContentColor = OnBackground
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            InstructionCard(
                icon = "📱",
                title = "1. Menggerakkan Bola (Tilt Control)",
                content = "Pegang HP mendatar. Miringkan HP secara santai:\n" +
                        "• Miring ke kiri → Bola menggelinding ke kiri\n" +
                        "• Miring ke kanan → Bola menggelinding ke kanan\n" +
                        "• Miring ke depan (atas turun) → Bola meluncur ke atas\n" +
                        "• Miring ke belakang (atas naik) → Bola meluncur ke bawah\n\n" +
                        "Tips: Cukup miringkan sedikit saja. Saat HP didatarkan, bola akan otomatis melambat dan berhenti."
            )

            InstructionCard(
                icon = "🎯",
                title = "2. Tujuan Permainan",
                content = "Arahkan bola dari titik awal (kotak hijau bertuliskan S) menuju titik finish (portal emas bersinar bertuliskan F) secepat mungkin sebelum waktu habis!"
            )

            InstructionCard(
                icon = "🧭",
                title = "3. Gerbang Kompas (Magnetometer)",
                content = "Pada level tertentu, lorong dihalangi oleh Gerbang Kompas (balok cyan bergembok):\n" +
                        "• Cara 1 (Putar Badan): Putar badan Anda sesuai petunjuk di HUD atas (misal: 'Putar badan ke kanan 90°') dan tahan ~0.5 detik.\n" +
                        "• Cara 2 (Sentuh Cepat): Anda juga dapat langsung menekan tombol 'Buka Manual' di baris atas jika sedang bermain di tempat sempit/tiduran!\n\n" +
                        "Kontrol bola tetap stabil dan tidak terpengaruh oleh putaran badan."
            )

            InstructionCard(
                icon = "🕳️",
                title = "4. Rintangan Lubang",
                content = "Hindari lubang hitam (O)! Jika bola jatuh ke dalam lubang:\n" +
                        "• Bola kembali ke posisi awal (S)\n" +
                        "• HP bergetar singkat\n" +
                        "• Waktu penalti bertambah +3 detik"
            )

            InstructionCard(
                icon = "⏸️",
                title = "5. Jeda Permainan (Pause)",
                content = "• Tombol jeda (⏸) selalu ada di kanan atas layar.\n" +
                        "• Fitur 'Auto-pause sensor jarak' dapat diaktifkan di menu Tes Sensor. Jika aktif, mendekatkan tangan ke atas HP akan menjeda game otomatis."
            )

            InstructionCard(
                icon = "⭐",
                title = "6. Perhitungan Skor",
                content = "Skor dihitung murni dari kecepatan penyelesaian:\n" +
                        "Skor = maks(0, 1000 - Total Detik × 10)\n\n" +
                        "Selesaikan labirin secepat mungkin tanpa terjatuh ke lubang untuk mencetak Rekor Tertinggi!"
            )

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InstructionCard(icon: String, title: String, content: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row {
                Text(text = icon, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface,
                lineHeight = 22.sp
            )
        }
    }
}

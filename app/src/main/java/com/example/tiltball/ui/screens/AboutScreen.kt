package com.example.tiltball.ui.screens

import androidx.compose.foundation.BorderStroke
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

/** Layar informasi aplikasi, arsitektur, teknologi, dan penggunaan sensor. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tentang Tilt Ball",
                        fontWeight = FontWeight.Bold,
                        color = OnBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = OnBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            AboutSectionCard(
                title = "🎮 Tilt Ball v2.0",
                content = "Game arcade labirin interaktif berbasis sensor perangkat Android. " +
                        "Didesain dengan kontrol kemiringan yang halus, visual dark-arcade modern, " +
                        "dan mekanisme gerbang kompas cerdas untuk pengalaman bermain yang simpel dan adiktif."
            )

            AboutSectionCard(
                title = "🛠️ Teknologi & Arsitektur",
                content = "• Bahasa: Kotlin 2.2.10\n" +
                        "• UI Framework: Jetpack Compose + Material 3 (100% Declarative)\n" +
                        "• Rendering Arena: Compose Canvas murni beresolusi tinggi dengan shading 3D\n" +
                        "• Arsitektur: MVVM + Repository Pattern (Clean Architecture)\n" +
                        "• State Management: StateFlow + collectAsStateWithLifecycle (Atomic updates)\n" +
                        "• Database: SQLite native via SQLiteOpenHelper (Transactional & Zero ORM overhead)\n" +
                        "• Navigasi: Navigation Compose dengan Single Shared ViewModel\n" +
                        "• Sensor API: Android SensorManager via Coroutines callbackFlow"
            )

            AboutSectionCard(
                title = "📡 Sensor yang Digunakan",
                content = "Aplikasi ini memanfaatkan 3 sensor fisik asli perangkat (dioptimalkan untuk Redmi 12):\n\n" +
                        "1. Accelerometer (Kategori Motion):\n" +
                        "   Mengukur kemiringan ponsel dengan low-pass filter (α=0.8) dan dead-zone cerdas untuk kontrol bola yang presisi tanpa licin.\n\n" +
                        "2. Magnetometer / Kompas (Kategori Position):\n" +
                        "   Mengukur medan magnet bumi untuk menghitung sudut rotasi badan pemain saat membuka Gerbang Kompas (dilengkapi tombol buka manual untuk kenyamanan).\n\n" +
                        "3. Proximity (Kategori Position - Opsional):\n" +
                        "   Mendeteksi objek di atas layar untuk jeda otomatis saat layar tertutup tangan/diletakkan terbalik."
            )

            AboutSectionCard(
                title = "📱 Kompatibilitas Perangkat",
                content = "• Min SDK: Android 7.0 (API 24)\n" +
                        "• Target SDK: Android 15 (API 35)\n" +
                        "• Diuji & dioptimalkan: Xiaomi Redmi 12 & seluruh ponsel Android dengan sensor accelerometer dan magnetometer.\n" +
                        "• Tidak memerlukan gyroscope atau koneksi internet (100% Offline & Hemat Daya)."
            )

            AboutSectionCard(
                title = "👨‍💻 Pengembang",
                content = "Dibuat dengan dedikasi tinggi sebagai proyek demonstrasi arsitektur Android modern, bersih, dan performa tinggi."
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutSectionCard(title: String, content: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
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

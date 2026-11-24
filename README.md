# 🎮 Tilt Ball v2.0

Game Android arcade labirin satu pemain berbasis sensor fisik yang dibangun menggunakan **Kotlin**, **Jetpack Compose (Material 3)**, dan **SQLite murni (SQLiteOpenHelper)**. Pemain mengarahkan bola dari titik awal (*Start*) menuju titik tujuan (*Finish*) dengan memiringkan perangkat fisik sebelum batas waktu habis, sembari menghindari rintangan lubang dan membuka **Gerbang Kompas** dengan memutar badan.

Proyek ini dirancang dengan arsitektur bersih (**MVVM + Repository Pattern**), tanpa framework pihak ketiga (100% dependensi Android bawaan/Jetpack), mudah dipelajari siswa/mahasiswa, dan diuji langsung pada perangkat fisik seperti **Xiaomi Redmi 12**.

---

## 🌟 Fitur Utama

1. **Fisika Bola Realistis & Responsif (Accelerometer)**:
   - Digerakkan oleh sensor *Accelerometer* fisik dengan *low-pass filter* (α = 0.8) untuk meredam getaran tangan dan *dead-zone* (0.3 m/s²) agar bola tidak bergerak sendiri saat HP diletakkan datar.
   - Deteksi tabrakan lingkaran vs kotak (*Circle-AABB Collision*) dengan *sub-stepping* sehingga bola tidak pernah menembus dinding.
2. **Gerbang Kompas (Magnetometer + Accelerometer)**:
   - Pada Level 2 ke atas, lorong menuju finish dihalangi oleh Gerbang Kompas ('G') yang bertindak sebagai dinding saat tertutup.
   - Memanfaatkan sensor *Magnetometer* (`TYPE_MAGNETIC_FIELD`) bersama *Accelerometer* melalui `SensorManager.getRotationMatrix` dan `getOrientation`.
   - Menggunakan arah relatif (*relative heading*): arah saat countdown selesai disimpan sebagai acuan nol (`headingRef`).
   - Pemain memutar badan sesuai instruksi HUD (contoh: "Putar badan ke kanan 90°") dan menahan arah selama 800 ms. Progress ring akan terisi hingga penuh, lalu gerbang terbuka permanen.
   - Kontrol bola tetap murni relatif terhadap layar HP dan tidak terganggu oleh putaran badan.
   - **Fallback Jujur**: Jika magnetometer tidak ada, tidak merespons, atau tidak akurat (*unreliable* > 5 detik), tombol "Buka Manual" akan otomatis muncul di HUD.
3. **Auto-Pause Sensor Jarak Opsional (Proximity)**:
   - Proximity dijadikan fitur opsional dengan toggle di layar Diagnostik Sensor (default: MATI).
   - Jika diaktifkan dan sensor tersedia, mendekatkan tangan ke dahi HP akan menjeda game secara otomatis.
   - Tombol *Lanjutkan* terkunci dan meminta pengguna menjauhkan tangan sebelum permainan dapat diteruskan.
   - Tombol *Pause* manual selalu tersedia di pojok kanan atas HUD.
4. **5 Level Menantang**:
   - Level 1: *Arena Terbuka* (tanpa gerbang, batas 70 detik).
   - Level 2: *Gerbang Kanan* (target +90°, batas 80 detik).
   - Level 3: *Gerbang Kiri* (target -90°, batas 90 detik).
   - Level 4: *Putar Balik* (target 180°, batas 80 detik).
   - Level 5: *Labirin Master* (target +90°, batas 90 detik).
   - Seluruh level telah divalidasi dengan algoritma **Breadth-First Search (BFS)** pada startup untuk menjamin selalu dapat diselesaikan.
5. **Database Riwayat & Rekor SQLite (Database Versi 2)**:
   - Penyimpanan data permanen offline menggunakan `SQLiteOpenHelper` (SQL native).
   - Mencatat skor tertinggi keseluruhan, rekor terbaik per level, riwayat tiap sesi permainan, serta progres pembukaan level bertahap (Level 1 terbuka secara default).
   - Skor murni berbasis waktu: `max(0, 1000 - detikTotal * 10)`.
6. **Layar Diagnostik Sensor Lengkap (SensorScreen)**:
   - Ringkasan status sensor wajib: "Sensor wajib (Accelerometer + Magnetometer): OK / BERMASALAH".
   - Deteksi *watchdog* 2 detik untuk mengetahui apakah sensor terdaftar tapi tidak merespons.
   - Nilai live Accelerometer (x, y, z), kalibrasi titik nol, dan slider sensitivitas (0.5× s/d 2.0×).
   - Nilai live Magnetometer: kekuatan medan magnet (µT, normal 25-65 µT), azimuth absolut, arah relatif terhadap acuan, dan tombol set ulang arah awal.
   - Toggle auto-pause proximity beserta status live Dekat/Jauh.
   - Daftar lengkap seluruh sensor fisik di HP (`SensorManager.getSensorList(Sensor.TYPE_ALL)`).
7. **100% Offline & Aman**:
   - Tanpa iklan, tanpa pelacak, tanpa koneksi internet, dan tanpa runtime permission berbahaya.

---

## 🛠️ Teknologi yang Digunakan

- **Bahasa**: Kotlin 2.2.10
- **Toolkit UI**: Jetpack Compose (Compose BOM 2026.02.01) + Material 3
- **Rendering Arena**: Compose `Canvas` (skala berbasis sel independen resolusi)
- **State Management**: Kotlin Coroutines & `StateFlow` + `collectAsStateWithLifecycle`
- **Sensor Stream**: Kotlin Coroutines `callbackFlow` dengan `.conflate()` dan unregister otomatis di `awaitClose`
- **Database**: SQLite native via `android.database.sqlite.SQLiteOpenHelper` (Database Versi 2)
- **Navigasi**: Jetpack Navigation Compose
- **Target SDK**: Android 15 (API 35/37) | **Min SDK**: Android 7.0 (API 24)

---

## 🏗️ Arsitektur Aplikasi (MVVM + Repository)

```text
┌────────────────────────────────────────────────────────┐
│                      UI / View                         │
│  (Jetpack Compose Screens, Canvas, Dialogs, Overlays)  │
└───────────────▲────────────────────────▲───────────────┘
                │ StateFlow              │ Events / Input
                │ (collectAsState)       │
┌───────────────┴────────────────────────┴───────────────┐
│                     ViewModel                          │
│  (GameViewModel, HistoryViewModel, SensorViewModel)    │
└───────────────▲────────────────────────▲───────────────┘
                │                        │ Flow (callbackFlow)
                │ suspend fun            │
┌───────────────┴──────────┐   ┌─────────┴───────────────┐
│     GameRepository       │   │    SensorController     │
│ (Dispatchers.IO context) │   │ (Accelerometer,         │
└───────────────▲──────────┘   │  Magnetometer,          │
                │              │  Proximity)             │
┌───────────────┴──────────┐   └─────────────────────────┘
│      GameDbHelper        │
│   (SQLiteOpenHelper v2)  │
└───────────────▲──────────┘
                │
┌───────────────┴──────────┐
│   tiltball.db (SQLite)   │
└──────────────────────────┘
```

---

## 🗄️ Skema Database SQLite (Versi 2)

Database internal `tiltball.db` (Versi 2):

### 1. Tabel `game_results`
```sql
CREATE TABLE game_results (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    level INTEGER NOT NULL,
    score INTEGER NOT NULL,
    duration_ms INTEGER NOT NULL,
    penalty_ms INTEGER NOT NULL,
    completed_at INTEGER NOT NULL
);
CREATE INDEX idx_results_level ON game_results (level);
CREATE INDEX idx_results_score ON game_results (score);
```

### 2. Tabel `level_progress`
```sql
CREATE TABLE level_progress (
    level INTEGER PRIMARY KEY,
    unlocked INTEGER NOT NULL,
    best_score INTEGER NOT NULL DEFAULT 0,
    best_time_ms INTEGER NOT NULL DEFAULT 0
);
```

---

## 📡 Sensor yang Digunakan & Fungsinya

| No | Sensor | Kategori Android | Frekuensi Laporan | Fungsi dalam Game |
|---|---|---|---|---|
| 1 | **Accelerometer** (`TYPE_ACCELEROMETER`) | Motion | `SENSOR_DELAY_GAME` | Menggerakkan bola secara 2D pada sumbu X dan Y berdasarkan kemiringan HP portrait. |
| 2 | **Magnetometer** (`TYPE_MAGNETIC_FIELD`) | Position | `SENSOR_DELAY_UI` | Menghitung sudut azimuth & orientasi relatif untuk membuka Gerbang Kompas saat pemain memutar badan. |
| 3 | **Proximity** (`TYPE_PROXIMITY`) | Position | `SENSOR_DELAY_NORMAL` | Mendeteksi tangan di depan dahi HP untuk memicu auto-pause (opsional, default: MATI). |

> **Catatan Kompatibilitas**: Xiaomi Redmi 12 tidak memiliki sensor Giroskop (*Gyroscope*). Seluruh kalkulasi kemiringan menggunakan vektor gravitasi murni dari *Accelerometer*, dan seluruh kalkulasi arah putaran menggunakan *Magnetometer* + *Accelerometer*. Sensor cahaya (*Light Sensor*) tidak digunakan lagi agar permainan berjalan konsisten di semua varian perangkat.

---

## 📸 Tangkapan Layar (Screenshots)

| Menu Utama | Pilih Level | Gameplay & Gerbang Kompas | Gerbang Terbuka |
|:---:|:---:|:---:|:---:|
| ![Menu](screenshots/menu.png) | ![Level](screenshots/level_select.png) | ![Game](screenshots/game.png) | ![Gate Open](screenshots/gate_open.png) |

| Jeda & Kalibrasi Kompas | Level Selesai | Riwayat & Rekor | Diagnostik Sensor Lengkap |
|:---:|:---:|:---:|:---:|
| ![Pause](screenshots/pause.png) | ![Complete](screenshots/complete.png) | ![History](screenshots/history.png) | ![Sensors](screenshots/sensors.png) |

---

## 🚀 Cara Menjalankan Proyek

1. **Buka di Android Studio**:
   - Pilih menu **File > Open**, arahkan ke folder proyek `TIltBall`.
2. **Sync Gradle**:
   - Tunggu proses Gradle Sync selesai.
3. **PENTING: Reset Database / Uninstall App Lama**:
   - Karena database dinaikkan ke **Versi 2**, jika aplikasi versi sebelumnya sudah terpasang di HP, lakukan *Uninstall* terlebih dahulu atau *Clear Data* aplikasi melalui Pengaturan HP agar skema tabel baru terpasang bersih.
4. **Jalankan Aplikasi**:
   - Sambungkan HP Android (aktifkan USB Debugging) atau pilih Android Emulator.
   - Klik tombol **Run 'app'** (ikon hijau ▶) di toolbar atas.

---

## 📁 Struktur Folder Proyek

```text
com.example.tiltball
 ├── MainActivity.kt                      (Single Activity & bootstrap)
 ├── data
 │    ├── model
 │    │    ├── GameResult.kt              (Model hasil permainan)
 │    │    └── LevelProgress.kt           (Model status level & rekor)
 │    ├── local
 │    │    └── GameDbHelper.kt            (SQLiteOpenHelper v2 & raw queries)
 │    └── repository
 │         └── GameRepository.kt          (Repository pembungkus database I/O)
 ├── sensor
 │    └── SensorController.kt             (Accelerometer, Magnetometer, Proximity + Watchdog)
 ├── game
 │    ├── GameEngine.kt                   (Logika fisika 2D bola & Gerbang Kompas)
 │    ├── LevelData.kt                    (Model data sel, GateData, & grid parser)
 │    ├── Levels.kt                       (Definisi 5 level permainan dengan gerbang)
 │    └── LevelValidator.kt               (BFS pathfinding verifier)
 ├── viewmodel
 │    ├── GameViewModel.kt                (State machine & orientasi relatif kompas)
 │    ├── HistoryViewModel.kt             (Handler database riwayat & progres)
 │    ├── SensorViewModel.kt              (Live stream diagnostik & kalibrasi)
 │    └── ViewModelFactory.kt             (Manual Dependency Injection Factory)
 └── ui
      ├── navigation
      │    └── AppNavGraph.kt             (Rute dan navigasi Compose)
      ├── screens
      │    ├── MainMenuScreen.kt          (Menu utama)
      │    ├── LevelSelectScreen.kt       (Pilihan 5 level)
      │    ├── GameScreen.kt              (Arena permainan & game loop)
      │    ├── LevelCompleteScreen.kt     (Hasil penyelesaian level)
      │    ├── GameOverScreen.kt          (Layar waktu habis)
      │    ├── HistoryScreen.kt           (Daftar riwayat & reset progres)
      │    ├── HowToPlayScreen.kt         (Panduan permainan & gerbang kompas)
      │    ├── AboutScreen.kt             (Informasi app & sensor)
      │    └── SensorScreen.kt            (Diagnostik sensor & kalibrasi)
      ├── components
      │    ├── GameCanvas.kt              (Canvas rendering arena, bola, & gerbang)
      │    ├── HudBar.kt                  (Timer, level, petunjuk arah & progress gerbang)
      │    ├── PauseOverlay.kt            (Overlay jeda & tips kalibrasi kompas)
      │    ├── CountdownOverlay.kt        (Animasi hitung mundur 3-2-1)
      │    ├── ResultCard.kt              (Komponen kartu skor)
      │    ├── EmptyState.kt              (Tampilan data kosong)
      │    └── ConfirmDialog.kt           (Dialog konfirmasi hapus/reset)
      └── theme
           ├── Color.kt                   (Palet warna UI)
           ├── Theme.kt                   (Tema Material 3 Gelap)
           └── Type.kt                    (Tipografi teks)
```

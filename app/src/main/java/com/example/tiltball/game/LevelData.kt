package com.example.tiltball.game

/**
 * Jenis sel dalam grid level.
 */
enum class CellType {
    FLOOR,  // lantai bebas
    WALL,   // dinding permanen
    START,  // posisi awal bola
    FINISH, // titik akhir kemenangan
    HOLE,   // rintangan lubang (penalti + reset start)
    GATE    // gerbang kompas (tertutup = dinding cyan, terbuka = lantai)
}

/**
 * Data untuk Gerbang Kompas.
 * @param targetDegrees Arah putaran badan relatif terhadap arah awal (derajat, e.g. +90, -90, 180)
 * @param toleranceDegrees Batas toleransi sudut (ramah & responsif, default 40 derajat)
 * @param holdMs Durasi penahanan arah untuk membuka gerbang (default 500 ms)
 */
data class GateData(
    val targetDegrees: Float,
    val toleranceDegrees: Float = 40f,
    val holdMs: Long = 500L
)

/**
 * Data satu level: nomor, nama, grid, batas waktu, dan konfigurasi gerbang (opsional).
 */
data class LevelData(
    val number: Int,
    val name: String,
    val grid: Array<Array<CellType>>,   // [baris][kolom]
    val timeLimitMs: Long,
    val startRow: Int,
    val startCol: Int,
    val finishRow: Int,
    val finishCol: Int,
    val gateData: GateData? = null
) {
    override fun equals(other: Any?) = other is LevelData && number == other.number
    override fun hashCode() = number
}

class LevelParseException(msg: String) : Exception(msg)

/**
 * Parser grid: mengubah list 14 string (9 karakter) menjadi LevelData.
 * Legenda: # = dinding, . = lantai, S = start, F = finish, O = lubang, G = gerbang kompas.
 */
fun parseLevel(
    number: Int,
    name: String,
    rawGrid: List<String>,
    timeLimitMs: Long,
    gateData: GateData? = null
): LevelData {
    require(rawGrid.size == 14) { "Level $number harus 14 baris, ditemukan ${rawGrid.size}" }

    var startRow = -1; var startCol = -1
    var finishRow = -1; var finishCol = -1

    val grid = Array(14) { row ->
        val line = rawGrid[row]
        require(line.length == 9) {
            "Level $number baris $row harus 9 karakter, ditemukan ${line.length}"
        }
        Array(9) { col ->
            when (line[col]) {
                '#' -> CellType.WALL
                '.' -> CellType.FLOOR
                'S' -> { startRow = row; startCol = col; CellType.START }
                'F' -> { finishRow = row; finishCol = col; CellType.FINISH }
                'O' -> CellType.HOLE
                'G' -> CellType.GATE
                else -> throw LevelParseException(
                    "Level $number: karakter tidak dikenal '${line[col]}' di [$row,$col]"
                )
            }
        }
    }

    if (startRow == -1) throw LevelParseException("Level $number tidak punya titik S")
    if (finishRow == -1) throw LevelParseException("Level $number tidak punya titik F")

    return LevelData(number, name, grid, timeLimitMs, startRow, startCol, finishRow, finishCol, gateData)
}

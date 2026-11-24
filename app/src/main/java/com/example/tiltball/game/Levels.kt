package com.example.tiltball.game

/**
 * Definisi 5 level permainan.
 * Grid: 14 baris × 9 kolom.
 * Legenda: # = dinding, . = lantai, S = start, F = finish, O = lubang, G = gerbang kompas.
 */
object Levels {

    // ─── Level 1: Arena terbuka, tanpa gerbang (70 detik) ───────────────────
    private val level1Grid = listOf(
        "#########",
        "#S......#",
        "#.......#",
        "#...#...#",
        "#.......#",
        "#.......#",
        "#..#.#..#",
        "#.......#",
        "#.......#",
        "#...#...#",
        "#.......#",
        "#.......#",
        "#......F#",
        "#########"
    )

    // ─── Level 2: Gerbang Kompas Putar Kanan (+90°) (80 detik) ─────────────
    // Gerbang 'G' menutup lorong di baris 9 sebelum finish
    private val level2Grid = listOf(
        "#########",
        "#S......#",
        "#.......#",
        "#.##....#",
        "#....O..#",
        "#.......#",
        "#...##..#",
        "#.O.....#",
        "#.......#",
        "####GG###",
        "#...O...#",
        "#.......#",
        "#......F#",
        "#########"
    )

    // ─── Level 3: Jalur Zig-Zag, Gerbang Putar Kiri (-90°) (90 detik) ───────
    // Gerbang 'G' di baris 7
    private val level3Grid = listOf(
        "#########",
        "#S......#",
        "#.#####.#",
        "#.......#",
        "#.#####.#",
        "#.....O.#",
        "#.#####.#",
        "#GG.....#",
        "#.####..#",
        "#O......#",
        "#.####..#",
        "#.....O.#",
        "#......F#",
        "#########"
    )

    // ─── Level 4: Lorong Sempit, Gerbang Putar Balik (180°) (80 detik) ──────
    // Gerbang 'G' di baris 11 sebelum jalan ke finish
    private val level4Grid = listOf(
        "#########",
        "#S#.....#",
        "#.#.###.#",
        "#...O.#.#",
        "###.#.#.#",
        "#O..#...#",
        "#.###.#.#",
        "#.....#.#",
        "#.###O#.#",
        "#.#...#.#",
        "#.#.###.#",
        "#O..OG..#",
        "#.....#F#",
        "#########"
    )

    // ─── Level 5: Labirin Master, Gerbang Putar Kanan (+90°) (90 detik) ────
    // Gerbang 'G' di baris 11 menutup jalan utama sebelum finish
    private val level5Grid = listOf(
        "#########",
        "#S......#",
        "#.#####.#",
        "#...#...#",
        "###.#.###",
        "#...O...#",
        "###.#####",
        "#.......#",
        "#####.###",
        "#...#...#",
        "###...###",
        "####GG###",
        "#...O..F#",
        "#########"
    )

    val all: List<LevelData> by lazy {
        listOf(
            parseLevel(1, "Arena Terbuka", level1Grid, 70_000L, null),
            parseLevel(2, "Gerbang Kanan", level2Grid, 80_000L, GateData(targetDegrees = 90f)),
            parseLevel(3, "Gerbang Kiri", level3Grid, 90_000L, GateData(targetDegrees = -90f)),
            parseLevel(4, "Putar Balik", level4Grid, 80_000L, GateData(targetDegrees = 180f)),
            parseLevel(5, "Labirin Master", level5Grid, 90_000L, GateData(targetDegrees = 90f))
        )
    }

    fun getLevel(number: Int): LevelData? = all.firstOrNull { it.number == number }
}

package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.engine.TerminalTheme

object LemonThemes {
    val LemonAmberCrt = TerminalTheme(
        id = "amber_crt",
        name = "Lemon Amber CRT",
        description = "Retro 80s phosphor amber glow with warm bronze background",
        background = Color(0xFF140F04),
        foreground = Color(0xFFFFB000),
        promptColor = Color(0xFFFFD54F),
        accentColor = Color(0xFFFF8F00),
        surfaceColor = Color(0xFF221A07),
        cursorColor = Color(0xFFFFCA28),
        windowTitleBar = Color(0xFF2E2209),
        windowBorder = Color(0xFFFFB000),
        isPhosphorAmber = true
    )

    val MatrixGreen = TerminalTheme(
        id = "matrix_green",
        name = "Matrix Green Phosphor",
        description = "Vivid phosphor monochrome terminal aesthetic",
        background = Color(0xFF041007),
        foreground = Color(0xFF00FF66),
        promptColor = Color(0xFF69F0AE),
        accentColor = Color(0xFF00E676),
        surfaceColor = Color(0xFF0A2210),
        cursorColor = Color(0xFF00FF66),
        windowTitleBar = Color(0xFF0F3018),
        windowBorder = Color(0xFF00FF66),
        isPhosphorGreen = true
    )

    val XfceSlate = TerminalTheme(
        id = "xfce_slate",
        name = "XFCE Retro Slate",
        description = "Classic Unix XFCE desktop terminal with steel blue & charcoal tones",
        background = Color(0xFF1A1D20),
        foreground = Color(0xFFECEFF1),
        promptColor = Color(0xFF64B5F6),
        accentColor = Color(0xFF81C784),
        surfaceColor = Color(0xFF262C31),
        cursorColor = Color(0xFF42A5F5),
        windowTitleBar = Color(0xFF2E343A),
        windowBorder = Color(0xFF455A64)
    )

    val CyberpunkSynthwave = TerminalTheme(
        id = "cyberpunk",
        name = "Cyberpunk Synthwave",
        description = "Neon magenta, electric cyan, and deep retro-wave violet",
        background = Color(0xFF120726),
        foreground = Color(0xFFFF529A),
        promptColor = Color(0xFF00E5FF),
        accentColor = Color(0xFFFFD600),
        surfaceColor = Color(0xFF200E40),
        cursorColor = Color(0xFF00E5FF),
        windowTitleBar = Color(0xFF2F155C),
        windowBorder = Color(0xFFFF007F)
    )

    val DraculaVelvet = TerminalTheme(
        id = "dracula",
        name = "Dracula Velvet",
        description = "Modern dark theme with soft purples, greens, and pinks",
        background = Color(0xFF1E1F29),
        foreground = Color(0xFFF8F8F2),
        promptColor = Color(0xFF50FA7B),
        accentColor = Color(0xFFBD93F9),
        surfaceColor = Color(0xFF282A36),
        cursorColor = Color(0xFFFF79C6),
        windowTitleBar = Color(0xFF343746),
        windowBorder = Color(0xFF6272A4)
    )

    val MonokaiPro = TerminalTheme(
        id = "monokai",
        name = "Monokai Pro",
        description = "Vibrant lime, orange, and sky syntax palette on carbon",
        background = Color(0xFF1E2022),
        foreground = Color(0xFFF7F7F0),
        promptColor = Color(0xFFA6E22E),
        accentColor = Color(0xFFFD971F),
        surfaceColor = Color(0xFF2D3134),
        cursorColor = Color(0xFFE6DB74),
        windowTitleBar = Color(0xFF383C40),
        windowBorder = Color(0xFFA6E22E)
    )

    val SolarizedDark = TerminalTheme(
        id = "solarized",
        name = "Solarized Dark",
        description = "Precision optical contrast with cyan and deep ocean blues",
        background = Color(0xFF002B36),
        foreground = Color(0xFF93A1A1),
        promptColor = Color(0xFF2AA198),
        accentColor = Color(0xFF268BD2),
        surfaceColor = Color(0xFF073642),
        cursorColor = Color(0xFF839496),
        windowTitleBar = Color(0xFF0B414F),
        windowBorder = Color(0xFF2AA198)
    )

    val LxqtSteel = TerminalTheme(
        id = "lxqt_steel",
        name = "LXQt Steel Blue",
        description = "Lightweight desktop aesthetic inspired by Openbox & LXQt",
        background = Color(0xFF161B22),
        foreground = Color(0xFFD0D7DE),
        promptColor = Color(0xFF58A6FF),
        accentColor = Color(0xFF3FB950),
        surfaceColor = Color(0xFF21262D),
        cursorColor = Color(0xFF58A6FF),
        windowTitleBar = Color(0xFF30363D),
        windowBorder = Color(0xFF484F58)
    )

    val allThemes = listOf(
        LemonAmberCrt,
        MatrixGreen,
        XfceSlate,
        CyberpunkSynthwave,
        DraculaVelvet,
        MonokaiPro,
        SolarizedDark,
        LxqtSteel
    )

    fun getThemeById(id: String): TerminalTheme {
        return allThemes.find { it.id == id } ?: LemonAmberCrt
    }
}

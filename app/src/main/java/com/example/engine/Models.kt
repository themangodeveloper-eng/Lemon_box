package com.example.engine

import androidx.compose.ui.graphics.Color

enum class OutputType {
    STDOUT,
    STDERR,
    PROMPT,
    COMMAND,
    SUCCESS,
    INFO,
    WARNING,
    ASCII_ART,
    SYSTEM
}

data class TerminalLine(
    val text: String,
    val type: OutputType = OutputType.STDOUT,
    val customColor: Color? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class TerminalTheme(
    val id: String,
    val name: String,
    val description: String,
    val background: Color,
    val foreground: Color,
    val promptColor: Color,
    val accentColor: Color,
    val surfaceColor: Color,
    val cursorColor: Color,
    val windowTitleBar: Color,
    val windowBorder: Color,
    val isPhosphorAmber: Boolean = false,
    val isPhosphorGreen: Boolean = false
)

data class VFile(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    var content: String = "",
    val permissions: String = if (isDirectory) "drwxr-xr-x" else "-rw-r--r--",
    val modifiedTime: Long = System.currentTimeMillis()
)

data class GitCommit(
    val hash: String,
    val message: String,
    val author: String = "lemon-dev <dev@lemonbox.local>",
    val timestamp: Long = System.currentTimeMillis(),
    val branch: String = "main"
)

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val user: String = "lemon",
    val cpuPercent: Float,
    val memoryMb: Float,
    val status: String = "S", // S = Sleeping, R = Running
    val time: String = "00:01"
)

data class SystemResourceSnapshot(
    val cpuPercent: Int,
    val memoryUsedMb: Long,
    val memoryTotalMb: Long,
    val memoryFreeMb: Long,
    val storageFreeMb: Long,
    val uptimeSeconds: Long,
    val processList: List<ProcessInfo>
)

enum class DesktopWindowType(val defaultTitle: String) {
    TERMINAL("lemon_box Terminal (lemon_sh)"),
    PYTHON_STUDIO("Python 3.12 Studio & REPL"),
    GIT_WORKBENCH("Git Project Workbench"),
    DEV_TOOLS("Cross-Platform Dev Studio (JDK / SDK / NDK)"),
    SCRIPTS("Scripting Automator"),
    RESOURCE_MONITOR("lemon_top System & Memory Monitor"),
    FILE_EXPLORER("Lemon Filesystem Explorer"),
    SETTINGS("Desktop & Terminal Appearance")
}

data class WindowState(
    val id: String,
    val type: DesktopWindowType,
    val title: String,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false,
    val zIndex: Float = 0f,
    val workspace: Int = 1
)

enum class PanelPosition {
    TOP,
    BOTTOM
}

enum class DesktopThemeMode {
    XFCE_SLATE,
    LXQT_LIGHT_STEEL,
    LEMON_AMBER_CRT,
    MATRIX_GREEN,
    CYBERPUNK_NEON,
    DRACULA_VELVET
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DesktopWindowType
import com.example.engine.SystemResourceSnapshot
import com.example.engine.TerminalTheme
import com.example.engine.WindowState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DesktopPanel(
    theme: TerminalTheme,
    windows: List<WindowState>,
    activeWindowId: String?,
    activeWorkspace: Int,
    resourceSnapshot: SystemResourceSnapshot,
    onOpenWindow: (DesktopWindowType) -> Unit,
    onFocusWindow: (String) -> Unit,
    onSwitchWorkspace: (Int) -> Unit,
    onToggleQuickTheme: () -> Unit,
    onQuickDropCaches: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    var currentTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .background(theme.surfaceColor.copy(alpha = 0.95f))
            .border(width = 1.dp, color = theme.windowBorder.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Whisker / Lemon Application Menu Button (🍋)
            Box {
                Row(
                    modifier = Modifier
                        .height(28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isMenuOpen) theme.promptColor.copy(alpha = 0.3f) else theme.background)
                        .border(1.dp, theme.promptColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .clickable { isMenuOpen = !isMenuOpen }
                        .padding(horizontal = 8.dp)
                        .testTag("whisker_menu_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🍋", fontSize = 13.sp)
                    Text(
                        text = "lemon",
                        color = theme.promptColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Dropdown Menu for all desktop apps
                DropdownMenu(
                    expanded = isMenuOpen,
                    onDismissRequest = { isMenuOpen = false },
                    modifier = Modifier
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.windowBorder)
                ) {
                    DropdownMenuItem(
                        text = { Text(">_ lemon_box Terminal (lemon_sh)", color = theme.foreground, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.TERMINAL)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🐍 Python 3.12 Studio & REPL", color = theme.accentColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.PYTHON_STUDIO)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⎇ Git Project Workbench", color = theme.promptColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.GIT_WORKBENCH)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⚙ JDK, SDK & NDK Cross-Compiler", color = theme.foreground, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.DEV_TOOLS)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⚡ Scripting Automator", color = theme.promptColor, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.SCRIPTS)
                        }
                    )
                    HorizontalDivider(color = theme.windowBorder.copy(alpha = 0.5f))
                    DropdownMenuItem(
                        text = { Text("📊 lemon_top System & Memory", color = theme.foreground, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.RESOURCE_MONITOR)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("📁 Lemon Filesystem Explorer", color = theme.foreground, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.FILE_EXPLORER)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🎨 Desktop Appearance & Themes", color = theme.foreground, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                        onClick = {
                            isMenuOpen = false
                            onOpenWindow(DesktopWindowType.SETTINGS)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 2. Workspace Switcher [ 1 ] [ 2 ] [ 3 ]
            Row(
                modifier = Modifier
                    .height(26.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(theme.background)
                    .border(0.5.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                    .padding(1.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(1, 2, 3).forEach { ws ->
                    val isCurrent = ws == activeWorkspace
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(22.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isCurrent) theme.accentColor else Color.Transparent)
                            .clickable { onSwitchWorkspace(ws) }
                            .testTag("workspace_btn_$ws"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$ws",
                            color = if (isCurrent) Color.Black else theme.foreground.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 3. Open Windows Taskbar Switcher
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                windows.filter { it.workspace == activeWorkspace }.forEach { win ->
                    val isActive = win.id == activeWindowId && !win.isMinimized
                    Row(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isActive) theme.windowTitleBar else theme.background)
                            .border(
                                width = 1.dp,
                                color = if (isActive) theme.promptColor else theme.windowBorder.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(3.dp)
                            )
                            .clickable { onFocusWindow(win.id) }
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val icon = when (win.type) {
                            DesktopWindowType.TERMINAL -> ">_"
                            DesktopWindowType.PYTHON_STUDIO -> "py"
                            DesktopWindowType.GIT_WORKBENCH -> "git"
                            DesktopWindowType.DEV_TOOLS -> "sdk"
                            DesktopWindowType.SCRIPTS -> "sh"
                            DesktopWindowType.RESOURCE_MONITOR -> "top"
                            DesktopWindowType.FILE_EXPLORER -> "dir"
                            DesktopWindowType.SETTINGS -> "cfg"
                        }
                        Text(
                            text = icon,
                            color = if (isActive) theme.promptColor else theme.foreground.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = win.title.take(12),
                            color = if (isActive) theme.foreground else theme.foreground.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // 4. Modular Status Tray: CPU %, RAM MB, Theme toggle, Quick GC, Clock
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // CPU Gauge
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.background)
                        .border(0.5.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CPU ${resourceSnapshot.cpuPercent}%",
                        color = if (resourceSnapshot.cpuPercent > 75) Color(0xFFFF5252) else theme.accentColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // RAM Footprint Gauge (clickable to drop caches)
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.background)
                        .border(0.5.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .clickable(onClick = onQuickDropCaches)
                        .padding(horizontal = 5.dp)
                        .testTag("panel_ram_gauge"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${resourceSnapshot.memoryUsedMb}M",
                        color = theme.promptColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick Theme Switcher icon
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.background)
                        .border(0.5.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .clickable(onClick = onToggleQuickTheme)
                        .testTag("panel_theme_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎨", fontSize = 10.sp)
                }

                // Clock
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.background)
                        .border(0.5.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentTime.ifEmpty { "09:22" },
                        color = theme.foreground,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

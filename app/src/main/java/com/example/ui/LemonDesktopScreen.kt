package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DesktopWindowType
import com.example.engine.PanelPosition
import com.example.ui.components.CrtScanlineOverlay
import com.example.ui.components.DesktopPanel
import com.example.ui.components.WindowFrame
import com.example.ui.windows.*

data class DesktopIconItem(
    val type: DesktopWindowType,
    val iconEmoji: String,
    val label: String
)

@Composable
fun LemonDesktopScreen(
    viewModel: LemonDesktopViewModel,
    modifier: Modifier = Modifier
) {
    val theme by viewModel.currentTheme.collectAsState()
    val crtEnabled by viewModel.crtScanlinesEnabled.collectAsState()
    val panelPos by viewModel.panelPosition.collectAsState()
    val workspace by viewModel.activeWorkspace.collectAsState()
    val windows by viewModel.windows.collectAsState()
    val activeWindowId by viewModel.activeWindowId.collectAsState()
    val resourceSnapshot by viewModel.resourceSnapshot.collectAsState()
    val scripts by viewModel.scripts.collectAsState()

    val desktopIcons = listOf(
        DesktopIconItem(DesktopWindowType.TERMINAL, ">_", "Terminal"),
        DesktopIconItem(DesktopWindowType.PYTHON_STUDIO, "🐍", "Python 3.12"),
        DesktopIconItem(DesktopWindowType.GIT_WORKBENCH, "⎇", "Git Manager"),
        DesktopIconItem(DesktopWindowType.DEV_TOOLS, "⚙", "SDK / NDK"),
        DesktopIconItem(DesktopWindowType.SCRIPTS, "⚡", "Automator"),
        DesktopIconItem(DesktopWindowType.RESOURCE_MONITOR, "📊", "lemon_top"),
        DesktopIconItem(DesktopWindowType.FILE_EXPLORER, "📁", "Filesystem"),
        DesktopIconItem(DesktopWindowType.SETTINGS, "🎨", "Themes & Setup")
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(theme.background)
        ) {
            // TOP PANEL (if XFCE style)
            if (panelPos == PanelPosition.TOP) {
                DesktopPanel(
                    theme = theme,
                    windows = windows,
                    activeWindowId = activeWindowId,
                    activeWorkspace = workspace,
                    resourceSnapshot = resourceSnapshot,
                    onOpenWindow = { viewModel.openWindow(it) },
                    onFocusWindow = { viewModel.focusWindow(it) },
                    onSwitchWorkspace = { viewModel.switchWorkspace(it) },
                    onToggleQuickTheme = { viewModel.cycleNextTheme() },
                    onQuickDropCaches = { viewModel.dropCaches() }
                )
            }

            // DESKTOP WORKSPACE AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Desktop Background & Icons
                Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    // Desktop branding watermark
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                    ) {
                        Text("🍋", fontSize = 18.sp)
                        Column {
                            Text(
                                text = "lemon_box Desktop Workspace $workspace",
                                color = theme.promptColor.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "XFCE / LXQt Hybrid Architecture | Dev Toolchains Ready",
                                color = theme.foreground.copy(alpha = 0.35f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Desktop Shortcuts Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 75.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(desktopIcons) { iconItem ->
                            DesktopIconView(
                                icon = iconItem,
                                theme = theme,
                                onClick = { viewModel.openWindow(iconItem.type) }
                            )
                        }
                    }
                }

                // Active Windows Layer (filtered by current workspace)
                val visibleWindows = windows.filter { it.workspace == workspace && !it.isMinimized }
                visibleWindows.forEach { win ->
                    val isFocused = win.id == activeWindowId

                    WindowFrame(
                        title = win.title,
                        windowType = win.type,
                        theme = theme,
                        isFocused = isFocused,
                        isMaximized = win.isMaximized,
                        onFocus = { viewModel.focusWindow(win.id) },
                        onMinimize = { viewModel.minimizeWindow(win.id) },
                        onToggleMaximize = { viewModel.toggleMaximizeWindow(win.id) },
                        onClose = { viewModel.closeWindow(win.id) },
                        modifier = if (win.isMaximized) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        }
                    ) {
                        when (win.type) {
                            DesktopWindowType.TERMINAL -> {
                                TerminalWindow(
                                    terminalEngine = viewModel.terminalEngine,
                                    theme = theme,
                                    crtScanlinesEnabled = crtEnabled,
                                    onOpenScripts = { viewModel.openWindow(DesktopWindowType.SCRIPTS) }
                                )
                            }
                            DesktopWindowType.PYTHON_STUDIO -> {
                                PythonStudioWindow(theme = theme)
                            }
                            DesktopWindowType.GIT_WORKBENCH -> {
                                GitWorkbenchWindow(
                                    terminalEngine = viewModel.terminalEngine,
                                    theme = theme
                                )
                            }
                            DesktopWindowType.DEV_TOOLS -> {
                                DevToolsWindow(theme = theme)
                            }
                            DesktopWindowType.SCRIPTS -> {
                                ScriptStudioWindow(
                                    scripts = scripts,
                                    theme = theme,
                                    onRunScriptInTerminal = { viewModel.runScriptInTerminal(it) },
                                    onSaveScript = { viewModel.saveScript(it) },
                                    onDeleteScript = { viewModel.deleteScript(it) },
                                    onToggleFavorite = { viewModel.toggleFavorite(it) }
                                )
                            }
                            DesktopWindowType.RESOURCE_MONITOR -> {
                                ResourceManagerWindow(
                                    resourceSnapshot = resourceSnapshot,
                                    theme = theme,
                                    onDropCaches = { viewModel.dropCaches() },
                                    onKillProcess = { viewModel.killProcess(it) }
                                )
                            }
                            DesktopWindowType.FILE_EXPLORER -> {
                                FileExplorerWindow(
                                    terminalEngine = viewModel.terminalEngine,
                                    theme = theme,
                                    onOpenFileInEditor = { fileName ->
                                        viewModel.openWindow(DesktopWindowType.TERMINAL)
                                        viewModel.terminalEngine.executeCommand("nano $fileName")
                                    }
                                )
                            }
                            DesktopWindowType.SETTINGS -> {
                                SettingsWindow(
                                    currentTheme = theme,
                                    crtScanlinesEnabled = crtEnabled,
                                    panelPosition = panelPos,
                                    onSelectTheme = { viewModel.selectTheme(it) },
                                    onToggleCrtScanlines = { viewModel.toggleCrtScanlines(it) },
                                    onSetPanelPosition = { viewModel.setPanelPosition(it) }
                                )
                            }
                        }
                    }
                }

                // CRT Scanlines across desktop
                CrtScanlineOverlay(enabled = crtEnabled, scanlineAlpha = 0.04f)
            }

            // BOTTOM PANEL (if LXQt style)
            if (panelPos == PanelPosition.BOTTOM) {
                DesktopPanel(
                    theme = theme,
                    windows = windows,
                    activeWindowId = activeWindowId,
                    activeWorkspace = workspace,
                    resourceSnapshot = resourceSnapshot,
                    onOpenWindow = { viewModel.openWindow(it) },
                    onFocusWindow = { viewModel.focusWindow(it) },
                    onSwitchWorkspace = { viewModel.switchWorkspace(it) },
                    onToggleQuickTheme = { viewModel.cycleNextTheme() },
                    onQuickDropCaches = { viewModel.dropCaches() }
                )
            }
        }
    }
}

@Composable
private fun DesktopIconView(
    icon: DesktopIconItem,
    theme: com.example.engine.TerminalTheme,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag("desktop_icon_${icon.type.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(theme.surfaceColor)
                .border(1.dp, theme.windowBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon.iconEmoji,
                fontSize = 18.sp,
                color = theme.promptColor,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = icon.label,
            color = theme.foreground,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

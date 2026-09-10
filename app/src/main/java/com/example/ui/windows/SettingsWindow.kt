package com.example.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PanelPosition
import com.example.engine.TerminalTheme
import com.example.ui.theme.LemonThemes

@Composable
fun SettingsWindow(
    currentTheme: TerminalTheme,
    crtScanlinesEnabled: Boolean,
    panelPosition: PanelPosition,
    onSelectTheme: (TerminalTheme) -> Unit,
    onToggleCrtScanlines: (Boolean) -> Unit,
    onSetPanelPosition: (PanelPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.background)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section 1: Colourful Terminal Themes
        item {
            Text(
                text = "🎨 Colourful Terminal Themes",
                color = currentTheme.promptColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(LemonThemes.allThemes) { theme ->
            val isSelected = theme.id == currentTheme.id
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surfaceColor)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) theme.accentColor else theme.windowBorder.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { onSelectTheme(theme) }
                    .padding(8.dp)
                    .testTag("theme_picker_${theme.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Color dot
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(theme.promptColor)
                            )
                            Text(
                                text = theme.name,
                                color = theme.promptColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = theme.description,
                            color = theme.foreground.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (isSelected) {
                        Text(
                            text = "[ACTIVE]",
                            color = theme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Section 2: CRT Scanline Raster & Visual Effects
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "📺 Retro Visuals & CRT Effects",
                color = currentTheme.promptColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(currentTheme.surfaceColor)
                    .border(1.dp, currentTheme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CRT Horizontal Scanlines",
                        color = currentTheme.foreground,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Simulates authentic 80s/90s phosphor monitor scanlines",
                        color = currentTheme.foreground.copy(alpha = 0.6f),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Switch(
                    checked = crtScanlinesEnabled,
                    onCheckedChange = onToggleCrtScanlines,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = currentTheme.accentColor,
                        checkedTrackColor = currentTheme.windowTitleBar
                    ),
                    modifier = Modifier.testTag("switch_crt_scanlines")
                )
            }
        }

        // Section 3: Modular Panel Position (XFCE Top vs LXQt Bottom)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎛 Modular Desktop Panel Layout",
                color = currentTheme.promptColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PanelPositionCard(
                    title = "XFCE Style (Top)",
                    subtitle = "Taskbar & system tray docked at top of screen",
                    isSelected = panelPosition == PanelPosition.TOP,
                    theme = currentTheme,
                    modifier = Modifier.weight(1f),
                    onClick = { onSetPanelPosition(PanelPosition.TOP) }
                )

                PanelPositionCard(
                    title = "LXQt Style (Bottom)",
                    subtitle = "Traditional taskbar docked at bottom of screen",
                    isSelected = panelPosition == PanelPosition.BOTTOM,
                    theme = currentTheme,
                    modifier = Modifier.weight(1f),
                    onClick = { onSetPanelPosition(PanelPosition.BOTTOM) }
                )
            }
        }

        // Section 4: Comprehensive Keyboard Shortcuts Cheat Sheet
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⌨ Comprehensive Keyboard Shortcuts",
                color = currentTheme.promptColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            val shortcuts = listOf(
                "Ctrl + C" to "SIGINT / Cancel current line or exit Python REPL",
                "Ctrl + L" to "Clear terminal screen buffer",
                "Ctrl + D" to "Send EOF / Close active session",
                "Ctrl + A / E" to "Jump cursor to Beginning / End of prompt",
                "Ctrl + K" to "Kill line after cursor",
                "Ctrl + U" to "Erase line before cursor",
                "Alt + 1..3" to "Switch between Desktop Workspaces",
                "Alt + Tab" to "Cycle through open application windows",
                "F11" to "Toggle Fullscreen Window focus"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(currentTheme.surfaceColor)
                    .border(1.dp, currentTheme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                shortcuts.forEach { (combo, action) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = combo,
                            color = currentTheme.accentColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = action,
                            color = currentTheme.foreground.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PanelPositionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    theme: TerminalTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) theme.windowTitleBar else theme.surfaceColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.accentColor else theme.windowBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                color = if (isSelected) theme.accentColor else theme.foreground,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = theme.foreground.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.example.engine.TerminalTheme

@Composable
fun DeveloperKeyboardBar(
    theme: TerminalTheme,
    onInsertText: (String) -> Unit,
    onCtrlC: () -> Unit,
    onCtrlL: () -> Unit,
    onCtrlD: () -> Unit,
    onHistoryUp: () -> Unit,
    onHistoryDown: () -> Unit,
    onTab: () -> Unit,
    onOpenScripts: () -> Unit,
    modifier: Modifier = Modifier
) {
    var ctrlLatched by remember { mutableStateOf(false) }
    var altLatched by remember { mutableStateOf(false) }

    SurfaceKeyBar(
        theme = theme,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // CTRL modifier key
            KeyButton(
                label = "CTRL",
                isActive = ctrlLatched,
                theme = theme,
                testTag = "key_ctrl",
                onClick = { ctrlLatched = !ctrlLatched }
            )

            // ALT modifier key
            KeyButton(
                label = "ALT",
                isActive = altLatched,
                theme = theme,
                testTag = "key_alt",
                onClick = { altLatched = !altLatched }
            )

            // ESC key
            KeyButton(
                label = "ESC",
                theme = theme,
                testTag = "key_esc",
                onClick = {
                    ctrlLatched = false
                    altLatched = false
                }
            )

            // TAB key
            KeyButton(
                label = "TAB",
                theme = theme,
                testTag = "key_tab",
                onClick = onTab
            )

            // Up / Down history keys
            KeyIconButton(
                icon = {
                    Icon(
                        Icons.Default.KeyboardArrowUp,
                        contentDescription = "Previous Command",
                        tint = theme.foreground,
                        modifier = Modifier.size(16.dp)
                    )
                },
                theme = theme,
                testTag = "key_history_up",
                onClick = onHistoryUp
            )

            KeyIconButton(
                icon = {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Next Command",
                        tint = theme.foreground,
                        modifier = Modifier.size(16.dp)
                    )
                },
                theme = theme,
                testTag = "key_history_down",
                onClick = onHistoryDown
            )

            // Common terminal symbol keys
            val symbols = listOf("|", "~", "/", "-", "_", "$", "&", "<", ">", ";", ":", "\"", "'", "=")
            symbols.forEach { sym ->
                KeyButton(
                    label = sym,
                    theme = theme,
                    testTag = "key_symbol_$sym",
                    onClick = { onInsertText(sym) }
                )
            }

            // Quick Ctrl+C key
            KeyButton(
                label = "^C",
                theme = theme,
                testTag = "key_ctrl_c",
                isAccent = true,
                onClick = {
                    ctrlLatched = false
                    onCtrlC()
                }
            )

            // Quick Ctrl+L key (Clear)
            KeyButton(
                label = "^L",
                theme = theme,
                testTag = "key_ctrl_l",
                onClick = {
                    ctrlLatched = false
                    onCtrlL()
                }
            )

            // Quick Ctrl+D key (EOF / Exit)
            KeyButton(
                label = "^D",
                theme = theme,
                testTag = "key_ctrl_d",
                onClick = {
                    ctrlLatched = false
                    onCtrlD()
                }
            )

            // Scripts launcher shortcut
            KeyButton(
                label = "SCRIPTS",
                theme = theme,
                testTag = "key_scripts",
                isAccent = true,
                onClick = onOpenScripts
            )
        }
    }
}

@Composable
private fun SurfaceKeyBar(
    theme: TerminalTheme,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(theme.surfaceColor.copy(alpha = 0.95f))
            .border(1.dp, theme.windowBorder.copy(alpha = 0.5f))
    ) {
        content()
    }
}

@Composable
private fun KeyButton(
    label: String,
    theme: TerminalTheme,
    testTag: String,
    isActive: Boolean = false,
    isAccent: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = when {
        isActive -> theme.accentColor
        isAccent -> theme.windowTitleBar
        else -> theme.background
    }
    val textColor = when {
        isActive -> Color.Black
        isAccent -> theme.promptColor
        else -> theme.foreground
    }

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 36.dp, minHeight = 32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isActive) theme.accentColor else theme.windowBorder.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun KeyIconButton(
    icon: @Composable () -> Unit,
    theme: TerminalTheme,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp, 32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(theme.background)
            .border(1.dp, theme.windowBorder.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.DesktopWindowType
import com.example.engine.TerminalTheme

@Composable
fun WindowFrame(
    title: String,
    windowType: DesktopWindowType,
    theme: TerminalTheme,
    isFocused: Boolean,
    isMaximized: Boolean,
    onFocus: () -> Unit,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val borderColor = if (isFocused) theme.windowBorder else theme.windowBorder.copy(alpha = 0.35f)
    val titleBarBg = if (isFocused) theme.windowTitleBar else theme.surfaceColor

    val iconText = when (windowType) {
        DesktopWindowType.TERMINAL -> ">_"
        DesktopWindowType.PYTHON_STUDIO -> "py"
        DesktopWindowType.GIT_WORKBENCH -> "git"
        DesktopWindowType.DEV_TOOLS -> "sdk"
        DesktopWindowType.SCRIPTS -> "sh"
        DesktopWindowType.RESOURCE_MONITOR -> "top"
        DesktopWindowType.FILE_EXPLORER -> "dir"
        DesktopWindowType.SETTINGS -> "cfg"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .shadow(if (isFocused) 8.dp else 2.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(theme.background)
            .border(width = if (isFocused) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .clickable(onClick = onFocus)
    ) {
        // XFCE / LXQt Retro Window Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(titleBarBg)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Window Icon badge & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.promptColor.copy(alpha = 0.2f))
                        .border(0.5.dp, theme.promptColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        color = theme.promptColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = title,
                    color = if (isFocused) theme.foreground else theme.foreground.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: XFCE Style Window Controls [_] [□] [✕]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Minimize button
                WindowControlButton(
                    symbol = "_",
                    testTag = "win_btn_minimize",
                    color = theme.foreground.copy(alpha = 0.8f),
                    onClick = onMinimize
                )

                // Maximize / Restore button
                WindowControlButton(
                    symbol = if (isMaximized) "❐" else "□",
                    testTag = "win_btn_maximize",
                    color = theme.foreground.copy(alpha = 0.8f),
                    onClick = onToggleMaximize
                )

                // Close button (XFCE red hover accent)
                WindowControlButton(
                    symbol = "✕",
                    testTag = "win_btn_close",
                    color = Color(0xFFFF5252),
                    onClick = onClose
                )
            }
        }

        // Window Content Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content()
        }
    }
}

@Composable
private fun WindowControlButton(
    symbol: String,
    testTag: String,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

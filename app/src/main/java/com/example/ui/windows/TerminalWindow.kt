package com.example.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.OutputType
import com.example.engine.TerminalEngine
import com.example.engine.TerminalLine
import com.example.engine.TerminalTheme
import com.example.ui.components.CrtScanlineOverlay
import com.example.ui.components.DeveloperKeyboardBar
import kotlinx.coroutines.delay

@Composable
fun TerminalWindow(
    terminalEngine: TerminalEngine,
    theme: TerminalTheme,
    crtScanlinesEnabled: Boolean,
    onOpenScripts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lines by terminalEngine.lines.collectAsState()
    val prompt by terminalEngine.currentPrompt.collectAsState()
    val input by terminalEngine.currentInput.collectAsState()
    val editingFile by terminalEngine.editingFile.collectAsState()

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // Auto-scroll to bottom on new output lines
    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(lines.size - 1)
        }
    }

    // Auto focus text input
    LaunchedEffect(Unit) {
        delay(200)
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    // Blinking cursor state
    var cursorVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            cursorVisible = !cursorVisible
            delay(530)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        if (editingFile != null) {
            // Full-screen Nano / Text Editor overlay
            NanoEditorView(
                fileName = editingFile!!.name,
                initialContent = editingFile!!.content,
                theme = theme,
                onSave = { newContent -> terminalEngine.saveAndCloseEditor(newContent) },
                onCancel = { terminalEngine.cancelEditor() }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Terminal Terminal Output Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clickable {
                            try { focusRequester.requestFocus() } catch (_: Exception) {}
                        }
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("terminal_output_list")
                    ) {
                        items(lines) { line ->
                            TerminalLineRow(line = line, theme = theme)
                        }

                        // Active Prompt & Input Line
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prompt,
                                    color = theme.promptColor,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                BasicTextField(
                                    value = input,
                                    onValueChange = { terminalEngine.setInput(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .focusRequester(focusRequester)
                                        .testTag("terminal_input_field"),
                                    textStyle = TextStyle(
                                        color = theme.foreground,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    cursorBrush = SolidColor(if (cursorVisible) theme.cursorColor else Color.Transparent),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                    keyboardActions = KeyboardActions(onGo = {
                                        terminalEngine.submitInput()
                                    })
                                )
                            }
                        }
                    }

                    // CRT Scanlines visual effect
                    CrtScanlineOverlay(enabled = crtScanlinesEnabled)
                }

                // Developer Accessory Keyboard Bar
                DeveloperKeyboardBar(
                    theme = theme,
                    onInsertText = { char ->
                        terminalEngine.setInput(input + char)
                    },
                    onCtrlC = { terminalEngine.handleCtrlC() },
                    onCtrlL = { terminalEngine.handleCtrlL() },
                    onCtrlD = { terminalEngine.handleCtrlD() },
                    onHistoryUp = { terminalEngine.historyUp() },
                    onHistoryDown = { terminalEngine.historyDown() },
                    onTab = {
                        // Auto-complete simple filenames or commands
                        val trimmed = input.trim()
                        if (trimmed.isNotEmpty()) {
                            val matches = terminalEngine.vfs.keys.map { it.substringAfterLast("/") }
                                .filter { it.startsWith(trimmed) }
                            if (matches.isNotEmpty()) {
                                terminalEngine.setInput(matches.first())
                            }
                        }
                    },
                    onOpenScripts = onOpenScripts
                )
            }
        }
    }
}

@Composable
private fun TerminalLineRow(line: TerminalLine, theme: TerminalTheme) {
    val textColor = when (line.type) {
        OutputType.COMMAND -> theme.promptColor
        OutputType.PROMPT -> theme.accentColor
        OutputType.SUCCESS -> Color(0xFF4CAF50)
        OutputType.STDERR -> Color(0xFFFF5252)
        OutputType.WARNING -> Color(0xFFFFB74D)
        OutputType.INFO -> Color(0xFF64B5F6)
        OutputType.SYSTEM -> theme.foreground.copy(alpha = 0.5f)
        OutputType.ASCII_ART -> theme.promptColor
        OutputType.STDOUT -> theme.foreground
    }

    Text(
        text = line.text,
        color = line.customColor ?: textColor,
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        lineHeight = 16.sp
    )
}

@Composable
fun NanoEditorView(
    fileName: String,
    initialContent: String,
    theme: TerminalTheme,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    var content by remember { mutableStateOf(initialContent) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Nano Title Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.windowTitleBar)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GNU nano 7.2 (lemon_box)",
                color = theme.promptColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "File: $fileName",
                color = theme.foreground,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2E7D32))
                        .clickable { onSave(content) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("nano_save_button")
                ) {
                    Text("Save (^O)", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFC62828))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("nano_exit_button")
                ) {
                    Text("Exit (^X)", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Editor input
        BasicTextField(
            value = content,
            onValueChange = { content = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
                .testTag("nano_text_editor"),
            textStyle = TextStyle(
                color = theme.foreground,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            ),
            cursorBrush = SolidColor(theme.cursorColor)
        )

        // Nano status bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surfaceColor)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("^G Get Help  ^O WriteOut  ^W Where Is  ^K Cut Text", color = theme.foreground.copy(alpha = 0.6f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("${content.lines().size} lines", color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

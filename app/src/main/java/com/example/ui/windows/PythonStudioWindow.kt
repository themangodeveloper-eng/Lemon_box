package com.example.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.PythonInterpreter
import com.example.engine.TerminalTheme

@Composable
fun PythonStudioWindow(
    theme: TerminalTheme,
    modifier: Modifier = Modifier
) {
    val interpreter = remember { PythonInterpreter() }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Script Editor, 1 = Interactive REPL

    var scriptCode by remember {
        mutableStateOf(
            """
# lemon_box Python 3.12 Automation Suite
import math
import sys
import time

print("Python version:", sys.version)
print("Testing arithmetic & standard library:")

for n in range(1, 6):
    cube = n ** 3
    root = math.sqrt(cube)
    print(f"n={n} | cube={cube} | sqrt(cube)={root:.3f}")

print("Python execution completed successfully!")
            """.trimIndent()
        )
    }

    val scriptLogs = remember { mutableStateListOf<String>() }
    val replHistory = remember { mutableStateListOf<Pair<String, String>>() }
    var replInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Tab switcher: [ Script Editor ] [ Interactive REPL ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.windowTitleBar)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabButton(
                label = "📄 Script Editor",
                isSelected = selectedTab == 0,
                theme = theme,
                testTag = "tab_python_editor",
                onClick = { selectedTab = 0 }
            )
            TabButton(
                label = "💬 Interactive REPL (>>>)",
                isSelected = selectedTab == 1,
                theme = theme,
                testTag = "tab_python_repl",
                onClick = { selectedTab = 1 }
            )
        }

        if (selectedTab == 0) {
            // Script Editor Mode
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "main.py (Python 3.12 embedded)",
                        color = theme.promptColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                scriptLogs.clear()
                                scriptLogs.add("[python3 main.py] Spawning interpreter...")
                                val results = interpreter.executeScript(scriptCode)
                                scriptLogs.addAll(results)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("run_python_script_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Run Script", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = { scriptLogs.clear() },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = theme.foreground, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Code Input Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.windowBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    BasicTextField(
                        value = scriptCode,
                        onValueChange = { scriptCode = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("python_code_input"),
                        textStyle = TextStyle(
                            color = theme.foreground,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(theme.cursorColor)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Execution Console Output
                Text(
                    text = "Execution Console Output:",
                    color = theme.accentColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(6.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        if (scriptLogs.isEmpty()) {
                            item {
                                Text(
                                    text = "Ready to execute. Tap 'Run Script' above.",
                                    color = theme.foreground.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        } else {
                            items(scriptLogs) { log ->
                                Text(
                                    text = log,
                                    color = if (log.contains("Error")) Color(0xFFFF5252) else theme.foreground,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Interactive REPL Mode
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.windowBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    item {
                        Text(
                            text = "Python 3.12.3 interactive REPL [lemon_box]\nType arithmetic or statements like 'print(math.sqrt(64))' or 'x = 42'",
                            color = theme.promptColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = theme.windowBorder.copy(alpha = 0.3f)
                        )
                    }

                    items(replHistory) { item ->
                        Text(
                            text = ">>> ${item.first}",
                            color = theme.promptColor,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.second.isNotEmpty()) {
                            Text(
                                text = item.second,
                                color = theme.foreground,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // REPL Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(theme.windowTitleBar)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ">>> ",
                        color = theme.promptColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    BasicTextField(
                        value = replInput,
                        onValueChange = { replInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("python_repl_input"),
                        textStyle = TextStyle(
                            color = theme.foreground,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(theme.cursorColor),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            if (replInput.isNotBlank()) {
                                val out = interpreter.executeReplLine(replInput)
                                replHistory.add(replInput to out)
                                replInput = ""
                            }
                        })
                    )

                    Button(
                        onClick = {
                            if (replInput.isNotBlank()) {
                                val out = interpreter.executeReplLine(replInput)
                                replHistory.add(replInput to out)
                                replInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("Eval", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabButton(
    label: String,
    isSelected: Boolean,
    theme: TerminalTheme,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(if (isSelected) theme.accentColor else theme.background)
            .border(
                width = 1.dp,
                color = if (isSelected) theme.accentColor else theme.windowBorder.copy(alpha = 0.4f),
                shape = RoundedCornerShape(3.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else theme.foreground,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

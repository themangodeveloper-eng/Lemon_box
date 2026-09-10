package com.example.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ScriptEntity
import com.example.engine.TerminalTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScriptStudioWindow(
    scripts: List<ScriptEntity>,
    theme: TerminalTheme,
    onRunScriptInTerminal: (ScriptEntity) -> Unit,
    onSaveScript: (ScriptEntity) -> Unit,
    onDeleteScript: (ScriptEntity) -> Unit,
    onToggleFavorite: (ScriptEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScript by remember { mutableStateOf<ScriptEntity?>(scripts.firstOrNull()) }
    var isCreatingNew by remember { mutableStateOf(false) }

    // Editor fields
    var editorName by remember { mutableStateOf(selectedScript?.name ?: "") }
    var editorType by remember { mutableStateOf(selectedScript?.type ?: "bash") }
    var editorCode by remember { mutableStateOf(selectedScript?.code ?: "") }
    var editorDesc by remember { mutableStateOf(selectedScript?.description ?: "") }

    LaunchedEffect(selectedScript) {
        if (selectedScript != null) {
            editorName = selectedScript!!.name
            editorType = selectedScript!!.type
            editorCode = selectedScript!!.code
            editorDesc = selectedScript!!.description
        }
    }

    LaunchedEffect(scripts) {
        if (selectedScript == null && scripts.isNotEmpty()) {
            selectedScript = scripts.first()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(8.dp)
    ) {
        // Toolbar: Scripts Automator Title & "New Script" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(theme.windowTitleBar)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Scripting Automator",
                color = theme.promptColor,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = {
                    isCreatingNew = true
                    selectedScript = null
                    editorName = "custom_automation.sh"
                    editorType = "bash"
                    editorCode = "# Custom automation script\necho 'Starting automated task...'\n"
                    editorDesc = "Custom automated workflow"
                },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier
                    .height(26.dp)
                    .testTag("new_script_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New", tint = Color.Black, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("New Script", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Split view: Left = Scripts list, Right = Script Editor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left: Scripts List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surfaceColor)
                    .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(scripts) { script ->
                    val isSelected = selectedScript?.id == script.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) theme.windowTitleBar else theme.background)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) theme.promptColor else theme.windowBorder.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(3.dp)
                            )
                            .clickable {
                                isCreatingNew = false
                                selectedScript = script
                            }
                            .padding(6.dp)
                            .testTag("script_item_${script.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = script.name,
                                    color = if (isSelected) theme.promptColor else theme.foreground,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Icon(
                                    if (script.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (script.isFavorite) Color(0xFFFFD700) else theme.foreground.copy(alpha = 0.4f),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onToggleFavorite(script) }
                                )
                            }
                            Text(
                                text = script.description,
                                color = theme.foreground.copy(alpha = 0.6f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Right: Editor and Runner
            Column(
                modifier = Modifier
                    .weight(1.6f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surfaceColor)
                    .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(8.dp)
            ) {
                // Editor header & actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = editorName,
                        onValueChange = { editorName = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(theme.background)
                            .border(0.5.dp, theme.windowBorder, RoundedCornerShape(3.dp))
                            .padding(4.dp),
                        textStyle = TextStyle(color = theme.promptColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                        cursorBrush = SolidColor(theme.cursorColor),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Run in Terminal button
                    Button(
                        onClick = {
                            val target = selectedScript ?: ScriptEntity(
                                name = editorName,
                                type = editorType,
                                code = editorCode,
                                description = editorDesc
                            )
                            onRunScriptInTerminal(target)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("run_script_in_terminal_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("Run in Terminal", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Description field
                BasicTextField(
                    value = editorDesc,
                    onValueChange = { editorDesc = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.background)
                        .border(0.5.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                        .padding(4.dp),
                    textStyle = TextStyle(color = theme.foreground.copy(alpha = 0.8f), fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(theme.cursorColor),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (editorDesc.isEmpty()) {
                            Text("Description...", color = theme.foreground.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Script Code Editor
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.background)
                        .border(0.5.dp, theme.windowBorder, RoundedCornerShape(3.dp))
                        .padding(6.dp)
                ) {
                    BasicTextField(
                        value = editorCode,
                        onValueChange = { editorCode = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("script_code_editor"),
                        textStyle = TextStyle(color = theme.foreground, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp),
                        cursorBrush = SolidColor(theme.cursorColor)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Bottom Save & Delete actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val scriptToSave = (selectedScript ?: ScriptEntity(
                                name = editorName,
                                type = editorType,
                                code = editorCode,
                                description = editorDesc
                            )).copy(
                                name = editorName,
                                type = editorType,
                                code = editorCode,
                                description = editorDesc
                            )
                            onSaveScript(scriptToSave)
                            selectedScript = scriptToSave
                            isCreatingNew = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.windowTitleBar),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("save_script_button")
                    ) {
                        Text("Save Script", color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    if (selectedScript != null) {
                        IconButton(
                            onClick = {
                                onDeleteScript(selectedScript!!)
                                selectedScript = null
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
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
import com.example.engine.TerminalEngine
import com.example.engine.TerminalTheme
import com.example.engine.VFile

@Composable
fun FileExplorerWindow(
    terminalEngine: TerminalEngine,
    theme: TerminalTheme,
    onOpenFileInEditor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var files by remember { mutableStateOf(terminalEngine.vfs.values.toList()) }
    var selectedFile by remember { mutableStateOf<VFile?>(files.firstOrNull()) }
    var newFileName by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }

    fun refreshFiles() {
        files = terminalEngine.vfs.values.toList()
        if (selectedFile != null && !terminalEngine.vfs.containsKey(selectedFile!!.path)) {
            selectedFile = files.firstOrNull()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(8.dp)
    ) {
        // Toolbar: Path breadcrumb & New File button
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
                text = "📁 /home/lemon/dev",
                color = theme.promptColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showNewFileDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = theme.accentColor),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier
                    .height(26.dp)
                    .testTag("btn_new_file")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New", tint = Color.Black, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text("New File", color = Color.Black, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }

        if (showNewFileDialog) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BasicTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.surfaceColor)
                        .border(1.dp, theme.promptColor, RoundedCornerShape(3.dp))
                        .padding(4.dp)
                        .testTag("input_new_filename"),
                    textStyle = TextStyle(color = theme.foreground, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(theme.cursorColor),
                    singleLine = true
                )

                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            terminalEngine.executeCommand("touch $newFileName")
                            refreshFiles()
                            newFileName = ""
                            showNewFileDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Create", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = { showNewFileDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text("Cancel", color = theme.foreground, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Split Layout: Files List on Left, Preview on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left: File items
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surfaceColor)
                    .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(files) { file ->
                    val isSelected = selectedFile?.path == file.path
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) theme.windowTitleBar else theme.background)
                            .border(1.dp, if (isSelected) theme.promptColor else theme.windowBorder.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                            .clickable { selectedFile = file }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (file.isDirectory) theme.promptColor else theme.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = file.name,
                                color = if (isSelected) theme.promptColor else theme.foreground,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "${file.content.length} B",
                            color = theme.foreground.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Right: File Content Preview & Actions
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surfaceColor)
                    .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                if (selectedFile != null) {
                    val file = selectedFile!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(file.name, color = theme.promptColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { onOpenFileInEditor(file.name) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(3.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(2.dp))
                                Text("Edit", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }

                            Button(
                                onClick = {
                                    terminalEngine.executeCommand("rm ${file.name}")
                                    refreshFiles()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(3.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(10.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(file.permissions, color = theme.accentColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(theme.background)
                            .border(0.5.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                            .padding(6.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(file.content.lines()) { line ->
                                Text(line, color = theme.foreground, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No file selected", color = theme.foreground.copy(alpha = 0.4f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

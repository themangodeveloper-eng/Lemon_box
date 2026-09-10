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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.TerminalEngine
import com.example.engine.TerminalTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GitWorkbenchWindow(
    terminalEngine: TerminalEngine,
    theme: TerminalTheme,
    modifier: Modifier = Modifier
) {
    var commitMessage by remember { mutableStateOf("") }
    var actionStatus by remember { mutableStateOf<String?>("Repository initialized on branch 'main'") }

    var branchName by remember { mutableStateOf(terminalEngine.gitBranch) }
    var stagedFiles by remember { mutableStateOf(terminalEngine.gitStaged.toList()) }
    var unstagedFiles by remember { mutableStateOf(terminalEngine.gitUnstaged.toList()) }
    var commits by remember { mutableStateOf(terminalEngine.gitCommits.toList()) }

    fun refreshGitState() {
        branchName = terminalEngine.gitBranch
        stagedFiles = terminalEngine.gitStaged.toList()
        unstagedFiles = terminalEngine.gitUnstaged.toList()
        commits = terminalEngine.gitCommits.toList()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(8.dp)
    ) {
        // Git Toolbar: Branch, Sync, Refresh
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(theme.windowTitleBar)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "⎇ Branch:",
                    color = theme.promptColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.surfaceColor)
                        .border(0.5.dp, theme.accentColor, RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = branchName,
                        color = theme.accentColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = {
                        terminalEngine.executeCommand("git push")
                        actionStatus = "Remote sync: branch '$branchName' pushed successfully."
                        refreshGitState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .height(26.dp)
                        .testTag("git_push_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Push", modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Push", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }

                Button(
                    onClick = {
                        terminalEngine.executeCommand("git status")
                        actionStatus = "Refreshed repository status."
                        refreshGitState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = theme.surfaceColor),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = theme.foreground, modifier = Modifier.size(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Status banner
        if (actionStatus != null) {
            Text(
                text = actionStatus!!,
                color = theme.accentColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // Two Column Split: Staging area & Commit graph
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Left Column: Staged & Unstaged files
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surfaceColor)
                    .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                // Staged Files Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Staged (${stagedFiles.size})",
                        color = Color(0xFF4CAF50),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Stage All",
                        color = theme.promptColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable {
                                terminalEngine.executeCommand("git add .")
                                actionStatus = "Staged all modified files."
                                refreshGitState()
                            }
                            .testTag("git_stage_all_button")
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 70.dp)
                ) {
                    if (stagedFiles.isEmpty()) {
                        item {
                            Text("No staged files", color = theme.foreground.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        items(stagedFiles) { file ->
                            Text("+ $file", color = Color(0xFF4CAF50), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = theme.windowBorder.copy(alpha = 0.3f))

                // Unstaged Files Header
                Text(
                    text = "Modified / Untracked (${unstagedFiles.size})",
                    color = Color(0xFFFFB74D),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (unstagedFiles.isEmpty()) {
                        item {
                            Text("Working tree clean", color = theme.foreground.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        items(unstagedFiles) { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        terminalEngine.executeCommand("git add $file")
                                        refreshGitState()
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("M $file", color = Color(0xFFFFB74D), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                Text("[+]", color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                // Commit Input & Button
                BasicTextField(
                    value = commitMessage,
                    onValueChange = { commitMessage = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(3.dp))
                        .background(theme.background)
                        .border(0.5.dp, theme.windowBorder, RoundedCornerShape(3.dp))
                        .padding(4.dp)
                        .testTag("git_commit_message_input"),
                    textStyle = TextStyle(color = theme.foreground, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                    cursorBrush = SolidColor(theme.cursorColor),
                    singleLine = true,
                    decorationBox = { inner ->
                        if (commitMessage.isEmpty()) {
                            Text("Commit message...", color = theme.foreground.copy(alpha = 0.4f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        inner()
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (commitMessage.isNotBlank()) {
                            terminalEngine.executeCommand("git commit -m \"$commitMessage\"")
                            actionStatus = "Created commit on '$branchName'."
                            commitMessage = ""
                            refreshGitState()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .testTag("git_commit_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Commit", modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Commit to $branchName", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Right Column: Commit History Tree
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(theme.surfaceColor)
                    .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(6.dp)
            ) {
                Text(
                    text = "Commit History Graph",
                    color = theme.promptColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(commits) { commit ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(3.dp))
                                .background(theme.background)
                                .border(0.5.dp, theme.windowBorder.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                .padding(6.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "* ${commit.hash}",
                                        color = theme.accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = SimpleDateFormat("HH:mm", Locale.US).format(Date(commit.timestamp)),
                                        color = theme.foreground.copy(alpha = 0.5f),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = commit.message,
                                    color = theme.foreground,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

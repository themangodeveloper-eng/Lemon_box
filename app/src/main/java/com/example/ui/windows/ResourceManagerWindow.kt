package com.example.ui.windows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
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
import com.example.engine.SystemResourceSnapshot
import com.example.engine.TerminalTheme

@Composable
fun ResourceManagerWindow(
    resourceSnapshot: SystemResourceSnapshot,
    theme: TerminalTheme,
    onDropCaches: () -> Unit,
    onKillProcess: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .padding(8.dp)
    ) {
        // Header & Quick Optimizer button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(theme.windowTitleBar)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Speed, contentDescription = "Performance", tint = theme.promptColor, modifier = Modifier.size(16.dp))
                Text(
                    text = "lemon_top Resource & Memory Manager",
                    color = theme.promptColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    onDropCaches()
                    statusMessage = "Dropped caches & triggered GC: Reclaimed heap."
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                shape = RoundedCornerShape(3.dp),
                modifier = Modifier
                    .height(26.dp)
                    .testTag("btn_drop_caches")
            ) {
                Icon(Icons.Default.CleaningServices, contentDescription = "Clean", modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Clean Memory", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
        }

        if (statusMessage != null) {
            Text(
                text = statusMessage!!,
                color = theme.accentColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Resource Metrics Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ResourceMetricCard(
                title = "CPU LOAD",
                value = "${resourceSnapshot.cpuPercent}%",
                subtitle = "ARM64 8 Cores",
                color = if (resourceSnapshot.cpuPercent > 70) Color(0xFFFF5252) else theme.accentColor,
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            ResourceMetricCard(
                title = "RAM FOOTPRINT",
                value = "${resourceSnapshot.memoryUsedMb} MB",
                subtitle = "${resourceSnapshot.memoryFreeMb} MB Free",
                color = theme.promptColor,
                theme = theme,
                modifier = Modifier.weight(1f)
            )
            ResourceMetricCard(
                title = "SYSTEM UPTIME",
                value = "${resourceSnapshot.uptimeSeconds / 3600}h ${(resourceSnapshot.uptimeSeconds % 3600) / 60}m",
                subtitle = "Load: 0.14, 0.18",
                color = Color(0xFF81C784),
                theme = theme,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Process List Table
        Text(
            text = "Active Processes & Tasks (Linux / Android Subsystem):",
            color = theme.foreground.copy(alpha = 0.8f),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.surfaceColor)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PID", color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.width(35.dp))
            Text("NAME", color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("CPU%", color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp))
            Text("MEM", color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp))
            Text("KILL", color = Color(0xFFFF5252), fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.width(35.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                .background(theme.surfaceColor.copy(alpha = 0.6f))
                .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(resourceSnapshot.processList) { proc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(2.dp))
                        .background(theme.background)
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("%4d".format(proc.pid), color = theme.foreground.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(35.dp))
                    Text(proc.name, color = theme.foreground, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                    Text("%4.1f%%".format(proc.cpuPercent), color = theme.accentColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(45.dp))
                    Text("%4.1fM".format(proc.memoryMb), color = theme.promptColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(45.dp))

                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Kill Process",
                        tint = Color(0xFFFF5252),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                onKillProcess(proc.pid)
                                statusMessage = "Sent SIGTERM to process ${proc.pid} (${proc.name})"
                            }
                            .testTag("kill_proc_${proc.pid}")
                    )
                }
            }
        }
    }
}

@Composable
private fun ResourceMetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    theme: TerminalTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(theme.surfaceColor)
            .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(6.dp)
    ) {
        Column {
            Text(title, color = theme.foreground.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text(value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(subtitle, color = theme.foreground.copy(alpha = 0.5f), fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

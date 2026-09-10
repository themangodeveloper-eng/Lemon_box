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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.engine.CrossPlatformDevTools
import com.example.engine.TerminalTheme

@Composable
fun DevToolsWindow(
    theme: TerminalTheme,
    modifier: Modifier = Modifier
) {
    val devTools = remember { CrossPlatformDevTools() }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = JDK, 1 = Android SDK, 2 = NDK C/C++

    // JDK state
    var javaCode by remember {
        mutableStateOf(
            """
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from lemon_box Java Development Kit!");
        System.out.println("OpenJDK 21 LTS JVM running on Linux aarch64.");
    }
}
            """.trimIndent()
        )
    }

    // NDK state
    var cCode by remember {
        mutableStateOf(
            """
#include <stdio.h>

int main(void) {
    printf("lemon_box NDK ARM64 native binary executed successfully!\n");
    return 0;
}
            """.trimIndent()
        )
    }
    var selectedAbi by remember { mutableStateOf("arm64-v8a") }

    val buildLogs = remember { mutableStateListOf<String>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        // Tab Bar: [ ☕ JDK 21 ] [ 🤖 Android SDK ] [ ⚡ NDK C/C++ ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(theme.windowTitleBar)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabPill(
                label = "☕ OpenJDK 21",
                isSelected = selectedTab == 0,
                theme = theme,
                testTag = "tab_jdk",
                onClick = { selectedTab = 0 }
            )
            TabPill(
                label = "🤖 Android SDK",
                isSelected = selectedTab == 1,
                theme = theme,
                testTag = "tab_sdk",
                onClick = { selectedTab = 1 }
            )
            TabPill(
                label = "⚡ NDK (C/C++)",
                isSelected = selectedTab == 2,
                theme = theme,
                testTag = "tab_ndk",
                onClick = { selectedTab = 2 }
            )
        }

        when (selectedTab) {
            0 -> {
                // JDK TAB
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HelloWorld.java", color = theme.promptColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DevActionButton("javac", Color(0xFF1565C0), "jdk_javac_button") {
                                buildLogs.clear()
                                val res = devTools.compileJava("HelloWorld.java", javaCode)
                                buildLogs.addAll(res.output)
                            }
                            DevActionButton("java", Color(0xFF2E7D32), "jdk_java_button") {
                                val res = devTools.runJava("HelloWorld", javaCode)
                                buildLogs.addAll(res)
                            }
                            DevActionButton("javap -c", theme.surfaceColor, "jdk_javap_button") {
                                buildLogs.clear()
                                buildLogs.addAll(devTools.javapDisassemble("HelloWorld"))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Code Editor
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
                            value = javaCode,
                            onValueChange = { javaCode = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = TextStyle(color = theme.foreground, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp),
                            cursorBrush = SolidColor(theme.cursorColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    BuildOutputConsole(buildLogs, theme)
                }
            }
            1 -> {
                // ANDROID SDK TAB
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Android SDK & Gradle Tools", color = theme.promptColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DevActionButton("gradle assembleDebug", Color(0xFF2E7D32), "sdk_gradle_build") {
                                buildLogs.clear()
                                buildLogs.addAll(devTools.runGradleBuild("assembleDebug"))
                            }
                            DevActionButton("adb devices", theme.surfaceColor, "sdk_adb_devices") {
                                buildLogs.clear()
                                buildLogs.addAll(devTools.adbDevices())
                            }
                            DevActionButton("sdkmanager", theme.surfaceColor, "sdk_sdkmanager") {
                                buildLogs.clear()
                                buildLogs.addAll(devTools.sdkList())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // SDK Info Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SdkCard("Android API", "35 (Vanilla)", theme, Modifier.weight(1f))
                        SdkCard("Build-Tools", "35.0.0", theme, Modifier.weight(1f))
                        SdkCard("NDK Toolchain", "r26d LLVM 17", theme, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Build & Toolchain Output:", color = theme.accentColor, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    BuildOutputConsole(buildLogs, theme, modifier = Modifier.weight(1f))
                }
            }
            2 -> {
                // NDK C/C++ TAB
                Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("native.c (LLVM Clang 17)", color = theme.promptColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DevActionButton("ndk-build", Color(0xFF6A1B9A), "ndk_build_button") {
                                buildLogs.clear()
                                buildLogs.addAll(devTools.ndkBuild())
                            }
                            DevActionButton("clang", Color(0xFF1565C0), "ndk_clang_button") {
                                buildLogs.clear()
                                val res = devTools.compileClang("native.c", "main", cCode)
                                buildLogs.addAll(res.output)
                            }
                            DevActionButton("./main", Color(0xFF2E7D32), "ndk_run_button") {
                                buildLogs.addAll(devTools.runNativeBinary("main", cCode))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ABI Target Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Target ABI:", color = theme.foreground.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        listOf("arm64-v8a", "x86_64", "armeabi-v7a").forEach { abi ->
                            val isSelected = abi == selectedAbi
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isSelected) theme.accentColor else theme.surfaceColor)
                                    .clickable { selectedAbi = abi }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = abi,
                                    color = if (isSelected) Color.Black else theme.foreground,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // C Code Editor
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
                            value = cCode,
                            onValueChange = { cCode = it },
                            modifier = Modifier.fillMaxSize(),
                            textStyle = TextStyle(color = theme.foreground, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 16.sp),
                            cursorBrush = SolidColor(theme.cursorColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    BuildOutputConsole(buildLogs, theme)
                }
            }
        }
    }
}

@Composable
private fun TabPill(
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
            .border(1.dp, if (isSelected) theme.accentColor else theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
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

@Composable
private fun DevActionButton(
    label: String,
    bgColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SdkCard(
    title: String,
    value: String,
    theme: TerminalTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(theme.surfaceColor)
            .border(0.5.dp, theme.windowBorder.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(6.dp)
    ) {
        Column {
            Text(title, color = theme.foreground.copy(alpha = 0.6f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            Text(value, color = theme.promptColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BuildOutputConsole(
    logs: List<String>,
    theme: TerminalTheme,
    modifier: Modifier = Modifier.height(110.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .border(1.dp, theme.windowBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(6.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (logs.isEmpty()) {
                item {
                    Text("Select a toolchain action above to execute.", color = theme.foreground.copy(alpha = 0.4f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            } else {
                items(logs) { log ->
                    val color = when {
                        log.contains("error") || log.contains("FAIL") -> Color(0xFFFF5252)
                        log.contains("SUCCESS") || log.contains("finished in") -> Color(0xFF4CAF50)
                        log.contains("warning") -> Color(0xFFFFB74D)
                        log.startsWith("[") -> theme.accentColor
                        else -> theme.foreground
                    }
                    Text(text = log, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

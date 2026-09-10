package com.example.engine

import android.content.Context
import android.os.Build
import android.os.Process
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class TerminalEngine(
    private val context: Context,
    private val onExecuteAutomation: (suspend (String) -> Unit)? = null
) {
    private val _lines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val lines: StateFlow<List<TerminalLine>> = _lines.asStateFlow()

    private val _currentPrompt = MutableStateFlow("lemon@box:~/dev$ ")
    val currentPrompt: StateFlow<String> = _currentPrompt.asStateFlow()

    private val _currentInput = MutableStateFlow("")
    val currentInput: StateFlow<String> = _currentInput.asStateFlow()

    private val _isReplMode = MutableStateFlow(false)
    val isReplMode: StateFlow<Boolean> = _isReplMode.asStateFlow()

    private val _editingFile = MutableStateFlow<VFile?>(null)
    val editingFile: StateFlow<VFile?> = _editingFile.asStateFlow()

    private val _history = mutableListOf<String>()
    private var historyIndex = -1

    private val pythonInterpreter = PythonInterpreter()
    private val devTools = CrossPlatformDevTools()

    // Environment variables
    private val env = mutableMapOf(
        "USER" to "lemon",
        "HOSTNAME" to "box",
        "HOME" to "/home/lemon",
        "PWD" to "/home/lemon/dev",
        "TERM" to "lemon-256color",
        "SHELL" to "/bin/lemon_sh",
        "JAVA_HOME" to "/opt/jdk-21",
        "ANDROID_SDK_ROOT" to "/opt/android-sdk",
        "ANDROID_NDK_ROOT" to "/opt/android-sdk/ndk/26.3.11579264",
        "PATH" to "/usr/local/bin:/usr/bin:/bin:/opt/jdk-21/bin:/opt/android-sdk/platform-tools"
    )

    private val aliases = mutableMapOf(
        "ll" to "ls -la",
        "cls" to "clear",
        "py" to "python3",
        "lemon" to "lemonfetch",
        "bench" to "python sys_health_bench.py"
    )

    // Virtual File System
    val vfs = mutableMapOf<String, VFile>()

    // Git state
    var gitBranch = "main"
    val gitStaged = mutableSetOf<String>()
    val gitUnstaged = mutableSetOf<String>()
    val gitCommits = mutableListOf<GitCommit>()

    // Simulated Processes
    private val activeProcesses = mutableListOf(
        ProcessInfo(1, "systemd", "root", 0.1f, 12.4f, "S", "04:12"),
        ProcessInfo(42, "lemon_desktop", "lemon", 1.8f, 48.6f, "S", "02:18"),
        ProcessInfo(108, "lemon_sh", "lemon", 0.4f, 18.2f, "R", "00:35"),
        ProcessInfo(144, "python_daemon", "lemon", 0.2f, 22.0f, "S", "00:15"),
        ProcessInfo(256, "res_monitor", "lemon", 0.5f, 14.1f, "R", "00:08")
    )

    init {
        initVirtualFileSystem()
        initGitRepo()
        printWelcomeBanner()
    }

    private fun initVirtualFileSystem() {
        vfs["/home/lemon/dev/HelloWorld.java"] = VFile(
            name = "HelloWorld.java",
            path = "/home/lemon/dev/HelloWorld.java",
            isDirectory = false,
            content = """
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from lemon_box Java Development Kit!");
        System.out.println("LTS Java 21 Bytecode Virtual Machine ready.");
    }
}
            """.trimIndent()
        )

        vfs["/home/lemon/dev/script.py"] = VFile(
            name = "script.py",
            path = "/home/lemon/dev/script.py",
            isDirectory = false,
            content = """
# lemon_box Python automation script
import math
import sys

print("Initializing cross-platform dev pipeline...")
print(f"Python Platform: {sys.platform}")
for i in range(1, 4):
    print(f"Task step {i}: sqrt({i * 100}) = {math.sqrt(i * 100):.2f}")
print("lemon_box dev suite initialized.")
            """.trimIndent()
        )

        vfs["/home/lemon/dev/native.c"] = VFile(
            name = "native.c",
            path = "/home/lemon/dev/native.c",
            isDirectory = false,
            content = """
#include <stdio.h>

int main(void) {
    printf("lemon_box Native NDK arm64 C runtime executed successfully!\n");
    return 0;
}
            """.trimIndent()
        )

        vfs["/home/lemon/dev/build.sh"] = VFile(
            name = "build.sh",
            path = "/home/lemon/dev/build.sh",
            isDirectory = false,
            permissions = "-rwxr-xr-x",
            content = """
#!/bin/bash
echo "[lemon_box] Running complete dev suite build..."
javac HelloWorld.java
java HelloWorld
python script.py
ndk-build
echo "[lemon_box] All cross-platform targets built successfully."
            """.trimIndent()
        )

        vfs["/home/lemon/dev/README.md"] = VFile(
            name = "README.md",
            path = "/home/lemon/dev/README.md",
            isDirectory = false,
            content = """
# lemon_box Linux Terminal & Retro Desktop
Built with:
- XFCE / LXQt modular panels & desktop manager
- Built-in Git, Python 3.12, OpenJDK 21, Android SDK, and NDK
- Colorful phosphor CRT themes (Amber, Green, Synthwave, Slate)
- Custom scripting automation & comprehensive keyboard shortcuts
            """.trimIndent()
        )
    }

    private fun initGitRepo() {
        gitCommits.add(
            GitCommit(
                hash = "a7b3c91",
                message = "Initial commit: Add lemon_box core workspace",
                timestamp = System.currentTimeMillis() - 7200000
            )
        )
        gitCommits.add(
            GitCommit(
                hash = "f4e19d2",
                message = "Add JDK and Python cross-platform dev scripts",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
        gitUnstaged.add("HelloWorld.java")
        gitUnstaged.add("script.py")
    }

    private fun printWelcomeBanner() {
        val banner = listOf(
            TerminalLine("  🍋 lemon_box Linux Terminal Emulator v2.4.1", OutputType.SUCCESS),
            TerminalLine("  OS: Lemon Linux 6.6-rt | Desktop: XFCE/LXQt Hybrid | Arch: aarch64", OutputType.INFO),
            TerminalLine("  Integrated Dev: [Git] [Python 3.12] [JDK 21] [Android SDK] [NDK r26d]", OutputType.PROMPT),
            TerminalLine("  Type 'help' for commands, 'lemonfetch' for system specs, 'python' for REPL.", OutputType.SYSTEM),
            TerminalLine("----------------------------------------------------------------------", OutputType.SYSTEM)
        )
        _lines.value = banner
    }

    fun setInput(input: String) {
        _currentInput.value = input
    }

    fun submitInput(input: String = _currentInput.value) {
        val cmd = input.trim()
        _currentInput.value = ""

        if (_isReplMode.value) {
            handleReplInput(input)
            return
        }

        if (cmd.isNotEmpty()) {
            _history.add(cmd)
            historyIndex = _history.size
        }

        // Print command with prompt
        appendLine("${_currentPrompt.value}$cmd", OutputType.COMMAND)

        if (cmd.isEmpty()) return

        executeCommand(cmd)
    }

    private fun handleReplInput(input: String) {
        appendLine(">>> $input", OutputType.COMMAND)
        if (input.trim() == "exit()" || input.trim() == "quit()") {
            _isReplMode.value = false
            _currentPrompt.value = "lemon@box:~/dev$ "
            appendLine("Exited Python REPL.", OutputType.INFO)
            return
        }
        val res = pythonInterpreter.executeReplLine(input)
        if (res.isNotEmpty()) {
            res.lines().forEach { appendLine(it, OutputType.STDOUT) }
        }
    }

    fun executeCommand(rawCommand: String) {
        // Alias expansion
        var cmd = rawCommand
        val firstWord = cmd.split(" ").firstOrNull() ?: ""
        if (aliases.containsKey(firstWord)) {
            cmd = cmd.replaceFirst(firstWord, aliases[firstWord]!!)
        }

        // Script runner shortcut: ./script.sh or ./main
        if (cmd.startsWith("./")) {
            val executable = cmd.removePrefix("./")
            handleExecution(executable)
            return
        }

        val parts = splitCommandLine(cmd)
        if (parts.isEmpty()) return

        val program = parts[0]
        val args = parts.drop(1)

        when (program) {
            "help" -> printHelp()
            "clear" -> _lines.value = emptyList()
            "lemonfetch", "neofetch" -> printLemonFetch()
            "ls" -> handleLs(args)
            "pwd" -> appendLine(env["PWD"] ?: "/home/lemon/dev", OutputType.STDOUT)
            "cd" -> handleCd(args)
            "cat" -> handleCat(args)
            "touch" -> handleTouch(args)
            "mkdir" -> handleMkdir(args)
            "rm" -> handleRm(args)
            "echo" -> handleEcho(args)
            "nano", "edit" -> handleNano(args)
            "grep" -> handleGrep(args)
            "wc" -> handleWc(args)
            "uname" -> handleUname(args)
            "whoami" -> appendLine(env["USER"] ?: "lemon", OutputType.STDOUT)
            "date" -> appendLine(SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(Date()), OutputType.STDOUT)
            "history" -> handleHistory()
            "env" -> handleEnv()
            "export" -> handleExport(args)
            "alias" -> handleAlias(args)
            "top" -> handleTop()
            "ps" -> handlePs()
            "free" -> handleFree()
            "kill" -> handleKill(args)
            "curl" -> handleCurl(args)
            "chmod" -> handleChmod(args)

            // Cross-Platform Dev Suites
            "git" -> handleGit(args)
            "python", "python3" -> handlePython(args)
            "java" -> handleJava(args)
            "javac" -> handleJavac(args)
            "javap" -> handleJavap(args)
            "jar" -> handleJar(args)
            "sdkmanager" -> handleSdkmanager(args)
            "adb" -> handleAdb(args)
            "gradle", "./gradlew" -> handleGradle(args)
            "aapt" -> handleAapt(args)
            "ndk-build" -> handleNdkBuild()
            "clang", "gcc" -> handleClang(args)

            // Memory footprint & cache management
            "drop_caches", "optimize_ram" -> handleDropCaches()

            else -> {
                appendLine("lemon_sh: command not found: $program. Type 'help' for built-in dev tools and commands.", OutputType.STDERR)
            }
        }
    }

    private fun handleExecution(executable: String) {
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val fullPath = if (executable.startsWith("/")) executable else "$cwd/$executable"
        val file = vfs[fullPath]

        if (file == null) {
            appendLine("lemon_sh: ./$executable: No such file or directory", OutputType.STDERR)
            return
        }

        if (executable.endsWith(".sh")) {
            appendLine("[exec] Executing shell script: $executable", OutputType.INFO)
            file.content.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                    executeCommand(trimmed)
                }
            }
        } else if (executable.endsWith(".py")) {
            handlePython(listOf(executable))
        } else {
            // Executing native binary or compiled target
            val result = devTools.runNativeBinary(executable, file.content)
            result.forEach { appendLine(it, OutputType.STDOUT) }
        }
    }

    private fun handleGit(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("usage: git [--version] [--help] <command> [<args>]", OutputType.STDOUT)
            appendLine("Commands: status, add, commit, log, branch, checkout, diff, clone, push, pull, remote, init", OutputType.INFO)
            return
        }
        when (args[0]) {
            "status" -> {
                appendLine("On branch $gitBranch", OutputType.INFO)
                if (gitStaged.isEmpty() && gitUnstaged.isEmpty()) {
                    appendLine("nothing to commit, working tree clean", OutputType.SUCCESS)
                } else {
                    if (gitStaged.isNotEmpty()) {
                        appendLine("Changes to be committed (use \"git restore --staged <file>...\" to unstage):", OutputType.INFO)
                        gitStaged.forEach { appendLine("  new file:   $it", OutputType.SUCCESS) }
                    }
                    if (gitUnstaged.isNotEmpty()) {
                        appendLine("Untracked / unstaged files (use \"git add <file>...\" to include):", OutputType.WARNING)
                        gitUnstaged.forEach { appendLine("  modified:   $it", OutputType.STDERR) }
                    }
                }
            }
            "add" -> {
                if (args.size < 2) {
                    appendLine("Nothing specified, nothing added.", OutputType.WARNING)
                    return
                }
                val target = args[1]
                if (target == "." || target == "-A") {
                    gitStaged.addAll(gitUnstaged)
                    gitUnstaged.clear()
                    appendLine("Staged all modified files.", OutputType.SUCCESS)
                } else {
                    if (gitUnstaged.contains(target)) {
                        gitUnstaged.remove(target)
                        gitStaged.add(target)
                        appendLine("Staged '$target'", OutputType.SUCCESS)
                    } else {
                        gitStaged.add(target)
                        appendLine("Staged '$target'", OutputType.SUCCESS)
                    }
                }
            }
            "commit" -> {
                if (gitStaged.isEmpty()) {
                    appendLine("no changes added to commit (use \"git add\")", OutputType.WARNING)
                    return
                }
                val mIndex = args.indexOf("-m")
                val msg = if (mIndex != -1 && mIndex + 1 < args.size) {
                    args.subList(mIndex + 1, args.size).joinToString(" ").replace("\"", "")
                } else {
                    "Update files via lemon_box"
                }
                val newHash = "%07x".format((Math.random() * 0xFFFFFFF).toLong())
                gitCommits.add(0, GitCommit(hash = newHash, message = msg, branch = gitBranch))
                appendLine("[$gitBranch $newHash] $msg", OutputType.SUCCESS)
                appendLine(" ${gitStaged.size} files changed, ${gitStaged.size * 14} insertions(+)", OutputType.STDOUT)
                gitStaged.clear()
            }
            "log" -> {
                appendLine("=== Git Commit History ===", OutputType.INFO)
                gitCommits.forEachIndexed { index, commit ->
                    val headBadge = if (index == 0) " (HEAD -> $gitBranch)" else ""
                    appendLine("* commit ${commit.hash}$headBadge", OutputType.PROMPT)
                    appendLine("| Author: ${commit.author}", OutputType.STDOUT)
                    appendLine("| Date:   ${SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US).format(Date(commit.timestamp))}", OutputType.STDOUT)
                    appendLine("|", OutputType.STDOUT)
                    appendLine("|     ${commit.message}", OutputType.SUCCESS)
                    appendLine("|", OutputType.STDOUT)
                }
            }
            "branch" -> {
                if (args.size > 1 && !args[1].startsWith("-")) {
                    gitBranch = args[1]
                    appendLine("Created and switched to branch '$gitBranch'", OutputType.SUCCESS)
                } else {
                    appendLine("* $gitBranch", OutputType.SUCCESS)
                    appendLine("  dev-feature-arm64", OutputType.STDOUT)
                    appendLine("  release-v2.4", OutputType.STDOUT)
                }
            }
            "checkout" -> {
                if (args.size > 1) {
                    val branchName = if (args[1] == "-b" && args.size > 2) args[2] else args[1]
                    gitBranch = branchName
                    appendLine("Switched to branch '$gitBranch'", OutputType.SUCCESS)
                } else {
                    appendLine("git checkout: please specify a branch name", OutputType.WARNING)
                }
            }
            "diff" -> {
                appendLine("diff --git a/HelloWorld.java b/HelloWorld.java", OutputType.INFO)
                appendLine("--- a/HelloWorld.java", OutputType.STDERR)
                appendLine("+++ b/HelloWorld.java", OutputType.SUCCESS)
                appendLine("@@ -1,5 +1,6 @@", OutputType.INFO)
                appendLine(" public class HelloWorld {", OutputType.STDOUT)
                appendLine("+    // lemon_box modern cross-platform integration", OutputType.SUCCESS)
                appendLine("     public static void main(String[] args) {", OutputType.STDOUT)
            }
            "clone" -> {
                val url = if (args.size > 1) args[1] else "https://github.com/lemon-box/core-tools.git"
                appendLine("Cloning into 'lemon-tools' from $url...", OutputType.INFO)
                appendLine("remote: Enumerating objects: 48, done.", OutputType.STDOUT)
                appendLine("remote: Compressing objects: 100% (36/36), done.", OutputType.STDOUT)
                appendLine("Receiving objects: 100% (48/48), 124.50 KiB | 2.10 MiB/s, done.", OutputType.STDOUT)
                appendLine("Resolving deltas: 100% (14/14), done.", OutputType.SUCCESS)
                vfs["/home/lemon/dev/lemon-tools/main.py"] = VFile("main.py", "/home/lemon/dev/lemon-tools/main.py", false, "print('Lemon Tools Loaded')")
            }
            "push" -> {
                appendLine("Enumerating objects: 5, done.", OutputType.INFO)
                appendLine("Writing objects: 100% (3/3), 412 bytes | 412.00 KiB/s, done.", OutputType.STDOUT)
                appendLine("To https://github.com/lemon-box/dev-workspace.git", OutputType.STDOUT)
                appendLine("   a7b3c91..${gitCommits.firstOrNull()?.hash ?: "f4e19d2"}  $gitBranch -> $gitBranch", OutputType.SUCCESS)
            }
            "pull" -> {
                appendLine("Already up to date on branch $gitBranch.", OutputType.SUCCESS)
            }
            "init" -> {
                appendLine("Initialized empty Git repository in ${env["PWD"]}/.git/", OutputType.SUCCESS)
            }
            else -> {
                appendLine("git: '${args[0]}' is not a git command. See 'git --help'.", OutputType.STDERR)
            }
        }
    }

    private fun handlePython(args: List<String>) {
        if (args.isEmpty()) {
            // Enter Interactive REPL
            _isReplMode.value = true
            _currentPrompt.value = ">>> "
            appendLine("Python 3.12.3 (lemon_box embedded Linux ARM64, Sep 10 2026)", OutputType.SUCCESS)
            appendLine("Type \"help()\", \"copyright\", \"credits\" or \"exit()\" to leave.", OutputType.INFO)
            return
        }

        val target = args[0]
        if (target == "-c" && args.size > 1) {
            val code = args.subList(1, args.size).joinToString(" ")
            val lines = pythonInterpreter.executeScript(code)
            lines.forEach { appendLine(it, OutputType.STDOUT) }
            return
        }

        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val fullPath = if (target.startsWith("/")) target else "$cwd/$target"
        val file = vfs[fullPath]
        if (file == null) {
            appendLine("python: can't open file '$target': [Errno 2] No such file or directory", OutputType.STDERR)
            return
        }

        appendLine("[python3] Executing script: $target", OutputType.INFO)
        val result = pythonInterpreter.executeScript(file.content)
        result.forEach { appendLine(it, OutputType.STDOUT) }
    }

    private fun handleJavac(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: javac <options> <source files>", OutputType.WARNING)
            return
        }
        val target = args[0]
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val fullPath = if (target.startsWith("/")) target else "$cwd/$target"
        val file = vfs[fullPath]

        if (file == null) {
            appendLine("javac: file not found: $target", OutputType.STDERR)
            return
        }

        val res = devTools.compileJava(target, file.content)
        res.output.forEach { appendLine(it, if (res.success) OutputType.SUCCESS else OutputType.STDERR) }
        if (res.success && res.artifactName != null) {
            val classPath = "$cwd/${res.artifactName}"
            vfs[classPath] = VFile(res.artifactName, classPath, false, "CAFEBABE 00000041 (Compiled Java Bytecode)")
        }
    }

    private fun handleJava(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("Usage: java [options] <mainclass> [args...]", OutputType.WARNING)
            return
        }
        if (args[0] == "-version" || args[0] == "--version") {
            devTools.javaVersion().forEach { appendLine(it, OutputType.INFO) }
            return
        }
        val className = args[0]
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val javaFile = vfs["$cwd/$className.java"] ?: vfs["$cwd/$className"]
        val res = devTools.runJava(className, javaFile?.content)
        res.forEach { appendLine(it, OutputType.STDOUT) }
    }

    private fun handleJavap(args: List<String>) {
        val target = if (args.isNotEmpty()) args[0] else "HelloWorld"
        val res = devTools.javapDisassemble(target)
        res.forEach { appendLine(it, OutputType.STDOUT) }
    }

    private fun handleJar(args: List<String>) {
        appendLine("Added manifest", OutputType.INFO)
        appendLine("adding: HelloWorld.class(in = 840) (out= 512)(deflated 39%)", OutputType.STDOUT)
        appendLine("Generated archive: app.jar [1.2 KB]", OutputType.SUCCESS)
    }

    private fun handleSdkmanager(args: List<String>) {
        devTools.sdkList().forEach { appendLine(it, OutputType.STDOUT) }
    }

    private fun handleAdb(args: List<String>) {
        if (args.isNotEmpty() && args[0] == "devices") {
            devTools.adbDevices().forEach { appendLine(it, OutputType.STDOUT) }
        } else {
            appendLine("Android Debug Bridge version 1.0.41", OutputType.INFO)
            appendLine("lemon_box ADB daemon running on port 5037", OutputType.SUCCESS)
        }
    }

    private fun handleGradle(args: List<String>) {
        val task = if (args.isNotEmpty()) args.joinToString(" ") else "assembleDebug"
        appendLine("./gradlew $task", OutputType.INFO)
        val res = devTools.runGradleBuild(task)
        res.forEach { appendLine(it, OutputType.STDOUT) }
    }

    private fun handleAapt(args: List<String>) {
        appendLine("package: name='com.aistudio.lemonbox.terminal' versionCode='1' versionName='1.0'", OutputType.INFO)
        appendLine("sdkVersion:'24' targetSdkVersion:'36'", OutputType.STDOUT)
        appendLine("uses-permission: name='android.permission.INTERNET'", OutputType.STDOUT)
        appendLine("application-label:'lemon_box'", OutputType.SUCCESS)
    }

    private fun handleNdkBuild() {
        devTools.ndkBuild().forEach { appendLine(it, OutputType.STDOUT) }
    }

    private fun handleClang(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("clang: error: no input files", OutputType.STDERR)
            return
        }
        val target = args[0]
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val fullPath = if (target.startsWith("/")) target else "$cwd/$target"
        val file = vfs[fullPath]
        if (file == null) {
            appendLine("clang: error: cannot find '$target'", OutputType.STDERR)
            return
        }
        val oIndex = args.indexOf("-o")
        val outName = if (oIndex != -1 && oIndex + 1 < args.size) args[oIndex + 1] else "a.out"
        val res = devTools.compileClang(target, outName, file.content)
        res.output.forEach { appendLine(it, if (res.success) OutputType.SUCCESS else OutputType.STDERR) }
        if (res.success && res.artifactName != null) {
            val binPath = "$cwd/${res.artifactName}"
            vfs[binPath] = VFile(res.artifactName, binPath, false, file.content, "-rwxr-xr-x")
        }
    }

    private fun handleDropCaches() {
        appendLine("[resource_mgr] Flushing unused memory caches and GC...", OutputType.INFO)
        System.gc()
        // Trim scrollback if larger than 300 lines
        if (_lines.value.size > 300) {
            _lines.value = _lines.value.takeLast(150)
            appendLine("[resource_mgr] Trimmed terminal scrollback buffer by 50%.", OutputType.INFO)
        }
        appendLine("[resource_mgr] Memory footprint optimized: 42.8 MB reclaimed. Ready.", OutputType.SUCCESS)
    }

    private fun printHelp() {
        val help = listOf(
            "=================== lemon_box Built-in Commands ===================",
            "  DEVELOPMENT TOOLCHAINS:",
            "    git <cmd>          : Full Git repo manager (status, add, commit, log, branch, diff, clone, push)",
            "    python / python3   : Python 3.12 runtime (run interactive REPL or 'python script.py')",
            "    javac / java       : Java 21 JDK compiler and JVM bytecode executor",
            "    javap -c <class>   : Disassemble Java class bytecode",
            "    sdkmanager / adb   : Android SDK package manager & bridge tools",
            "    gradle <task>      : Android build engine (e.g. 'gradle assembleDebug')",
            "    ndk-build / clang  : Native C/C++ cross-compilers for ARM64 & x86_64",
            "",
            "  SYSTEM & RESOURCE MANAGEMENT:",
            "    lemonfetch         : Retro ASCII system & desktop specs banner",
            "    top / ps           : Live process table & memory consumption",
            "    free -m            : Linux memory usage breakdown (Used/Free/Buff/Cache)",
            "    drop_caches        : Clean memory footprint & purge old buffers",
            "    kill <pid>         : Terminate process by PID",
            "",
            "  LINUX UTILITIES & AUTOMATION:",
            "    ls [-la] / cd / pwd: Directory navigation & file listing",
            "    cat / touch / rm   : File inspect, create, remove",
            "    nano / edit <file> : Open retro full-screen text editor",
            "    echo / grep / wc   : Text manipulation & pipeline tools",
            "    env / export / alias: Shell environment configuration",
            "    history / clear    : Command history & display controls",
            "    ./<script.sh>      : Execute custom shell or python scripts",
            "==================================================================="
        )
        help.forEach { appendLine(it, OutputType.INFO) }
    }

    private fun printLemonFetch() {
        val mem = getResourceSnapshot()
        val ascii = listOf(
            "        .----.          lemon_box @ linux-android",
            "      .'  ..  '.        -------------------------",
            "     /   /  \\   \\       OS: LemonOS Linux 6.6-rt (XFCE / LXQt Hybrid)",
            "    ;   ; 🍋 ;   ;      Host: Android API ${Build.VERSION.SDK_INT} (${Build.MODEL})",
            "    \\   \\  /   /        Kernel: Linux 6.6.21-android-arm64",
            "     '.  ''  .'         Uptime: ${mem.uptimeSeconds / 3600}h ${(mem.uptimeSeconds % 3600) / 60}m",
            "       '----'           Shell: lemon_sh 2.4.1",
            "                        Desktop: XFCE 4.18 / LXQt Modular Panel",
            "                        Terminal: lemon_box CRT (amber/green phosphor)",
            "                        Dev Kits: Git, Python 3.12, JDK 21, SDK 35, NDK r26d",
            "                        Memory: ${mem.memoryUsedMb}MB / ${mem.memoryTotalMb}MB (${(mem.memoryUsedMb * 100 / mem.memoryTotalMb)}%)",
            "                        CPU: ${mem.cpuPercent}% | Arch: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"}"
        )
        ascii.forEach { appendLine(it, OutputType.ASCII_ART) }
    }

    private fun handleLs(args: List<String>) {
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val showAll = args.contains("-a") || args.contains("-la") || args.contains("-al")
        val longFormat = args.contains("-l") || args.contains("-la") || args.contains("-al")

        val filesInDir = vfs.values.filter { it.path.startsWith(cwd) && it.path != cwd }

        if (longFormat) {
            appendLine("total ${filesInDir.size * 4}", OutputType.INFO)
            if (showAll) {
                appendLine("drwxr-xr-x 4 lemon lemon 4096 Sep 10 09:20 .", OutputType.STDOUT)
                appendLine("drwxr-xr-x 8 lemon lemon 4096 Sep 10 09:18 ..", OutputType.STDOUT)
                appendLine("drwxr-xr-x 7 lemon lemon 4096 Sep 10 09:20 .git", OutputType.PROMPT)
            }
            filesInDir.forEach { file ->
                val size = file.content.length.coerceAtLeast(124)
                val typeColor = when {
                    file.isDirectory -> OutputType.PROMPT
                    file.permissions.contains("x") -> OutputType.SUCCESS
                    file.name.endsWith(".py") -> OutputType.WARNING
                    file.name.endsWith(".java") -> OutputType.INFO
                    else -> OutputType.STDOUT
                }
                appendLine("${file.permissions} 1 lemon lemon $size Sep 10 09:22 ${file.name}", typeColor)
            }
        } else {
            val names = filesInDir.map { it.name }.toMutableList()
            if (showAll) {
                names.add(0, ".git")
                names.add(0, "..")
                names.add(0, ".")
            }
            appendLine(names.joinToString("  "), OutputType.STDOUT)
        }
    }

    private fun handleCd(args: List<String>) {
        val target = if (args.isNotEmpty()) args[0] else "/home/lemon"
        when (target) {
            "~", "" -> {
                env["PWD"] = "/home/lemon"
                _currentPrompt.value = "lemon@box:~$ "
            }
            "dev", "~/dev", "/home/lemon/dev" -> {
                env["PWD"] = "/home/lemon/dev"
                _currentPrompt.value = "lemon@box:~/dev$ "
            }
            ".." -> {
                val current = env["PWD"] ?: "/home/lemon/dev"
                if (current == "/home/lemon/dev") {
                    env["PWD"] = "/home/lemon"
                    _currentPrompt.value = "lemon@box:~$ "
                } else if (current == "/home/lemon") {
                    env["PWD"] = "/"
                    _currentPrompt.value = "lemon@box:/$ "
                }
            }
            else -> {
                appendLine("cd: no such file or directory: $target", OutputType.STDERR)
            }
        }
    }

    private fun handleCat(args: List<String>) {
        if (args.isEmpty()) return
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val fullPath = if (args[0].startsWith("/")) args[0] else "$cwd/${args[0]}"
        val file = vfs[fullPath]
        if (file == null) {
            appendLine("cat: ${args[0]}: No such file or directory", OutputType.STDERR)
        } else {
            file.content.lines().forEach { appendLine(it, OutputType.STDOUT) }
        }
    }

    private fun handleTouch(args: List<String>) {
        if (args.isEmpty()) return
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        args.forEach { name ->
            val fullPath = if (name.startsWith("/")) name else "$cwd/$name"
            if (!vfs.containsKey(fullPath)) {
                vfs[fullPath] = VFile(name, fullPath, false, "")
                gitUnstaged.add(name)
            }
        }
    }

    private fun handleMkdir(args: List<String>) {
        if (args.isEmpty()) return
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val name = args.last()
        val fullPath = "$cwd/$name"
        vfs[fullPath] = VFile(name, fullPath, true, "")
    }

    private fun handleRm(args: List<String>) {
        val target = args.lastOrNull() ?: return
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val fullPath = if (target.startsWith("/")) target else "$cwd/$target"
        if (vfs.remove(fullPath) != null) {
            gitUnstaged.remove(target)
            gitStaged.remove(target)
        } else {
            appendLine("rm: cannot remove '$target': No such file or directory", OutputType.STDERR)
        }
    }

    private fun handleEcho(args: List<String>) {
        val joined = args.joinToString(" ")
        if (joined.contains(">")) {
            val append = joined.contains(">>")
            val delim = if (append) ">>" else ">"
            val parts = joined.split(delim, limit = 2)
            val text = parts[0].trim().replace("\"", "").replace("'", "")
            val fileName = parts[1].trim()
            val cwd = env["PWD"] ?: "/home/lemon/dev"
            val fullPath = if (fileName.startsWith("/")) fileName else "$cwd/$fileName"
            val existing = vfs[fullPath]?.content ?: ""
            val newContent = if (append && existing.isNotEmpty()) "$existing\n$text" else text
            vfs[fullPath] = VFile(fileName, fullPath, false, newContent)
            gitUnstaged.add(fileName)
        } else {
            appendLine(joined.replace("\"", "").replace("'", ""), OutputType.STDOUT)
        }
    }

    private fun handleNano(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("nano: specify file name to edit", OutputType.WARNING)
            return
        }
        val target = args[0]
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val fullPath = if (target.startsWith("/")) target else "$cwd/$target"
        var file = vfs[fullPath]
        if (file == null) {
            file = VFile(target, fullPath, false, "")
            vfs[fullPath] = file
        }
        _editingFile.value = file
    }

    fun saveAndCloseEditor(newContent: String) {
        val file = _editingFile.value
        if (file != null) {
            file.content = newContent
            gitUnstaged.add(file.name)
            appendLine("[nano] File saved: ${file.name} (${newContent.length} bytes)", OutputType.SUCCESS)
        }
        _editingFile.value = null
    }

    fun cancelEditor() {
        _editingFile.value = null
    }

    private fun handleGrep(args: List<String>) {
        if (args.size < 2) {
            appendLine("usage: grep <pattern> <file>", OutputType.WARNING)
            return
        }
        val pattern = args[0]
        val target = args[1]
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val file = vfs["$cwd/$target"]
        if (file != null) {
            file.content.lines().forEach { line ->
                if (line.contains(pattern, ignoreCase = true)) {
                    appendLine(line, OutputType.SUCCESS)
                }
            }
        }
    }

    private fun handleWc(args: List<String>) {
        if (args.isEmpty()) return
        val target = args[0]
        val cwd = env["PWD"] ?: "/home/lemon/dev"
        val file = vfs["$cwd/$target"]
        if (file != null) {
            val lines = file.content.lines().size
            val words = file.content.split("\\s+".toRegex()).size
            val bytes = file.content.length
            appendLine("  $lines  $words $bytes $target", OutputType.STDOUT)
        }
    }

    private fun handleUname(args: List<String>) {
        if (args.contains("-a")) {
            appendLine("Linux lemon-box 6.6.21-lemon-rt-aarch64 #1 SMP PREEMPT Thu Sep 10 09:20:00 UTC 2026 aarch64 GNU/Linux", OutputType.STDOUT)
        } else {
            appendLine("Linux", OutputType.STDOUT)
        }
    }

    private fun handleHistory() {
        _history.forEachIndexed { idx, cmd ->
            appendLine("  %3d  %s".format(idx + 1, cmd), OutputType.STDOUT)
        }
    }

    private fun handleEnv() {
        env.forEach { (k, v) -> appendLine("$k=$v", OutputType.STDOUT) }
    }

    private fun handleExport(args: List<String>) {
        if (args.isEmpty()) return
        val pair = args[0].split("=", limit = 2)
        if (pair.size == 2) {
            env[pair[0].trim()] = pair[1].trim()
        }
    }

    private fun handleAlias(args: List<String>) {
        if (args.isEmpty()) {
            aliases.forEach { (k, v) -> appendLine("alias $k='$v'", OutputType.STDOUT) }
        } else {
            val pair = args[0].split("=", limit = 2)
            if (pair.size == 2) {
                aliases[pair[0].trim()] = pair[1].trim().replace("'", "")
            }
        }
    }

    private fun handleTop() {
        appendLine("top - 09:22:45 up 4:12, 1 user, load average: 0.14, 0.22, 0.18", OutputType.INFO)
        appendLine("Tasks: ${activeProcesses.size} total, 2 running, 3 sleeping, 0 stopped", OutputType.STDOUT)
        appendLine("%Cpu(s):  2.4 us,  1.1 sy,  0.0 ni, 96.2 id,  0.2 wa,  0.1 hi", OutputType.STDOUT)
        appendLine("MiB Mem :  512.0 total,  184.2 free,  142.6 used,  185.2 buff/cache", OutputType.STDOUT)
        appendLine("", OutputType.STDOUT)
        appendLine("  PID USER      PR  NI    VIRT    RES  %CPU  %MEM     TIME+ COMMAND", OutputType.INFO)
        activeProcesses.forEach {
            appendLine("%5d %-9s 20   0  %6.1fM %5.1fM  %4.1f  %4.1f   %s %s".format(
                it.pid, it.user, it.memoryMb * 1.5, it.memoryMb, it.cpuPercent, it.memoryMb / 5.12f, it.time, it.name
            ), OutputType.STDOUT)
        }
    }

    private fun handlePs() {
        appendLine("  PID TTY          TIME CMD", OutputType.INFO)
        activeProcesses.forEach {
            appendLine("%5d pts/0    %s %s".format(it.pid, it.time, it.name), OutputType.STDOUT)
        }
    }

    private fun handleFree() {
        val r = getResourceSnapshot()
        appendLine("               total        used        free      shared  buff/cache   available", OutputType.INFO)
        appendLine("Mem:             %3d         %3d         %3d           4          82         %3d".format(
            r.memoryTotalMb, r.memoryUsedMb, r.memoryFreeMb, r.memoryFreeMb
        ), OutputType.STDOUT)
        appendLine("Swap:            128           0         128", OutputType.STDOUT)
    }

    private fun handleKill(args: List<String>) {
        if (args.isEmpty()) {
            appendLine("kill: usage: kill <pid>", OutputType.WARNING)
            return
        }
        val pid = args[0].toIntOrNull()
        if (pid != null) {
            val removed = activeProcesses.removeAll { it.pid == pid }
            if (removed) {
                appendLine("[process_mgr] Process $pid terminated.", OutputType.SUCCESS)
            } else {
                appendLine("kill: ($pid) - No such process", OutputType.STDERR)
            }
        }
    }

    private fun handleCurl(args: List<String>) {
        val url = args.firstOrNull { !it.startsWith("-") } ?: "https://lemonbox.local"
        appendLine("HTTP/1.1 200 OK", OutputType.SUCCESS)
        appendLine("Server: lemon_box-microhttp/1.0", OutputType.STDOUT)
        appendLine("Content-Type: text/html; charset=UTF-8", OutputType.STDOUT)
        appendLine("Content-Length: 142", OutputType.STDOUT)
        appendLine("", OutputType.STDOUT)
        appendLine("<!DOCTYPE html><html><head><title>lemon_box Dev Gateway</title></head><body><h1>lemon_box Active</h1></body></html>", OutputType.INFO)
    }

    private fun handleChmod(args: List<String>) {
        if (args.size > 1) {
            val mode = args[0]
            val file = args[1]
            val cwd = env["PWD"] ?: "/home/lemon/dev"
            vfs["$cwd/$file"]?.let {
                appendLine("chmod: updated permissions of '$file' to $mode", OutputType.SUCCESS)
            }
        }
    }

    // Keyboard shortcut actions
    fun handleCtrlC() {
        if (_isReplMode.value) {
            _isReplMode.value = false
            _currentPrompt.value = "lemon@box:~/dev$ "
            appendLine("^C KeyboardInterrupt: Exited Python REPL", OutputType.WARNING)
        } else {
            appendLine("^C", OutputType.WARNING)
        }
        _currentInput.value = ""
    }

    fun handleCtrlL() {
        _lines.value = emptyList()
    }

    fun handleCtrlD() {
        if (_isReplMode.value) {
            _isReplMode.value = false
            _currentPrompt.value = "lemon@box:~/dev$ "
            appendLine("Exited Python REPL (EOF).", OutputType.INFO)
        } else {
            appendLine("logout (Connection closed to lemon_box console)", OutputType.INFO)
        }
    }

    fun historyUp() {
        if (_history.isNotEmpty() && historyIndex > 0) {
            historyIndex--
            _currentInput.value = _history[historyIndex]
        }
    }

    fun historyDown() {
        if (historyIndex < _history.size - 1) {
            historyIndex++
            _currentInput.value = _history[historyIndex]
        } else {
            historyIndex = _history.size
            _currentInput.value = ""
        }
    }

    fun getResourceSnapshot(): SystemResourceSnapshot {
        val runtime = Runtime.getRuntime()
        val totalMem = (runtime.totalMemory() / (1024 * 1024)).coerceAtLeast(64)
        val freeMem = (runtime.freeMemory() / (1024 * 1024)).coerceAtLeast(16)
        val usedMem = (totalMem - freeMem).coerceAtLeast(24)

        // Realistic simulated CPU % with slight jitter
        val cpu = (12 + (Math.random() * 8)).roundToInt()

        return SystemResourceSnapshot(
            cpuPercent = cpu,
            memoryUsedMb = usedMem,
            memoryTotalMb = 512,
            memoryFreeMb = (512 - usedMem).coerceAtLeast(32),
            storageFreeMb = 14200,
            uptimeSeconds = 15120L + (System.currentTimeMillis() / 1000 % 3600),
            processList = activeProcesses.toList()
        )
    }

    private fun appendLine(text: String, type: OutputType) {
        _lines.value = _lines.value + TerminalLine(text, type)
    }

    private fun splitCommandLine(command: String): List<String> {
        val regex = Regex("""[^\s"']+|"([^"]*)"|'([^']*)'""")
        return regex.findAll(command).map { match ->
            match.groupValues[1].ifEmpty { match.groupValues[2].ifEmpty { match.value } }
        }.toList()
    }
}

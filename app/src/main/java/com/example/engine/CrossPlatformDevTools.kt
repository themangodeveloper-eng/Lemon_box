package com.example.engine

class CrossPlatformDevTools {

    // Java Dev Kit (JDK 21)
    fun javaVersion(): List<String> = listOf(
        "openjdk version \"21.0.3\" 2026-04-16 LTS",
        "OpenJDK Runtime Environment (lemon_box embedded build 21.0.3+9-LTS)",
        "OpenJDK 64-Bit Server VM (build 21.0.3+9-LTS, mixed mode, sharing)",
        "Target Arch: aarch64 (Linux 6.6-rt android)"
    )

    fun compileJava(fileName: String, code: String): CompileResult {
        if (!fileName.endsWith(".java")) {
            return CompileResult(false, listOf("javac: error: file '$fileName' is not a valid Java source file (expected *.java)"))
        }
        val className = fileName.removeSuffix(".java")
        if (!code.contains("class $className")) {
            return CompileResult(false, listOf(
                "javac: error: class declaration matching '$className' not found in $fileName",
                "  public class $className { ... }"
            ))
        }
        if (!code.contains("public static void main")) {
            return CompileResult(false, listOf(
                "javac: warning: no 'public static void main(String[] args)' found in $className",
                "Compiled successfully with 1 warning: $className.class"
            ), className)
        }

        return CompileResult(
            success = true,
            output = listOf(
                "[javac] Compiling 1 source file to target bytecode 21",
                "[javac] Parsing $fileName...",
                "[javac] Generating $className.class [2.4 KB] (JVM Bytecode major version 65.0)",
                "[javac] Compilation finished in 84ms with 0 errors."
            ),
            artifactName = "$className.class"
        )
    }

    fun runJava(className: String, sourceCode: String?): List<String> {
        val cleanName = className.removeSuffix(".class")
        val lines = mutableListOf<String>()
        lines.add("[java] Starting JVM instance for class: $cleanName")

        if (sourceCode != null && sourceCode.contains("System.out.println(")) {
            val regex = Regex("""System\.out\.println\((.*?)\);""")
            val matches = regex.findAll(sourceCode)
            for (m in matches) {
                var rawArg = m.groupValues[1].trim()
                if ((rawArg.startsWith("\"") && rawArg.endsWith("\"")) || (rawArg.startsWith("'") && rawArg.endsWith("'"))) {
                    rawArg = rawArg.substring(1, rawArg.length - 1)
                }
                lines.add(rawArg)
            }
        } else {
            lines.add("Hello from lemon_box JVM Runtime! Cross-platform execution verified.")
            lines.add("Process finished with exit code 0 (execution time: 38ms)")
        }
        return lines
    }

    fun javapDisassemble(className: String): List<String> {
        val cleanName = className.removeSuffix(".class")
        return listOf(
            "Compiled from \"$cleanName.java\"",
            "public class $cleanName {",
            "  public $cleanName();",
            "    Code:",
            "       0: aload_0",
            "       1: invokespecial #1                  // Method java/lang/Object.\"<init>\":()V",
            "       4: return",
            "",
            "  public static void main(java.lang.String[]);",
            "    Code:",
            "       0: getstatic     #7                  // Field java/lang/System.out:Ljava/io/PrintStream;",
            "       3: ldc           #13                 // String Hello lemon_box",
            "       5: invokevirtual #15                 // Method java/io/PrintStream.println:(Ljava/lang/String;)V",
            "       8: return",
            "}"
        )
    }

    // Android SDK & Build Tools
    fun sdkList(): List<String> = listOf(
        "Installed Android SDK Packages (Path: /opt/android-sdk):",
        "  -------------------------------------------------------------",
        "  Path                         | Version | Description",
        "  -----------------------------+---------+---------------------",
        "  platforms;android-35         | 1       | Android SDK Platform 35 (Vanilla)",
        "  platforms;android-34         | 3       | Android SDK Platform 34 (UpsideDownCake)",
        "  build-tools;35.0.0           | 35.0.0  | Android SDK Build-Tools 35.0.0",
        "  platform-tools               | 35.0.1  | Android SDK Platform-Tools",
        "  cmdline-tools;latest         | 13.0    | Android SDK Command-line Tools",
        "  ndk;26.3.11579264            | 26.3.11 | Android NDK (Side by side) r26d"
    )

    fun adbDevices(): List<String> = listOf(
        "List of devices attached",
        "emulator-5554          device product:lemon_box_arm64 model:Virtual_Linux device:generic",
        "localhost:5555         device product:lemon_desktop transport_id:1"
    )

    fun runGradleBuild(task: String): List<String> = listOf(
        "> Configure project :app",
        "Using Kotlin 2.2.10 and Compose Compiler with Android Gradle Plugin 9.1",
        "",
        "> Task :app:preBuild UP-TO-DATE",
        "> Task :app:compileDebugKotlin",
        "> Task :app:processDebugResources",
        "> Task :app:compileDebugJavaWithJavac",
        "> Task :app:mergeDebugNativeLibs",
        "> Task :app:dexBuilderDebug",
        "> Task :app:mergeProjectDexDebug",
        "> Task :app:packageDebug",
        "> Task :app:assembleDebug",
        "",
        "BUILD SUCCESSFUL in 1s 420ms",
        "8 actionable tasks: 8 executed",
        "Output artifact: app/build/outputs/apk/debug/app-debug.apk [4.8 MB]"
    )

    // Android NDK & C/C++ Cross Compilation
    fun ndkBuild(): List<String> = listOf(
        "[lemon_ndk] Using Android NDK r26d (LLVM Clang 17.0.2)",
        "[arm64-v8a] Compile   : lemon_core <= native-lib.cpp",
        "[arm64-v8a] SharedLib : liblemon_core.so",
        "[x86_64]    Compile   : lemon_core <= native-lib.cpp",
        "[x86_64]    SharedLib : liblemon_core.so",
        "[lemon_ndk] Stripping unneeded symbols: arm64-v8a/liblemon_core.so [18 KB]",
        "[lemon_ndk] NDK build completed successfully for 2 ABI targets."
    )

    fun compileClang(fileName: String, outName: String, code: String): CompileResult {
        if (!fileName.endsWith(".c") && !fileName.endsWith(".cpp")) {
            return CompileResult(false, listOf("clang: error: expected .c or .cpp file, received: $fileName"))
        }
        if (!code.contains("main(")) {
            return CompileResult(false, listOf("clang: error: undefined reference to 'main' in $fileName"))
        }

        return CompileResult(
            success = true,
            output = listOf(
                "clang -O2 -target aarch64-linux-android $fileName -o $outName",
                "[clang] Code generation: 1 compilation unit -> ELF 64-bit LSB pie executable, ARM aarch64",
                "[linker] Dynamic link against libc.so, libm.so, libdl.so",
                "Compilation finished: binary generated -> $outName"
            ),
            artifactName = outName
        )
    }

    fun runNativeBinary(binaryName: String, code: String?): List<String> {
        val lines = mutableListOf<String>()
        lines.add("[exec] Spawning native ELF process: ./$binaryName")
        if (code != null && (code.contains("printf(") || code.contains("std::cout"))) {
            val regex = Regex("""printf\("(.*?)"\);""")
            val matches = regex.findAll(code)
            for (m in matches) {
                lines.add(m.groupValues[1].replace("\\n", ""))
            }
            if (lines.size == 1) {
                lines.add("Native binary output: execution finished with exit status 0")
            }
        } else {
            lines.add("Native execution completed successfully (status: 0)")
        }
        return lines
    }
}

data class CompileResult(
    val success: Boolean,
    val output: List<String>,
    val artifactName: String? = null
)

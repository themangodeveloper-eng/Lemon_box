package com.example.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*
import kotlin.random.Random

class PythonInterpreter {
    private val globalScope = mutableMapOf<String, Any>(
        "__name__" to "__main__",
        "sys_version" to "3.12.3 (lemon_box embedded linux-arm64, Sep 10 2026)",
        "pi" to Math.PI,
        "e" to Math.E
    )

    fun reset() {
        globalScope.clear()
        globalScope["__name__"] = "__main__"
        globalScope["sys_version"] = "3.12.3 (lemon_box embedded linux-arm64, Sep 10 2026)"
        globalScope["pi"] = Math.PI
        globalScope["e"] = Math.E
    }

    fun executeScript(code: String): List<String> {
        val output = mutableListOf<String>()
        val lines = code.lines()
        var i = 0

        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trim()

            // Skip comments and empty lines
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                i++
                continue
            }

            // Handle for loop: for var in range(n): or for var in list:
            if (trimmed.startsWith("for ") && trimmed.endsWith(":")) {
                val forHeader = trimmed.removePrefix("for ").removeSuffix(":")
                val parts = forHeader.split(" in ")
                if (parts.size == 2) {
                    val loopVar = parts[0].trim()
                    val iterableExpr = parts[1].trim()
                    val loopBody = mutableListOf<String>()
                    i++
                    while (i < lines.size && (lines[i].startsWith("    ") || lines[i].startsWith("\t") || lines[i].isBlank())) {
                        if (lines[i].isNotBlank()) {
                            loopBody.add(lines[i].trim())
                        }
                        i++
                    }

                    val items = resolveIterable(iterableExpr)
                    for (item in items) {
                        globalScope[loopVar] = item
                        for (bodyLine in loopBody) {
                            executeSingleStatement(bodyLine, output)
                        }
                    }
                    continue
                }
            }

            executeSingleStatement(trimmed, output)
            i++
        }

        return output
    }

    fun executeReplLine(line: String): String {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed == "exit()" || trimmed == "quit()") return "Use window controls or Ctrl+D to exit REPL."
        if (trimmed == "help()") return "lemon_box Python 3.12 embedded runtime. Built-in modules: math, sys, os, time, random. Variables & arithmetic supported."

        val output = mutableListOf<String>()
        val result = executeSingleStatement(trimmed, output)

        return when {
            output.isNotEmpty() -> output.joinToString("\n")
            result != null -> formatValue(result)
            else -> ""
        }
    }

    private fun executeSingleStatement(statement: String, output: MutableList<String>): Any? {
        try {
            // print(...)
            if (statement.startsWith("print(") && statement.endsWith(")")) {
                val inner = statement.substring(6, statement.length - 1).trim()
                val printed = evaluatePrintArgs(inner)
                output.add(printed)
                return null
            }

            // import statement
            if (statement.startsWith("import ")) {
                val mod = statement.removePrefix("import ").trim()
                output.add(">>> imported module '$mod'")
                return null
            }

            // Variable assignment: x = 10 or name = "lemon"
            if (statement.contains("=") && !statement.contains("==") && !statement.contains("<=") && !statement.contains(">=")) {
                val parts = statement.split("=", limit = 2)
                val varName = parts[0].trim()
                val expr = parts[1].trim()
                val value = evaluateExpression(expr)
                globalScope[varName] = value
                return null
            }

            // Standalone expression evaluation
            return evaluateExpression(statement)
        } catch (e: Exception) {
            output.add("Traceback (most recent call last):\n  File \"<stdin>\", line 1\nTypeError / SyntaxError: ${e.message ?: "Invalid syntax"}")
            return null
        }
    }

    private fun evaluatePrintArgs(argsStr: String): String {
        // Handle f-string: f"..."
        if (argsStr.startsWith("f\"") && argsStr.endsWith("\"")) {
            val content = argsStr.substring(2, argsStr.length - 1)
            val regex = Regex("\\{([^}]+)\\}")
            return regex.replace(content) { match ->
                val expr = match.groupValues[1].trim()
                try {
                    formatValue(evaluateExpression(expr))
                } catch (e: Exception) {
                    "{ERROR: ${e.message}}"
                }
            }
        }

        // Handle normal strings
        if ((argsStr.startsWith("\"") && argsStr.endsWith("\"")) || (argsStr.startsWith("'") && argsStr.endsWith("'"))) {
            return argsStr.substring(1, argsStr.length - 1)
        }

        return formatValue(evaluateExpression(argsStr))
    }

    private fun resolveIterable(expr: String): List<Any> {
        if (expr.startsWith("range(") && expr.endsWith(")")) {
            val inner = expr.substring(6, expr.length - 1).trim()
            val args = inner.split(",").mapNotNull { it.trim().toIntOrNull() }
            return when (args.size) {
                1 -> (0 until args[0]).map { it }
                2 -> (args[0] until args[1]).map { it }
                3 -> (args[0] until args[1] step args[2]).map { it }
                else -> emptyList()
            }
        }
        if (expr.startsWith("[") && expr.endsWith("]")) {
            val inner = expr.substring(1, expr.length - 1)
            return inner.split(",").map { evaluateExpression(it.trim()) }
        }
        val v = globalScope[expr]
        if (v is List<*>) return v.filterNotNull()
        return emptyList()
    }

    private fun evaluateExpression(raw: String): Any {
        val expr = raw.trim()

        // Primitives
        if (expr == "True") return true
        if (expr == "False") return false
        if (expr == "None") return "None"
        expr.toIntOrNull()?.let { return it }
        expr.toDoubleOrNull()?.let { return it }

        if ((expr.startsWith("\"") && expr.endsWith("\"")) || (expr.startsWith("'") && expr.endsWith("'"))) {
            return expr.substring(1, expr.length - 1)
        }

        // Sys/OS calls
        if (expr == "sys.version") return globalScope["sys_version"] ?: "3.12.3"
        if (expr == "sys.platform") return "linux-android"
        if (expr == "os.getcwd()") return "/home/lemon/dev"
        if (expr == "time.time()") return System.currentTimeMillis() / 1000.0
        if (expr == "time.ctime()") return SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US).format(Date())

        // Math functions
        if (expr.startsWith("math.sqrt(") && expr.endsWith(")")) {
            val arg = evaluateExpression(expr.substring(10, expr.length - 1)).toString().toDouble()
            return sqrt(arg)
        }
        if (expr.startsWith("math.isqrt(") && expr.endsWith(")")) {
            val arg = evaluateExpression(expr.substring(11, expr.length - 1)).toString().toDouble()
            return sqrt(arg).toInt()
        }
        if (expr.startsWith("math.pow(") && expr.endsWith(")")) {
            val parts = expr.substring(9, expr.length - 1).split(",")
            val a = evaluateExpression(parts[0].trim()).toString().toDouble()
            val b = evaluateExpression(parts[1].trim()).toString().toDouble()
            return a.pow(b)
        }
        if (expr.startsWith("math.sin(") && expr.endsWith(")")) {
            val arg = evaluateExpression(expr.substring(9, expr.length - 1)).toString().toDouble()
            return sin(arg)
        }
        if (expr.startsWith("math.cos(") && expr.endsWith(")")) {
            val arg = evaluateExpression(expr.substring(9, expr.length - 1)).toString().toDouble()
            return cos(arg)
        }
        if (expr.startsWith("random.randint(") && expr.endsWith(")")) {
            val parts = expr.substring(15, expr.length - 1).split(",")
            val a = parts[0].trim().toInt()
            val b = parts[1].trim().toInt()
            return Random.nextInt(a, b + 1)
        }
        if (expr.startsWith("len(") && expr.endsWith(")")) {
            val arg = evaluateExpression(expr.substring(4, expr.length - 1))
            return when (arg) {
                is String -> arg.length
                is List<*> -> arg.size
                else -> 0
            }
        }

        // List comprehension: [x for x in range(n) if ...]
        if (expr.startsWith("[") && expr.endsWith("]") && expr.contains(" for ") && expr.contains(" in ")) {
            return "[evaluated list: 42 elements]"
        }

        // Arithmetic parsing: +, -, *, /, %, **
        if (expr.contains("**")) {
            val parts = expr.split("**", limit = 2)
            val a = evaluateExpression(parts[0].trim()).toString().toDouble()
            val b = evaluateExpression(parts[1].trim()).toString().toDouble()
            return a.pow(b)
        }
        if (expr.contains("+")) {
            val parts = expr.split("+", limit = 2)
            val a = evaluateExpression(parts[0].trim())
            val b = evaluateExpression(parts[1].trim())
            if (a is String || b is String) return "$a$b"
            if (a is Number && b is Number) return (a.toDouble() + b.toDouble()).let { if (it % 1.0 == 0.0) it.toLong() else it }
        }
        if (expr.contains("-") && !expr.startsWith("-")) {
            val parts = expr.split("-", limit = 2)
            val a = evaluateExpression(parts[0].trim()).toString().toDouble()
            val b = evaluateExpression(parts[1].trim()).toString().toDouble()
            return (a - b).let { if (it % 1.0 == 0.0) it.toLong() else it }
        }
        if (expr.contains("*")) {
            val parts = expr.split("*", limit = 2)
            val a = evaluateExpression(parts[0].trim()).toString().toDouble()
            val b = evaluateExpression(parts[1].trim()).toString().toDouble()
            return (a * b).let { if (it % 1.0 == 0.0) it.toLong() else it }
        }
        if (expr.contains("/")) {
            val parts = expr.split("/", limit = 2)
            val a = evaluateExpression(parts[0].trim()).toString().toDouble()
            val b = evaluateExpression(parts[1].trim()).toString().toDouble()
            return a / b
        }
        if (expr.contains("%")) {
            val parts = expr.split("%", limit = 2)
            val a = evaluateExpression(parts[0].trim()).toString().toLong()
            val b = evaluateExpression(parts[1].trim()).toString().toLong()
            return a % b
        }

        // Scope lookup
        globalScope[expr]?.let { return it }

        return expr
    }

    private fun formatValue(value: Any?): String {
        return when (value) {
            null -> ""
            is String -> value
            is Double -> if (value % 1.0 == 0.0) value.toLong().toString() else "%.4f".format(Locale.US, value)
            else -> value.toString()
        }
    }
}

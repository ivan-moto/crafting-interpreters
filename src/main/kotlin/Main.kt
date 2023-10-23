package main

import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.system.exitProcess

var hadError = false

fun main(args: Array<String>) {
    when {
        args.size > 1 -> println("Usage: jlox [script]").also { exitProcess(64) }
        args.size == 1 -> runFile(args.first())
        else -> runPrompt()
    }
}

private fun runFile(path: String) {
    run(Path(path).readText())
    if (hadError) exitProcess(65)
}

private fun runPrompt() {
    while (true) {
        print("> ")
        val line = readlnOrNull()
        if (line == null) return
        run(line)
        hadError = false
    }
}

private fun run(source: String) {
    val tokens = Scanner(source).scanTokens()
    for (token in tokens) {
        println(token)
    }
}

private fun error(line: Int, message: String) {
    report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
    hadError = true
    System.err.println("[Line $line] Error $where: $message")
}

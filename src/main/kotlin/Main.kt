package main

import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.system.exitProcess

var hadError = false
var hadRuntimeError = false

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
    if (hadRuntimeError) exitProcess(70)
}

private fun runPrompt() {
    while (true) {
        print("> ")
        val line = readlnOrNull() ?: return
        run(line)
        hadError = false
    }
}

val scanner = Scanner()
val parser = Parser()
val interpreter = Interpreter()

private fun run(source: String) {
    val tokens = scanner.scanTokens(source)
    val statements = parser.parse(tokens)

    // Stop if there was a syntax error.
    if (hadError) return

    interpreter.interpret(statements)
}

private fun error(line: Int, message: String) {
    report(line, "", message)
}

private fun report(line: Int, where: String, message: String) {
    hadError = true
    System.err.println("[Line $line] Error $where: $message")
}

fun error(token: Token, message: String) {
    when (token.type) {
        TokenType.EOF -> report(token.line, "at end", message)
        else -> report(token.line, "at '${token.lexeme}'", message)
    }
}

fun runtimeError(error: RuntimeError) {
    hadRuntimeError = true
    System.err.println("[Line ${error.token.line}] ${error.message}")
}

package main

enum class TokenType {
    // Single character tokens
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    COMMA,
    DOT,
    MINUS,
    PLUS,
    SEMICOLON,
    SLASH,
    STAR,

    // One or two character tokens
    BANG,
    BANG_EQUAL,
    EQUAL,
    EQUAL_EQUAL,
    GREATER,
    GREATER_EQUAL,
    LESS,
    LESS_EQUAL,

    // Literals
    IDENTIFIER,
    STRING,
    NUMBER,

    // Keywords
    AND,
    CLASS,
    ELSE,
    FALSE,
    FUN,
    FOR,
    IF,
    NIL,
    OR,
    PRINT,
    RETURN,
    SUPER,
    THIS,
    TRUE,
    VAR,
    WHILE,

    EOF
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int,
    val literal: Any? = null,
)

class Scanner {

    private val keywords = mapOf(
        "and" to TokenType.AND,
        "class" to TokenType.CLASS,
        "else" to TokenType.ELSE,
        "false" to TokenType.FALSE,
        "fun" to TokenType.FUN,
        "for" to TokenType.FOR,
        "if" to TokenType.IF,
        "nil" to TokenType.NIL,
        "or" to TokenType.OR,
        "print" to TokenType.PRINT,
        "return" to TokenType.RETURN,
        "super" to TokenType.SUPER,
        "this" to TokenType.THIS,
        "true" to TokenType.TRUE,
        "var" to TokenType.VAR,
        "while" to TokenType.WHILE,
    )

    fun scanTokens(source: String) = buildList {
        val iter = SourceIterator(source)
        fun Token(type: TokenType, literal: Any? = null) = Token(
            type, source.substring(iter.start, iter.current), iter.line, literal
        )
        while (iter.hasNext()) {
            iter.start = iter.current
            when (iter.next()) {
                '(' -> add(Token(TokenType.LEFT_PAREN))
                ')' -> add(Token(TokenType.RIGHT_PAREN))
                '{' -> add(Token(TokenType.LEFT_BRACE))
                '}' -> add(Token(TokenType.RIGHT_BRACE))
                ',' -> add(Token(TokenType.COMMA))
                '.' -> add(Token(TokenType.DOT))
                '-' -> add(Token(TokenType.MINUS))
                '+' -> add(Token(TokenType.PLUS))
                ';' -> add(Token(TokenType.SEMICOLON))
                '*' -> add(Token(TokenType.STAR))
                '!' -> add(Token(if (iter.match('=')) TokenType.BANG_EQUAL else TokenType.BANG))
                '=' -> add(Token(if (iter.match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL))
                '<' -> add(Token(if (iter.match('=')) TokenType.LESS_EQUAL else TokenType.LESS))
                '>' -> add(Token(if (iter.match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER))
                '/' -> {
                    if (iter.match('/')) {
                        // A comment goes until the end of the line.
                        while (iter.peek() != '\n' && iter.hasNext()) iter.next()
                    }
                    // Not a comment.
                    add(Token(TokenType.SLASH))
                }
                ' ', '\r', '\t' -> continue
                '\n' -> iter.line++
                '"' -> {
                    while (iter.peek() != '"' && iter.hasNext()) {
                        if (iter.peek() == '\n') iter.line++
                        iter.next()
                    }
                    if (!iter.hasNext()) {
                        println("Unterminated string. Line: ${iter.line}")
                        continue
                    }
                    // The closing "".
                    iter.next()
                    // Trim surrounding quotes.
                    add(Token(TokenType.STRING, source.substring(iter.start + 1, iter.current - 1)))
                }
                in '0'..'9' -> {
                    while (isDigit(iter.peek())) iter.next()

                    // Look for fractional part.
                    if (iter.peek() == '.' && isDigit(iter.peekNext())) {
                        // Consume the "."
                        iter.next()

                        while (isDigit(iter.peek())) iter.next()
                    }
                    add(Token(TokenType.NUMBER, source.substring(iter.start, iter.current).toDouble()))
                }
                in 'a'..'z', in 'A'..'Z', '_' -> {
                    while(isAlphaNumeric(iter.peek())) iter.next()
                    val text = source.substring(iter.start, iter.current)
                    add(Token(keywords[text] ?: TokenType.IDENTIFIER, text))
                }
                else -> println("Unexpected character. Line: ${iter.line}")
            }

        }
        add(Token(TokenType.EOF))
    }

    private fun isDigit(char: Char?): Boolean = char != null && char in '0'..'9'
    private fun isAlpha(char: Char?): Boolean = char != null && (char in 'a'..'z' || char in 'A'..'Z' || char == '_')
    private fun isAlphaNumeric(char: Char?): Boolean = isAlpha(char) || isDigit(char)

    private class SourceIterator(val source: String) {
        // index of first char in lexeme being scanned
        var start = 0
        // index of current char in lexeme being scanned
        var current = 0
        // source line of current char
        var line = 1

        fun hasNext(): Boolean = current in source.indices
        fun next(): Char = source[current++]
        fun peek(): Char? = if (!hasNext()) null else source[current]
        fun peekNext(): Char? = if (current + 1 !in source.indices) null else source[current + 1]
        fun match(expected: Char): Boolean = (hasNext() && source[current] == expected).also { if (it) current++ }
    }
}

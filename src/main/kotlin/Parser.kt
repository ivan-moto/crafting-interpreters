package main

class ParseError(
    token: Token,
    message: String,
) : RuntimeException() {
    init {
        error(token, message)
    }
}

class Parser {

    fun parse(tokens: List<Token>): Expr? {
        val iter = TokenIterator(tokens)
        return try {
            expression(iter)
        } catch (e: ParseError) {
            null
        }
    }

    private fun expression(iter: TokenIterator): Expr {
        return equality(iter)
    }

    private fun equality(iter: TokenIterator): Expr {
        var expr = comparison(iter)
        while (iter.match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = iter.previous()
            val right = comparison(iter)
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun comparison(iter: TokenIterator): Expr {
        var expr = term(iter)
        while (iter.match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = iter.previous()
            val right = term(iter)
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun term(iter: TokenIterator): Expr {
        var expr = factor(iter)
        while (iter.match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = iter.previous()
            val right = factor(iter)
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun factor(iter: TokenIterator): Expr {
        var expr = unary(iter)
        while (iter.match(TokenType.SLASH, TokenType.STAR)) {
            val operator = iter.previous()
            val right = unary(iter)
            expr = Expr.Binary(expr, operator, right)
        }
        return expr
    }

    private fun unary(iter: TokenIterator): Expr {
        if (iter.match(TokenType.BANG, TokenType.MINUS)) {
            val operator = iter.previous()
            val right = unary(iter)
            return Expr.Unary(operator, right)
        }
        return primary(iter)
    }

    private fun primary(iter: TokenIterator): Expr {
        return when {
            iter.match(TokenType.FALSE) -> Expr.Literal(false)
            iter.match(TokenType.TRUE) -> Expr.Literal(true)
            iter.match(TokenType.NIL) -> Expr.Literal(null)
            iter.match(TokenType.NUMBER, TokenType.STRING) -> Expr.Literal(iter.previous().literal)
            iter.match(TokenType.LEFT_PAREN) -> {
                val expr = expression(iter)
                consume(iter, TokenType.RIGHT_PAREN, "Expect ')' after expression.")
                Expr.Grouping(expr)
            }
            else -> throw ParseError(iter.peek(), "Expect expression.")
        }
    }

    private fun consume(iter: TokenIterator, type: TokenType, message: String): Token {
        if (iter.nextHasType(type)) return iter.next()
        throw ParseError(iter.peek(), message)
    }

    private class TokenIterator(val tokens: List<Token>) {
        private var current = 0

        fun match(vararg types: TokenType): Boolean = types.any { nextHasType(it) }.also { if (it) next() }
        fun nextHasType(type: TokenType): Boolean = hasNext() && peek().type == type
        fun hasNext(): Boolean = peek().type != TokenType.EOF
        fun next(): Token = tokens[current++]
        fun peek(): Token = tokens[current]
        fun previous(): Token = tokens[current - 1]
    }
}
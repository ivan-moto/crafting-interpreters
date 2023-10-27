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

    fun parse(tokens: List<Token>): List<Stmt> {
        return buildList {
            val iter = TokenIterator(tokens)
            while (iter.hasNext()) {
                declaration(iter).also { if (it != null) add (it) }
            }
        }
    }

    private fun declaration(iter: TokenIterator): Stmt? {
        return try {
            if (iter.match(TokenType.VAR)) return varDeclaration(iter)
            statement(iter)
        } catch (e: ParseError) {
            synchronize(iter)
            null
        }
    }

    private fun synchronize(iter: TokenIterator) {
        val previous = iter.next()
        while (iter.hasNext()) {
            if (previous.type == TokenType.SEMICOLON) return
            when (iter.peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR, TokenType.FOR, TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return
                else -> iter.next()
            }
        }
    }

    private fun varDeclaration(iter: TokenIterator): Stmt {
        val name = consume(iter, TokenType.IDENTIFIER, "Expect variable name")
        val initializer = if (iter.match(TokenType.EQUAL)) expression(iter) else null
        consume(iter, TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return Stmt.Var(name, initializer)
    }

    private fun statement(iter: TokenIterator): Stmt {
        if (iter.match(TokenType.PRINT)) return printStatement(iter)
        if (iter.match(TokenType.LEFT_BRACE)) return Stmt.Block(block(iter))
        return expressionStatement(iter)
    }

    private fun printStatement(iter: TokenIterator): Stmt {
        val value = expression(iter)
        consume(iter, TokenType.SEMICOLON, "Expect ';' after value.")
        return Stmt.Print(value)
    }

    private fun block(iter: TokenIterator): List<Stmt> {
        return buildList {
            while (!iter.nextHasType(TokenType.RIGHT_BRACE) && iter.hasNext()) {
                declaration(iter).also { if (it != null) add (it) }
            }
            consume(iter, TokenType.RIGHT_BRACE, "Expect '}' after block.")
        }
    }

    private fun expressionStatement(iter: TokenIterator): Stmt {
        val expr = expression(iter)
        consume(iter, TokenType.SEMICOLON, "Expect ';' after expression.")
        return Stmt.Expression(expr)
    }

    private fun expression(iter: TokenIterator): Expr {
        return assignment(iter)
    }

    private fun assignment(iter: TokenIterator): Expr {
        val expr = equality(iter)
        if (iter.match(TokenType.EQUAL)) {
            val equals = iter.previous()
            val value = assignment(iter)

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            }
            error(equals, "Invalid assignment target.")
        }
        return expr
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
            iter.match(TokenType.IDENTIFIER) -> Expr.Variable(iter.previous())
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
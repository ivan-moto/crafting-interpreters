package main

class RuntimeError(
    val token: Token,
    override val message: String,
) : RuntimeException(message)

class Environment(
    val enclosing: Environment? = null,
    val values: MutableMap<String, Any?> = mutableMapOf(),
) {

    fun get(token: Token): Any? {
        if (token.lexeme in values) return values[token.lexeme]
        if (enclosing != null) return enclosing.get(token)
        throw RuntimeError(token, "Undefined variable ${token.lexeme}.")
    }

    fun define(token: Token, value: Any?) {
        values[token.lexeme] = value
    }

    fun assign(token: Token, value: Any?) {
        if (token.lexeme in values) {
            values[token.lexeme] = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(token, value)
            return
        }
        throw RuntimeError(token, "Undefined variable ${token.lexeme}.")
    }
}

class Interpreter : Expr.Visitor<Any?>, Stmt.Visitor<Unit> {

    private var environment = Environment()

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                statement.accept(this)
            }
        } catch (e: RuntimeError) {
            runtimeError(e)
        }
    }

    private fun Any?.stringify(): String {
        if (this == null) return "nil"
        if (this is Double) {
            return this.toString().substringBeforeLast(".0")
        }
        return this.toString()
    }

    override fun visit(expr: Expr.Assign): Any? {
        val value = expr.expression.accept(this)
        environment.assign(expr.name, value)
        return value
    }

    override fun visit(expr: Expr.Binary): Any {
        val left = expr.left.accept(this)
        val right = expr.right.accept(this)
        return when (expr.operator.type) {
            TokenType.BANG_EQUAL -> {
                left != right
            }
            TokenType.EQUAL_EQUAL -> {
                left == right
            }
            TokenType.GREATER -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) >= (right as Double)
            }
            TokenType.LESS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) <= (right as Double)
            }
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) - (right as Double)
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    return left + right
                }
                if (left is String && right is String) {
                    return left + right
                }
                throw RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
            }
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) / (right as Double)
            }
            TokenType.STAR -> {
                checkNumberOperands(expr.operator, left, right)
                (left as Double) * (right as Double)
            }
            else -> error("unreachable")
        }
    }

    override fun visit(expr: Expr.Grouping): Any? {
        return expr.expression.accept(this)
    }

    override fun visit(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visit(expr: Expr.Unary): Any {
        val right = expr.right.accept(this)
        return when (expr.operator.type) {
            TokenType.MINUS -> {
                -(right as Double)
            }
            TokenType.BANG -> right.isTruthy()
            else -> error("unreachable")
        }
    }

    override fun visit(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        if (operands.any { it !is Double }) throw RuntimeError(operator, "Operand/s must be a number")
    }

    private fun Any?.isTruthy(): Boolean = this != null && (this as? Boolean) != false

    override fun visit(stmt: Stmt.Block) {
        val new = Environment(environment)
        val previous = environment
        try {
            environment = new
            for (statement in stmt.statements) {
                statement.accept(this)
            }
        } finally {
            environment = previous
        }
    }

    override fun visit(stmt: Stmt.Expression) {
        stmt.expression.accept(this)
    }

    override fun visit(stmt: Stmt.Print) {
        println(stmt.expression.accept(this).stringify())
    }

    override fun visit(stmt: Stmt.Var) {
        val value = if (stmt.initializer != null) stmt.initializer.accept(this) else null
        environment.define(stmt.name, value)
    }
}

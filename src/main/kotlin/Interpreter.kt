package main

class RuntimeError(
    val token: Token,
    override val message: String,
) : RuntimeException(message)

class Interpreter : Visitor<Any?> {

    fun interpret(expr: Expr) {
        return try {
            println(expr.accept(this).stringify())
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

    private fun checkNumberOperands(operator: Token, vararg operands: Any?) {
        if (operands.any { it !is Double }) throw RuntimeError(operator, "Operand/s must be a number")
    }

    private fun Any?.isTruthy(): Boolean = this != null && (this as? Boolean) != false
}

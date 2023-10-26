package main

interface Visitor<R> {
    fun visit(expr: Expr.Binary): R
    fun visit(expr: Expr.Grouping): R
    fun visit(expr: Expr.Literal): R
    fun visit(expr: Expr.Unary): R
}

sealed class Expr {
    abstract fun <R>accept(visitor: Visitor<R>): R

    class Binary(
        val left: Expr,
        val operator: Token,
        val right: Expr,
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    class Grouping(
        val expression: Expr,
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    class Literal(
        val value: Any?,
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    class Unary(
        val operator: Token,
        val right: Expr,
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }
}


package main

sealed class Stmt {

    interface Visitor<R> {
        fun visit(stmt: Block): R
        fun visit(stmt: Expression): R
        fun visit(stmt:Print): R
        fun visit(stmt: Var): R
    }

    abstract fun <R>accept(visitor: Visitor<R>): R

    class Block(
        val statements: List<Stmt>,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    class Expression(
        val expression: Expr,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    class Print(
        val expression: Expr,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

    class Var(
        val name: Token,
        val initializer: Expr?,
    ) : Stmt() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }
}

package main

sealed class Expr {

    interface Visitor<R> {
        fun visit(expr: Assign): R
        fun visit(expr: Binary): R
        fun visit(expr: Grouping): R
        fun visit(expr: Literal): R
        fun visit(expr: Unary): R
        fun visit(expr: Variable): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

    class Assign(
        val name: Token,
        val expression: Expr,
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }

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

    class Variable(
        val name: Token,
    ) : Expr() {
        override fun <R> accept(visitor: Visitor<R>): R = visitor.visit(this)
    }
}

sealed class Stmt {

    interface Visitor<R> {
        fun visit(stmt: Block): R
        fun visit(stmt: Expression): R
        fun visit(stmt:Print): R
        fun visit(stmt: Var): R
    }

    abstract fun <R> accept(visitor: Visitor<R>): R

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

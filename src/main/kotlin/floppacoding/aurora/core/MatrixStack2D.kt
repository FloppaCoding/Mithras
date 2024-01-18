package floppacoding.aurora.core

import org.joml.Matrix3x2f

/**
 * # A stack for local coordinate transforms in 2D
 *
 * @author Aton
 */
class MatrixStack2D {
    private val stack = ArrayDeque(listOf(Matrix3x2f()))
    private var last = stack.last()

    fun push() {
        last = Matrix3x2f(stack.last())
        stack.addLast(last)
    }

    fun pop() {
        stack.removeLast()
        last = stack.last()
    }

    fun translate(x: Float, y: Float) {
        last.translate(x, y)
    }

    fun rotate(angle: Float) {
        last.rotate(angle)
    }

    fun scale(x: Float, y: Float) {
        last.scale(x, y)
    }

    fun peek(): Matrix3x2f {
        return last
    }

    fun loadIdentity() {
        last.identity()
    }

    fun clear() {
        stack.clear()
        stack.addLast(Matrix3x2f())
    }
}
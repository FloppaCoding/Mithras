package floppacoding.ui.constraints

import floppacoding.ui.elements.Element

class Constraints(var x: Position, var y: Position, var width: Size, var height: Size)

// todo: reduce interface usages (however im not sure if its possible)
interface Constraint {
    fun get(element: Element, type: Type): Float

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
    }
}

interface Position : Constraint

interface Size : Constraint

interface Measurement : Position, Size

enum class Type {
    X, Y, W, H;

    inline val axis: Int
        get() = ordinal % 2

    inline val isPosition: Boolean
        get() = ordinal < 2
}
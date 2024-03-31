package floppacoding.ui.constraints

import floppacoding.ui.elements.Element

class Constraints(var x: Position, var y: Position, var width: Size, var height: Size)

interface Constraint  {
    fun get(element: Element, axis: Axis): Float
}

interface Position : Constraint

interface Size : Constraint

interface Measurement : Position, Size

enum class Axis {
    HORIZONTAL, VERTICAL
}
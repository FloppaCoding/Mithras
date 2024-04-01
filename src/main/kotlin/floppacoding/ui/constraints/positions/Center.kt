package floppacoding.ui.constraints.positions

import floppacoding.ui.constraints.Constraint.Companion.HORIZONTAL
import floppacoding.ui.constraints.Position
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

class Center : Position {
    override fun get(element: Element, type: Type): Float {
        val axis = type.axis
        return if (axis == HORIZONTAL) (element.parent?.width ?: 0f) / 2f - element.width / 2f
        else (element.parent?.height ?: 0f) / 2f - element.height / 2f
    }
}
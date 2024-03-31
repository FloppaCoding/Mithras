package floppacoding.ui.constraints.positions

import floppacoding.ui.constraints.Axis
import floppacoding.ui.constraints.Position
import floppacoding.ui.elements.Element

class Aligning(private val align: Align, val padding: Float = 0f) : Position {
    override fun get(element: Element, axis: Axis): Float {
        if (align == Align.START) return padding

        val value = if (axis == Axis.HORIZONTAL) element.width else element.height
        val parentValue = (if (axis == Axis.HORIZONTAL) element.parent?.width else element.parent?.height) ?: 0f
        return if (align == Align.MIDDLE) parentValue / 2f - value / 2f else parentValue - value - padding
    }
}

enum class Align {
    START, MIDDLE, END
}
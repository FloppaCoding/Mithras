package floppacoding.renameui.elements.impl

import floppacoding.renameui.constraint.*
import floppacoding.renameui.elements.Element

class Column(
    constraints: Constraints?,
    private val padding: Float = 0f
) : Element(setupConstraint(constraints)) {
    override fun draw() {
        //renderer.border(x, y, width, height, 1f, Color.WHITE.rgb)
    }

    override fun addElement(element: Element) {
        if (element.constraints.y is Placeholder) {
            val last = elements.lastOrNull { it.constraints.y is Linked }
            element.constraints.y = Linked(last, padding, true)
        }
        super.addElement(element)
    }

    private companion object {
        fun setupConstraint(constraints: Constraints?): Constraints { // i don't know if this is a good way to do this
            return (constraints ?: placeholderConstraints()).apply {
                if (width is Placeholder) width = Bounding()
                if (height is Placeholder) height = Bounding()
            }
        }
    }
}
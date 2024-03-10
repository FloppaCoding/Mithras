package floppacoding.renameui.elements.impl

import floppacoding.aurora.core.TextAlign
import floppacoding.renameui.UI
import floppacoding.renameui.constraint.Constraints
import floppacoding.renameui.constraint.Pixel
import floppacoding.renameui.constraint.placeholderConstraints
import floppacoding.renameui.elements.Element
import java.awt.Color

class Text(
    var text: String,
    constraints: Constraints? = null,
    var color: Color,
    var size: Float,
    var align: TextAlign = TextAlign.LEFT_TOP
) : Element(constraints ?: placeholderConstraints()) {

    override fun initialize(ui: UI) {
        super.initialize(ui)
        // todo set constraints based on textalign
        constraints.width = Pixel(renderer.textWidth(text, size))
        constraints.height = Pixel(size)
    }

    override fun draw() {
        renderer.text(text, x, y, color.rgb, size, textAlign = align)
    }
}
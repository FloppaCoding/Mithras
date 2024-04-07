package floppacoding.ui.elements.impl

import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.constraints.px
import floppacoding.ui.elements.Element

class Text(text: String, textColor: IColor, constraints: Constraints?, val size: Measurement) : Element(constraints) {

    var text: String = text
        set(value) {
            if (field == value) return
            field = value
            textWidth.pixels = renderer.textWidth(value, height)

        }

    private var textWidth = 0.px

    init {
        this.color = textColor
    }

    override fun draw() {
//        renderer.border(x, y, width, height, 1f, java.awt.Color.WHITE.rgb)
        renderer.text(text, x, y, color!!.rgba, height)
    }

    override fun setupSize() {
        constraints.width = textWidth
        constraints.height = size
        position()
        val amount = renderer.textWidth(text, height)
        textWidth.pixels = amount
    }
}
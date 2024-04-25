package floppacoding.ui.elements.impl

import floppacoding.ui.color.Color
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.constraints.measurements.Pixel
import floppacoding.ui.constraints.px
import floppacoding.ui.elements.Element
import floppacoding.ui.utils.replaceUndefined
import kotlin.reflect.KProperty

class Text(
    text: String,
    textColor: Color,
    constraints: Constraints?,
    val size: Measurement
) : Element(constraints.replaceUndefined(w = 0.px, h = size)) {

    var text: String = text
        set(value) {
            if (field == value) return
            field = value
            (constraints.width as Pixel).pixels = renderer.textWidth(value, height)

        }

    init {
        this.color = textColor

        onInitialization {
            position()
            (this.constraints.width as Pixel).pixels = renderer.textWidth(text, height)
        }
    }

    override fun draw() {
        renderer.text(text, x, y, color!!.rgba, height)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return text
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        text = value
    }
}
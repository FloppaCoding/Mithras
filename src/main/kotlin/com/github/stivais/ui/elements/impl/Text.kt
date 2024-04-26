package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined
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
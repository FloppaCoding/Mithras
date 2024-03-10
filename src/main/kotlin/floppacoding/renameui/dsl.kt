package floppacoding.renameui

import floppacoding.aurora.core.TextAlign
import floppacoding.renameui.constraint.*
import floppacoding.renameui.elements.Element
import floppacoding.renameui.elements.impl.Column
import floppacoding.renameui.elements.impl.Group
import floppacoding.renameui.elements.impl.Rect
import floppacoding.renameui.elements.impl.Text
import java.awt.Color

/*
*
* Ideal syntax goal
*
* Hypothetical ClickGUI implemntation (starting from panel):
*
*   column(constraint = pos(panel.x, panel.y)) {
*       button(constraint = size(120, 20)) {
*           text(panel.name).center()
*       }
*       group {
*           for (module in modules) {
*               if (module.category != panel) continue
*               column {
*                   button(constraints = size(120, 15)) {
*                       text(module.name).center()
*                   }
*                   for (setting in module.settings) {
*                       setting.createElements() // make some abstract function in setting so they handle it themselves
*                   }
*               }
*           }
*       }
*       rect(constraint = size(120, 5))
*   }
*
*   // example function in the settings for BooleanSetting (example)
*   override fun createElements(): Element {
*       rect(constraints = size(120, 15)) {
*           text(at = pos(1, 3))
*           button(constraints = constraint(100, 2, 11, 11))
*       }
*   }
*
*
*/

fun constraint(x: Number, y: Number, w: Number, h: Number) = Constraints(x.px, y.px, w.px, h.px)

fun at(x: Number, y: Number) = Constraints(x.px, y.px, Placeholder, Placeholder)

fun size(w: Number, h: Number) = Constraints(Placeholder, Placeholder, w.px, h.px)

fun <E : Element> E.center(): E {
    if (parent == null) {
        println("Parent isn't initialized")
        return this
    }
    if (this is Text) align = TextAlign.CENTER_MIDDLE // so text renders in center
    if (constraints.x is Placeholder) constraints.x = Pixel((parent!!.width / 2f) - width / 2f)
    if (constraints.y is Placeholder) constraints.y = Pixel((parent!!.height / 2f) - height / 2f)
    return this
}

fun <E : Element> E.bound(): E {
    if (constraints.width is Placeholder) constraints.width = Bounding()
    if (constraints.height is Placeholder) constraints.height = Bounding()
    return this
}

fun Element.rect(constraints: Constraints, color: Color, block: Rect.() -> Unit = {}): Rect {
    val rect = Rect(constraints, color)
    rect.block()
    addElement(rect)
    return rect
}

fun Element.text(
    text: String,
    constraints: Constraints? = null,
    color: Color = Color.WHITE,
    size: Float = 9f,
    align: TextAlign = TextAlign.LEFT_TOP
): Text {
    val text = Text(text, constraints, color, size, align)
    addElement(text)
    return text
}

fun Element.column(
    constraints: Constraints? = null,
    padding: Float = 0f,
    block: Column.() -> Unit = {}
): Column {
    return Column(constraints, padding).also {
        addElement(it)
        it.block()
    }
}

fun Element.group(constraints: Constraints? = null, block: Group.() -> Unit): Group {
    return Group(constraints).also {
        addElement(it)
        it.block()
    }
}

fun <E : Element> E.indent(amount: Number): E {
    constraints.x = Pixel(amount.toFloat())
    constraints.width = Pixel(width - amount.toFloat() * 2f)
    return this
}


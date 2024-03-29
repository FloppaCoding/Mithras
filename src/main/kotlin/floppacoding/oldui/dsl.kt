package floppacoding.oldui

import floppacoding.aurora.core.TextAlign
import floppacoding.oldui.color.Color
import floppacoding.oldui.color.IColor
import floppacoding.oldui.constraint.*
import floppacoding.oldui.elements.ElementOLD
import floppacoding.oldui.elements.impl.Column
import floppacoding.oldui.elements.impl.Group
import floppacoding.oldui.elements.impl.Rect
import floppacoding.oldui.elements.impl.Text
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

fun constraint(x: Constraint, y: Constraint, w: Constraint, h: Constraint) = Constraints(x, y, w, h)

fun at(x: Constraint, y: Constraint) = Constraints(x, y, Placeholder, Placeholder)

fun size(w: Constraint, h: Constraint) = Constraints(Placeholder, Placeholder, w, h)

fun <E : ElementOLD> E.center(): E {
    if (parent == null) {
        println("Parent isn't initialized")
        return this
    }
    //if (this is Text) align = TextAlign.CENTER_MIDDLE // so text renders in center
    if (constraints.x is Placeholder) constraints.x = Aligning(Align.MIDDLE)
    if (constraints.y is Placeholder) constraints.y = Aligning(Align.MIDDLE)
    return this
}

fun <E : ElementOLD> E.bound(): E {
    if (constraints.width is Placeholder) constraints.width = Bounding()
    if (constraints.height is Placeholder) constraints.height = Bounding()
    return this
}

fun ElementOLD.rect(constraints: Constraints? = null, color: IColor, block: Rect.() -> Unit = {}): Rect {
    val rect = Rect(constraints, color)
    rect.block()
    addElement(rect)
    return rect
}

fun ElementOLD.text(
    text: String,
    constraints: Constraints? = null,
    color: IColor = Color(255, 255, 255),
    size: Float = 9f,
    align: TextAlign = TextAlign.LEFT_TOP
): Text {
    val text = Text(text, constraints, color, size, align)
    addElement(text)
    return text
}

fun ElementOLD.column(
    constraints: Constraints? = null,
    padding: Float = 0f,
    block: Column.() -> Unit = {}
): Column {
    return Column(constraints, padding).also {
        addElement(it)
        it.block()
    }
}

fun ElementOLD.group(constraints: Constraints? = null, block: Group.() -> Unit): Group {
    return Group(constraints).also {
        addElement(it)
        it.block()
    }
}

infix fun <E : ElementOLD> E.indentWidth(amount: Number): E {
    constraints.x = Pixel(amount.toFloat())
    constraints.width = Pixel(width - amount.toFloat() * 2f)
    return this
}

fun <E : ElementOLD> E.toggle(value: Boolean = !enabled): E {
    enabled = value
    return this
}

fun <E : ElementOLD> E.copySize(): E {
    constraints.apply {
        if (width is Placeholder) width = Copy()
        if (height is Placeholder) height = Copy()
    }
    return this
}


package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.ui.elements.*
import floppacoding.ui.elements.impl.Column
import floppacoding.ui.elements.impl.Rect
import floppacoding.ui.elements.impl.Text
import floppacoding.ui.utils.radii
import org.joml.Vector4f


/*
    column(constraint(panel.x, panel.y)) {
        button(size(240.px, 40.px), defaultColor, ClickGUIColor, radii(10, 10)) {
            text(panel.name)

            onClick(1) {
                panel.extended = !panel.extended
                parent!!.elements[1].toggle()
            }
        }
        column {
            for (module in modules.filter) {
                if (module.category != panel) continue

                column(height(animatable(from = 32.px, to = bound()))) {
                    button(size(240.px, 32.px)) {
                        text(module.name)

                        onClick(0) {
                            module.toggle()
                        }
                        onClick(1) {
                            disableAllButThis() // prob better for diff implementation
                        }
                    }
                    for (setting in module.settings) {
                        setting.createElement() // handle elements in setting class itself
                    }
                }
            }
        }.scrollable().toggle(panel.extended)
        rect(240.px, 10.px)
    }.draggable()
 */





fun create(renderer2D: Renderer2D): UIV2 {
    return UIV2(renderer2D).apply {
        main.apply {
            for (category in Category.entries) {
                val panelX = category.ordinal * 260 + 20

                column(at(panelX.px, 20.px)) {
                    rect(size(240.px, 40.px), radii(tr = 5f,  tl = 5f)) {
                        text(category.name)
                    }       
                    column {
                        for (module in modules.filter { category == it.category }) {
                            rect(size(240.px, 32.px)) {
                                text(module.name)
                            }
                        }
                    }
                    rect(size(240.px, 10.px), radii(br = 5f,  bl = 5f))
                }
            }



//            column(Constraints(10.px, 10.px, Undefined, Undefined)) {
//                repeat(10) {
//                    rect(Constraints(Undefined, Undefined, 240.px, 40.px)) {
//                        text("hi", Constraints(Undefined, Undefined, Undefined, Undefined))
//                    }
//                }
//            }
        }
    }
}

fun at(x: Position, y: Position) = Constraints(x, y, Undefined, Undefined)

fun size(width: Size, height: Size) = Constraints(Undefined, Undefined, width, height)

val Number.px
    get() = Pixel(this.toFloat())

fun Element.column(constraints: Constraints? = null, block: Column.() -> Unit = {}): Column {
    val column = Column(constraints)
    addElement(column)
    column.block()
    return column
}

fun Element.rect(
    constraints: Constraints? = null,
    radii: Vector4f? = null,
    block: Rect.() -> Unit = {}
): Rect {
    val rect = Rect(constraints, radii)
    addElement(rect)
    rect.block()
    return rect
}

fun Element.text(text: String, constraints: Constraints? = null, block: Text.() -> Unit = {}): Text {
    val text = Text(text, constraints)
    addElement(text)
    text.block()
    return text
}
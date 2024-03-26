package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.ui.elements.Constraints
import floppacoding.ui.elements.ElementV2
import floppacoding.ui.elements.Pixel
import floppacoding.ui.elements.impl.Column
import floppacoding.ui.elements.impl.Rect
import floppacoding.ui.elements.impl.Text


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


val Number.px
    get() = Pixel(this.toFloat())

fun ElementV2.column(constraints: Constraints, block: Column.() -> Unit = {}): Column {
    val column = Column(constraints)
    addElement(column)
    column.block()
    return column
}

fun ElementV2.rect(constraints: Constraints, block: Rect.() -> Unit = {}): Rect {
    val rect = Rect(constraints)
    addElement(rect)
    rect.block()
    return rect
}

fun ElementV2.text(text: String, constraints: Constraints, block: Text.() -> Unit = {}): Text {
    val rect = Text(text, constraints)
    addElement(rect)
    rect.block()
    return rect
}
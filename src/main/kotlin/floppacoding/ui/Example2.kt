package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.ui.animation.Animations
import floppacoding.ui.color.AnimatedColor
import floppacoding.ui.color.Color
import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.measurements.Animatable
import floppacoding.ui.constraints.measurements.Undefined
import floppacoding.ui.constraints.sizes.Bounding
import floppacoding.ui.constraints.sizes.Copying
import floppacoding.ui.elements.Element
import floppacoding.ui.elements.impl.Block
import floppacoding.ui.elements.impl.Column
import floppacoding.ui.elements.impl.Text
import floppacoding.ui.events.onClick
import floppacoding.ui.events.onMouseEnterExit
import floppacoding.ui.utils.*
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


fun create(renderer2D: Renderer2D): UI {
    return UI(renderer2D).apply {
        main.apply {
            for (category in Category.entries) {
                val panelX = category.ordinal * 260 + 20

                column(at(panelX.px, 20.px)) {
                    val animatable = Animatable(Bounding(), 0.px)
                    block(size(240.px, 40.px), Color(26, 26, 26), radii(tl = 5, tr = 5)) {
                        text(category.name)

                        onClick(1) {
                            animatable.animate(0.5.seconds, Animations.EaseInOutQuint)
                            true
                        }
                    }
                    column(size(Undefined, animatable)) {
                        for (module in modules.filter { category == it.category }) {
                            button(size(240.px, 32.px), Color(26, 26, 26), Color(50, 150, 220), module.enabled) {
                                text(module.name)

                                onClick(0) {
                                    module.toggle()
                                    true
                                }
                            }
                        }
                    }
                    block(size(240.px, 10.px), Color(26, 26, 26), radii(br = 5, bl = 5))
                }
            }
        }
    }
}

fun Element.button(
    constraints: Constraints? = null,
    offColor: IColor,
    onColor: IColor,
    on: Boolean = false,
    dsl: Block.() -> Unit
): Block {
    val mainColor = AnimatedColor(offColor, onColor)
    val hoverColor = AnimatedColor(Color(0, 0, 0, 0f), Color(255, 255, 255, 0.05f))
    if (on) {
        mainColor.animate(0f)
    }
    return block(constraints, mainColor) {
        block(Constraints(0.px, 0.px, Copying(), Copying()), hoverColor) {
            onMouseEnterExit {
                hoverColor.animate(0.25.seconds)
                true
            }
        }
        onClick(0) {
            mainColor.animate(0.15.seconds)
            false
        }
        dsl()
    }
}

fun Element.column(constraints: Constraints? = null, block: Column.() -> Unit = {}): Column {
    val column = Column(constraints)
    addElement(column)
    column.block()
    return column
}

fun Element.block(
    constraints: Constraints? = null,
    color: IColor,
    radii: Vector4f? = null,
    block: Block.() -> Unit = {}
): Block {
    val block = Block(constraints, color, radii)
    addElement(block)
    block.block()
    return block
}

fun Element.text(
    text: String,
    color: IColor = Color(255, 255, 255),
    constraints: Constraints? = null,
    block: Text.() -> Unit = {}
): Text {
    val text = Text(text, color, constraints)
    addElement(text)
    text.block()
    return text
}
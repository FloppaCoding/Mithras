package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.ui.animation.Animations
import floppacoding.ui.color.AnimatedColor
import floppacoding.ui.color.Color
import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.*
import floppacoding.ui.constraints.measurements.Animatable
import floppacoding.ui.constraints.positions.Center
import floppacoding.ui.constraints.sizes.Bounding
import floppacoding.ui.elements.Element
import floppacoding.ui.elements.impl.Block
import floppacoding.ui.elements.impl.Column
import floppacoding.ui.elements.impl.Text
import floppacoding.ui.events.onClick
import floppacoding.ui.events.onMouseEnterExit
import floppacoding.ui.utils.animate
import floppacoding.ui.utils.radii
import floppacoding.ui.utils.seconds
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
        val extended = MainSettings.panelExtended[category]!!

        column(at(x = panelX.px, y = 20.px)) {
            block(size(w = 240.px, h = 40.px), Color(26, 26, 26), radii(tl = 5, tr = 5)) {
                text(category.name, size = 65.percent)

                onClick(1) {
                    sibling()!!.height().animate(0.5.seconds, Animations.EaseInOutQuint)
                    extended.toggle()
                    true
                }
            }
            column(Animatable(from = Bounding(), to = 0.px, swapIf = !extended.enabled).toHeight()) {
                for (module in modules.filter { category == it.category }) {

                    column(Animatable(from = 32.px, to = Bounding()).toHeight()) {
                        button(size(w = 240.px, h = 32.px), Color(26, 26, 26), on = module.enabled) {
                            text(module.name, size = 60.percent)

                            onClick(0) {
                                module.toggle()
                                true
                            }
                            onClick(1) {
                                parent!!.height().animate(0.25.seconds, Animations.EaseInOutQuint)
                                true
                            }
                        }

                        for (setting in module.settings) {
                            when (setting) {
                                is BooleanSetting -> BooleanSetting(setting)
                            }
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

fun Element.BooleanSetting(setting: BooleanSetting) =
    block(size(240.px, 32.px), Color(37, 38, 38, 0.7f)) {
        text(text = setting.name, at(6.px, Center()), size = 50.percent)

        block(constrain(-10.px, Center(), 20.px, 20.px), Color(50, 150, 220), radii(all = 5)) {
            button(copyParent(indent = 1), on = setting.enabled, radii = radii(5)) {
                onClick(0) {
                    setting.toggle()
                    true
                }
            }
        }
    }

fun Element.button(
    constraints: Constraints? = null,
    offColor: IColor = Color(38, 38, 38),
    onColor: IColor = Color(50, 150, 220),
    on: Boolean = false,
    radii: Vector4f? = radii(),
    dsl: Block.() -> Unit
): Block {
    val mainColor = AnimatedColor(offColor, onColor)
    val hoverColor = AnimatedColor(Color.TRANSPARENT, Color(255, 255, 255, 0.05f))
    if (on) mainColor.animate(0f)

    return block(constraints, mainColor, radii) {
        block(color = hoverColor, radii = radii) {
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
    at: Constraints? = null,
    size: Measurement,
    color: IColor = Color(255, 255, 255),
    block: Text.() -> Unit = {}
): Text {
    val text = Text(text, color, at, size)
    addElement(text)
    text.block()
    return text
}
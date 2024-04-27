package com.github.stivais.ui

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.*
import com.github.stivais.ui.events.*
import com.github.stivais.ui.utils.*
import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.mithras.module.impl.debug.DebugModule.guiAnimSpeedTest
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt

fun terminalTest(renderer: Renderer2D): UI {

    val array = IntArray(14) { it + 1 }.apply { shuffle() }

    return UI(renderer) {
        column {

            var current = 1

            array.forEach {
                block(
                    constraints = size(50.px, 50.px),
                    color = Color {
                        when (current - it) {
                            0 -> getRGBA(0, 255, 0, 255)
                            -1 -> getRGBA(0, 200, 0, 255)
                            -2 -> getRGBA(0, 150, 0, 255)
                            else -> getRGBA(0, 0, 0, 255)
                        }
                    }
                ) {
                    text(
                        text = "$it"
                    )

                    onClick(0) {
                        if (current == it) {
                            current++
                            //color = Color.RGB(0, 255, 0)
                        }
                        true
                    }
                }
            }
        }
    }
//            block(c(10.px, 10.px, 200.px, 200.px), Color.RGB(38, 38, 38)) {
//                addElement(
//                    TextInput("a", at(10.px, 10.px))
//                )
//            }


}


// recreating current ui doesn't allow me to test everything i want add into the ui, but i dont want to make a new one
fun create(renderer2D: Renderer2D): UI {
    return UI(renderer2D) {


        for (category in Category.entries) {
            val panelX = category.ordinal * 260 + 20
            val extended = MainSettings.panelExtended[category]!!


            //
            column(at(x = panelX.px, y = 20.px)) {
                block(size(w = 240.px, h = 40.px), Color.RGB(26, 26, 26), radii(tl = 5, tr = 5)) {
                    text(
                        text = category.name,
                        size = 65.percent
                    )
                    onClick(1) {
                        sibling()!!.height().animate(0.5.seconds * guiAnimSpeedTest, Animations.EaseInOutQuint)
                        extended.toggle()
                        true
                    }
                }.draggable(target = this /* moves this parent */)

                val modules = column(Animatable(from = Bounding, to = 0.px, swapIf = !extended.enabled).toHeight()) {
                    for (module in modules.filter { category == it.category }) {

                        column(Animatable(from = 32.px, to = Bounding).toHeight()) {
                            button(
                                constraints = size(w = 240.px, h = 32.px),
                                offColor = Color.RGB(26, 26, 26),
                                on = module.enabled
                            ) {
                                text(
                                    text = module.name,
                                    size = 60.percent
                                )
                                onClick(0) {
                                    module.toggle()
                                    true
                                }
                                onClick(1) {
                                    parent!!.height()
                                        .animate(0.25.seconds * guiAnimSpeedTest, Animations.EaseInOutQuint)
                                    true
                                }
                            }

                            for (setting in module.settings) {
                                when (setting) {
                                    is BooleanSetting -> BooleanSetting(setting)
                                    is NumberSetting<*> -> NumberSetting(setting)
                                }
                            }
                            KeybindSetting(module)
                        }

                    }
                }.background(color = Color.RGB(38, 38, 38, 0.7f))

                block(
                    constraints = size(240.px, 10.px),
                    color = Color.RGB(26, 26, 26),
                    radius = radii(br = 5, bl = 5)
                )
                scrollable(0.2.seconds, target = modules)
            }
        }


    }
}

fun Element.KeybindSetting(module: Module) =
    group(size(w = 240.px, h = 32.px)) {
        text(
            text = "Keybind",
            at(x = 6.px, y = Center),
            size = 16.px
        )
        block(
            constraints = constrain(x = -6.px, y = 6.px, w = Bounding + 6.px, h = 70.percent),
            color = Color.RGB(38, 38, 38),
            radius = radii(all = 5)
        ) {
            val display = text(
                text = module.keyBind.localizedText.string,
                size = 70.percent
            )
            onClick(null) {
                module.keyBind = InputUtil.Type.MOUSE.createFromCode(button!!)
                ui.unfocus()
                true
            }
            onKeycodePressed {
                val key = if (code == GLFW.GLFW_KEY_ESCAPE) InputUtil.UNKNOWN_KEY else InputUtil.Type.KEYSYM.createFromCode(code)
                module.keyBind = key
                ui.unfocus()
                true
            }
            onFocusGain {
                outlineColor!!.animate(0.25.seconds)
            }
            onFocusLost {
                val str = module.keyBind.localizedText.string
                display.text = str
                outlineColor!!.animate(0.25.seconds)
            }
        }.focuses().outline(color = Color.Animated(from = Color.TRANSPARENT, to = Color.RGB(50, 150, 220)))
    }

fun Element.BooleanSetting(setting: BooleanSetting) =
    group(constraints = size(240.px, 32.px)) {
        text(
            text = setting.name,
            at = at(6.px, Center),
            size = 50.percent
        )
        button(
            constraints = constrain(-10.px, Center, 20.px, 20.px),
            on = setting.enabled,
            radii = radii(all = 5)
        ) {
            onClick(0) {
                setting.toggle()
                true
            }
        }.outline(Color.RGB(50, 150, 220))
    }

fun Element.NumberSetting(setting: NumberSetting<*>) =
    group(size(240.px, 40.px)) {
        text(
            text = setting.name,
            at(x = 6.px, y = Center - 3.px),
            size = 16.px
        )
        var display by text(
            text = setting.displayValue(),
            at(x = -6.px, y = Center - 3.px),
            size = 16.px
        )
        val slider = slider(
            constraints = constrain(6.px, -5.px, 228.px, 7.px),
            value = setting.doubleValue,
            min = setting.minDouble,
            max =  setting.maxDouble,
            onChange = {
                setting.setByPercent(it)
                display = setting.displayValue()
            }
        )
        takeEvents(from = slider)
//        onClick(0, sendEventTo(slider))
//        onRelease(0) { sendEventTo(slider) }
    }

// maybe make this its own class?
fun Element.slider(
    constraints: Constraints?,
    value: Double,
    min: Double,
    max: Double,
    onChange: (percent: Float) -> Unit
): Block {
    var dragging = false
    return block(constraints, Color.RGB(-0xefeff0), radii(3)) {
        val color = Color.Animated(Color.RGB(50, 150, 220), Color.RGB(75, 175, 245))
        // temp fix until i figure out a better solution?
        val sliderAnim = Animatable.Raw(((value - min) / (max - min) * (constraints?.width?.get(this, Type.W) ?: 0f)).toFloat())
        block(c(0.px, 0.px, sliderAnim, Copying), color, radii(all = 3f))

        onClick(0) {
            val pos = (ui.eventManager!!.mouseX - x).coerceIn(0f, width)
            sliderAnim.animate(to = pos, 0.75.seconds * guiAnimSpeedTest, Animations.EaseOutQuint)
            onChange(pos / width)
            dragging = true
            true
        }
        onMouseMove {
            if (dragging) {
                val pos = (ui.eventManager!!.mouseX - x).coerceIn(0f, width)
                sliderAnim.to(pos)
                onChange(pos / width)
            }
            true
        }
        onRelease(0) {
            dragging = false
        }
        onMouseEnterExit {
            color.animate(0.25.seconds)
            true
        }
    }
}

// imagine this was inside the numbersetting class
fun NumberSetting<*>.setByPercent(percent: Float) {
    doubleValue = percent * (maxDouble - minDouble) + minDouble
}

// imagine this was inside the numbersetting class
fun NumberSetting<*>.displayValue() = "${(doubleValue * 100.0).roundToInt() / 100.0}"

fun Element.button(
    constraints: Constraints? = null,
    offColor: Color = Color.RGB(38, 38, 38),
    onColor: Color = Color.RGB(50, 150, 220),
    on: Boolean = false,
    radii: FloatArray? = null,
    dsl: Block.() -> Unit
): Block {
    val mainColor = Color.Animated(offColor, onColor, on)
    val hoverColor = Color.Animated(Color.TRANSPARENT, Color.RGB(255, 255, 255, 0.05f))

    return block(constraints, mainColor, radii) {
        block(copyParent(), color = hoverColor, radius = radii) {
            onMouseEnterExit {
                hoverColor.animate(0.25.seconds * guiAnimSpeedTest)
                true
            }
        }
        onClick(0) {
            mainColor.animate(0.15.seconds * guiAnimSpeedTest)
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

// todo: improve outline color
fun Element.block(
    constraints: Constraints? = null,
    color: Color,
    radius: FloatArray? = null,
    block: Block.() -> Unit = {}
): Block {
    val block = if (radius != null) RoundedBlock(constraints, color, radius) else Block(constraints, color)
    addElement(block)
    block.block()
    return block
}

fun Element.text(
    text: String,
    at: Constraints? = null,
    size: Measurement = 50.percent,
    color: Color = Color.WHITE,
    block: Text.() -> Unit = {}
): Text {
    val text = Text(text, color, at, size)
    addElement(text)
    text.block()
    return text
}

fun Element.group(constraints: Constraints? = null, block: Group.() -> Unit = {}): Group {
    val column = Group(constraints)
    addElement(column)
    column.block()
    return column
}

//fun Element.image(
//    image: AuroraImage,
//    constraints: Constraints? = null,
//    radius: Float = 0f,
//    block: Image.() -> Unit = {}
//): Image {
//    val block = Image(image, constraints, radius, null)
//    addElement(block)
//    block.block()
//    return block
//}
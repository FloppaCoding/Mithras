package floppacoding.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.mithras.module.impl.debug.DebugModule.guiAnimSpeedTest
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.ui.animation.Animations
import floppacoding.ui.color.AnimatedColor
import floppacoding.ui.color.Color
import floppacoding.ui.color.IColor
import floppacoding.ui.constraints.*
import floppacoding.ui.constraints.measurements.Animatable
import floppacoding.ui.constraints.positions.Center
import floppacoding.ui.constraints.sizes.Bounding
import floppacoding.ui.constraints.sizes.Copying
import floppacoding.ui.elements.Element
import floppacoding.ui.elements.impl.*
import floppacoding.ui.events.*
import floppacoding.ui.utils.*
import net.minecraft.client.util.InputUtil
import org.joml.Vector4f
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt
import floppacoding.aurora.core.images.Image as AuroraImage

fun textInput(renderer: Renderer2D): UI {
    return UI(renderer).apply {
        main.apply {
            block(c(10.px, 10.px, 200.px, 200.px), Color(38, 38, 38)) {
                addElement(
                    TextInput("a", at(10.px, 10.px))
                )
            }
        }
    }
}


// recreating current ui doesn't allow me to test everything i want add into the ui, but i dont want to make a new one
fun create(renderer2D: Renderer2D): UI {
    return UI(renderer2D).apply {
        main.apply {


    for (category in Category.entries) {
        val panelX = category.ordinal * 260 + 20
        val extended = MainSettings.panelExtended[category]!!

        column(at(x = panelX.px, y = 20.px)) {
            block(size(w = 240.px, h = 40.px), Color(26, 26, 26), radii(tl = 5, tr = 5)) {
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
                            offColor = Color(26, 26, 26),
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
                                parent!!.height().animate(0.25.seconds * guiAnimSpeedTest, Animations.EaseInOutQuint)
                                true
                            }
                        }

                        for (setting in module.settings) {
                            when (setting) {
                                is BooleanSetting -> BooleanSetting(setting)
                                is NumberSetting -> NumberSetting(setting)
                            }
                        }
                        KeybindSetting(module)
                    }

                }
            }.background(Color(38, 38, 38, 0.7f))
            block(size(240.px, 10.px), Color(26, 26, 26), radii(br = 5, bl = 5))
            scrollable(0.2.seconds, target = modules)
        }
    }


        }
    }
}

fun Element.KeybindSetting(module: Module): Block {
    val keyStr = module.keyBind.localizedText.string

    return block(size(240.px, 32.px), Color(38, 38, 38, 0f)) {
        text(text = "Keybind", at(x = 6.px, y = Center), size = 16.px)

        block(constrain(-6.px, 6.px, Bounding + 6.px, 70.percent), Color(38, 38, 38), radii(all = 5)) {
            val display = text(text = keyStr, size = 70.percent)
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
        }.focuses().outline(color = anim(from = Color.TRANSPARENT, to = Color(50, 150, 220)))
    }
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
        }.outline(Color(50, 150, 220))
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
        onClick(0, sendEventTo(slider))
        onRelease(0) { sendEventTo(slider) }
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
    return block(constraints, Color(-0xefeff0), radii(3)) {
        val color = AnimatedColor(Color(50, 150, 220), Color(75, 175, 245))
        // temp fix until i figure out a better solution?
        val sliderAnim = Animatable.Raw(((value - min) / (max - min) * (constraints?.width?.get(this, Type.W) ?: 0f)).toFloat())
        block(c(0.px, 0.px, sliderAnim, Copying()), color, radii(all = 3f))

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
    offColor: IColor = Color(38, 38, 38),
    onColor: IColor = Color(50, 150, 220),
    on: Boolean = false,
    radii: Vector4f? = radii(),
    dsl: Block.() -> Unit
): Block {
    val mainColor = AnimatedColor(offColor, onColor, on)
    val hoverColor = AnimatedColor(Color.TRANSPARENT, Color(255, 255, 255, 0.05f))
    //if (on) mainColor.animate(0f)
    return block(constraints, mainColor, radii) {
        block(color = hoverColor, radii = radii) {
            onMouseEnterExit {
                hoverColor.animate(0.25.seconds * guiAnimSpeedTest)
                true
            }
        }
        onClick(0) {
            println("hello")
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

fun Element.group(constraints: Constraints? = null, block: Group.() -> Unit = {}): Group {
    val column = Group(constraints)
    addElement(column)
    column.block()
    return column
}

fun Element.image(
    image: AuroraImage,
    constraints: Constraints? = null,
    radius: Float = 0f,
    block: Image.() -> Unit = {}
): Image {
    val block = Image(image, constraints, radius, null)
    addElement(block)
    block.block()
    return block
}
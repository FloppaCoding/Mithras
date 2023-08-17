package floppacoding.mithras.ui.hud

import floppacoding.mithras.events.HudRenderEvent
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.NumberSetting
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.DrawContext

/**
 * Provides functionality for game overlay elements.
 * @author Aton
 */
abstract class HudElement{

    private val xSett: NumberSetting<*>
    private val ySett: NumberSetting<*>
    val scale: NumberSetting<*>

    var width: Int
    var height: Int

    private val zoomIncrement = 0.05

    /**
     * Use these instead of a direct reference to the NumberSetting
     */
    var x: Int
     get() = xSett.value.toInt()
     set(value) {
         xSett.doubleValue = value.toDouble()
     }

    var y: Int
        get() = ySett.value.toInt()
        set(value) {
            ySett.doubleValue = value.toDouble()
        }

    /**
     * Sets up a hud Element.
     * This constructor takes care of creating the [NumberSetting]s required to save the position and scale of the hud
     * element to the config.
     */
    constructor(module: Module, xDefault: Int = 0, yDefault: Int = 0, width: Int = 10, height: Int = 10, defaultScale: Double = 1.0) {
        val id = module.settings.count { it.name.startsWith("xHud") }
        val xHud = NumberSetting("xHud_$id", default = xDefault.toDouble(), visibility = Visibility.HIDDEN)
        val yHud = NumberSetting("yHud_$id", default = yDefault.toDouble(), visibility = Visibility.HIDDEN)
        val scaleHud = NumberSetting("scaleHud_$id",defaultScale,0.1,4.0, 0.01, visibility = Visibility.HIDDEN)

        module.addSettings(xHud, yHud, scaleHud)

        this.xSett = xHud
        this.ySett = yHud
        this.scale = scaleHud

        this.width = width
        this.height = height
    }

    /**
     * It is advised to use the other constructor unless this one is required.
     */
    constructor(xHud: NumberSetting<*>, yHud: NumberSetting<*>, width: Int = 10, height: Int = 10, scale: NumberSetting<*>) {
        this.xSett = xHud
        this.ySett = yHud
        this.scale = scale

        this.width = width
        this.height = height
    }

    /**
     * Resets the position of this hud element by setting the value of xSett and ySett to their default.
     *
     * Can be overridden in the implementation.
     */
    open fun resetElement() {
        xSett.reset()
        ySett.reset()
        scale.reset()
    }

    /**
     * Handles scroll wheel action for this element.
     * Can be overridden in implementation.
     */
    open fun scroll(amount: Int) {
        this.scale.doubleValue += amount * zoomIncrement
    }

    /**
     * This will initiate the hud render and translate to the correct position and scale.
     */
    @EventHandler
    fun onOverlay(event: HudRenderEvent) {
        event.context.matrices.push()
        event.context.matrices.translate(x.toFloat(), y.toFloat(), 0f)
        event.context.matrices.scale(scale.value.toFloat(), scale.value.toFloat(), 1f)

        renderHud(event.context)

        event.context.matrices.pop()
    }

    /**
     * Override this method in your implementations.
     *
     * This method is responsible for rendering the HUD element.
     * Within this method coordinates are already transformed regarding to the HUD position [x],[x] and [scale].
     */
    abstract fun renderHud(context: DrawContext)

    /**
     * Used for moving the hud element.
     * Draws a rectangle in place of the actual element
     */
    fun renderPreview(context: DrawContext) {
        context.matrices.push()
        context.matrices.translate(x.toFloat(), y.toFloat(), 0f)
        context.matrices.scale(scale.value.toFloat(), scale.value.toFloat(), 1f)

        context.fill(
            0,
            0,
            width,
            height,
            -0x44eaeaeb
        )

        context.matrices.pop()
    }
}
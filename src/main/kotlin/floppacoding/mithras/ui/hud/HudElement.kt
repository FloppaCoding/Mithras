package floppacoding.mithras.ui.hud

import floppacoding.aurora.mc_modern.Renderer2DMC
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
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
abstract class HudElement  {

    private val xSett: NumberSetting<Float>
    private val ySett: NumberSetting<Float>
    val scale: NumberSetting<Float>

    var width: Float
    var height: Float

    open val renderer: Renderer2DMC
        get() = Mithras.renderer2D

    private val zoomIncrement = 0.05f

    /**
     * Use these instead of a direct reference to the NumberSetting
     */
    var x: Float
        get() = xSett.value
        set(value) { xSett.value = value }

    var y: Float
        get() = ySett.value
        set(value) { ySett.value = value }

    /**
     * Sets up a hud Element.
     * This constructor takes care of creating the [NumberSetting]s required to save the position and scale of the hud
     * element to the config.
     */
    constructor(module: Module, xDefault: Float = 0f, yDefault: Float = 0f, width: Float = 10f, height: Float = 10f, defaultScale: Float = 1.0f) {
        val id = module.settings.count { it.name.startsWith("xHud") }
        val xHud = NumberSetting("xHud_$id", default = xDefault, increment = 0.01f, visibility = Visibility.HIDDEN)
        val yHud = NumberSetting("yHud_$id", default = yDefault, increment = 0.01f, visibility = Visibility.HIDDEN)
        val scaleHud = NumberSetting("scaleHud_$id",defaultScale,0.1f,4.0f, 0.01f, visibility = Visibility.HIDDEN)

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
    constructor(xHud: NumberSetting<Float>, yHud: NumberSetting<Float>, width: Float = 10f, height: Float = 10f, scale: NumberSetting<Float>) {
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
        this.scale.value += amount * zoomIncrement
    }

    /**
     * This will initiate the hud render and translate to the correct position and scale.
     */
    @EventHandler
    fun onOverlay(event: HudRenderEvent) {
        renderer.beginFrame()
        renderer.push()
        renderer.scale(mc.options.guiScale.value.toFloat(), mc.options.guiScale.value.toFloat())
        renderer.translate(x, y)
        renderer.scale(scale.value, scale.value)

        renderHud(event.context)

        renderer.pop()
        renderer.endFrame()
    }

    /**
     * Override this method in your implementations.
     *
     * This method is responsible for rendering the HUD element.
     * Within this method coordinates are already transformed in regard to the HUD position [x],[y] and [scale].
     * You can use [renderer] for nice rendering, the vanilla context is also available.
     * So the vanilla rendering can be used as well. Note that the vanilla context is **NOT** properly transformed.
     * You can transform the vanilla context to the same coordinates by surrounding your rendering in:
     *
     *      context.matrices.push()
     *      context.matrices.translate(x,y,0f)
     *      context.matrices.scale(scale.value, scale.value, 1f)
     *
     *      // Your rendering
     *
     *      context.matrices.pop()
     * You might also need to adjust for the GUI scale before the translation.
     */
    protected abstract fun renderHud(context: DrawContext)

    /**
     * Used for moving the hud element.
     * Draws a rectangle in place of the actual element
     */
    fun renderPreview() {
        renderer.push()
        renderer.translate(x, y)
        renderer.scale(scale.value, scale.value)

        drawPreview()

        renderer.pop()
    }

    /**
     * Draws a box with the dimensions of the hud element as a preview.
     *
     * If a custom preview is desired this can be overridden.
     */
    protected open fun drawPreview() {
        renderer.rect(0f, 0f, width, height, -0x44eaeaeb)
    }

    companion object {
        val DEFAULT_RENDERER: Renderer2DMC
            get() = Mithras.renderer2D
    }
}
package floppacoding.mithras.ui.nanovg

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.ui.nanovg.NVGR.beginFrame
import floppacoding.mithras.ui.nanovg.NVGR.endFrame
import floppacoding.mithras.utils.Utils.seconds
import floppacoding.mithras.utils.clock.Clock
import floppacoding.mithras.utils.clock.Executor
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.text.Text

/**
 * ### Parent class for GUI screens using the [NanoVG rendering library][NVGR].
 *
 * @param scale scale of the coordinate system in relation to the screen.
 * At scale = 2f a coordinate change of 1 will equal 2 pixels.
 *
 * @author Aton, Stivais
 */
abstract class NVGScreen(
    title: Text,
    private val scale: Float = 1f
) : Screen(title) {

    constructor(title: String, scale: Float = 1f) : this(MutableText.of(LiteralTextContent(title)), scale)

    private val clock = Clock()

    /**
     * If this is false it will render FPS in bottom-right corner.
     */
    open val displayPerformance: Boolean = false

    /** Used to show performance*/
    private var frames = 0

    /** Used to show performance */
    private var performance: String = ""

    /** Used to update fps */
    private val perfUpdater = Executor(1.seconds) {
        performance = "FPS : $frames, Frametime : ${clock.getTime() / 1000_000f}ms" // not avg frame time cuz too lazy for that
        frames = 0
    }

    val windowWidth: Float
        get() = mc.window.width / scale

    val windowHeight: Float
        get() = mc.window.height / scale

    /**
     * Sets up the frame and scaling.
     */
    final override fun render(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        clock.update()
        beginFrame()
        NVGR.scale(scale, scale)
        render(mc.mouse.x / scale, mc.mouse.y / scale, partialTicks)
        if (displayPerformance) {
            displayPerformance()
        }
        endFrame()
        super.render(context, mouseX, mouseY, partialTicks)
    }

    /**
     * Defaults this screen to not pause the game.
     */
    override fun shouldPause(): Boolean {
        return false
    }

    /**
     * Renders the GUI.
     *
     * Override this with your rendering code.
     * The frame is already set up and the mouse and rendering coordinates scaled to match the [scale] and be equal.
     */
    abstract fun render(mouseX: Double, mouseY: Double, delta: Float)

    private fun displayPerformance() {
        frames++
        perfUpdater.run()
        NVGR.text(performance, mc.window.width - 2f, mc.window.height - 2f, 16f, -1, textAlign = TextAlign.RIGHT)
    }

    fun getMouseX(): Double = mc.mouse.x / scale

    fun getMouseY(): Double = mc.mouse.y / scale
}
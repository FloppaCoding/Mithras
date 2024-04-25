package floppacoding.mithras.ui

import floppacoding.aurora.core.Renderer2D
import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.utils.clock.Clock
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.text.Text

/**
 * ### Parent class for GUI screens using the [NanoVG rendering library][renderer].
 *
 * All the methods from the superclass [Screen] which contain mouse coordinates are overridden and replaced with
 * variants which receive the correct mouse coordinates for the NanoVG coordinate space.
 *
 * @param scale scale of the coordinate system in relation to the screen.
 * At scale = 2f a coordinate change of 1 will equal 2 pixels.
 *
 * @author Aton, Stivais
 */
abstract class GuiScreen(
    title: Text,
    var scale: Float = 1f
) : Screen(title) {

    constructor(title: String, scale: Float = 1f) : this(MutableText.of(LiteralTextContent(title)), scale)

    open val renderer: Renderer2D
        get() = Mithras.renderer2D

    private val clock = Clock()

    /**
     * If this is false it will render FPS in bottom-right corner.
     */
    protected open val displayPerformance: Boolean = false

//    /** Used to show performance*/
//    private var frames = 0
//
//    /** Used to show performance */
//    private var performance: String = ""
//
//    /** Used to update fps */
//    private val perfUpdater = Executor(1.seconds) {
//        performance = "FPS : $frames, Frametime : ${clock.getTime() / 1000_000f}ms" // not avg frame time cuz too lazy for that
//        frames = 0
//    }

    // frametime metrics
    private var frames: Int = 0
    private var frameTime: Long = 0
    private var performance: String = ""

    val windowWidth: Float
        get() = mc.window.width / scale

    val windowHeight: Float
        get() = mc.window.height / scale

    /**
     * Sets up the frame and scaling.
     */
    final override fun render(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val start = System.nanoTime()
        renderer.beginFrame()
        renderer.text(performance, Mithras.mc.window.width - 2f, Mithras.mc.window.height - 2f, -1, 16f, textAlign = TextAlign.RIGHT_BOTTOM)
        renderer.scale(scale, scale)
        renderer.push()
        render(getMouseX(), getMouseY(), partialTicks)
        renderer.pop()
        if (displayPerformance) {
            displayPerformance()
        }
        renderer.endFrame()
        frames++
        frameTime += System.nanoTime() - start
        if (frames > 100) {
            performance = "frametime avg: ${(frameTime / frames) / 1_000_000.0}ms"
            frames = 0
            frameTime = 0
        }
        super.render(context, mouseX, mouseY, partialTicks)
    }

    /**
     * Renders the GUI.
     *
     * Override this with your rendering code.
     * The frame is already set up and the mouse and rendering coordinates scaled to match the [scale] and be equal.
     */
    protected abstract fun render(mouseX: Float, mouseY: Float, delta: Float)

    final override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseClicked(getMouseX(), getMouseY(), button)) return true

        return super.mouseClicked(mouseX, mouseY, button)
    }

    protected open fun mouseClicked(mouseX: Float, mouseY: Float, button: Int) : Boolean { return false }

    final override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (mouseReleased(getMouseX(), getMouseY(), button)) return true

        return super.mouseReleased(mouseX, mouseY, button)
    }

    protected open fun mouseReleased(mouseX: Float, mouseY: Float, button: Int) : Boolean { return false }

    final override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        if (mouseScrolled(getMouseX(), getMouseY(), amount.toFloat())) return true

        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    protected open fun mouseScrolled(mouseX: Float, mouseY: Float, amount: Float): Boolean { return false }

    final override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        val scaledDeltaX = (deltaX * client!!.window.width / client!!.window.scaledWidth / scale).toFloat()
        val scaledDeltaY = (deltaX * client!!.window.height / client!!.window.scaledHeight / scale).toFloat()

        if (mouseDragged(getMouseX(), getMouseY(), button, scaledDeltaX, scaledDeltaY)) return true


        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    protected open fun mouseDragged(mouseX: Float, mouseY: Float, button: Int, deltaX: Float, deltaY: Float): Boolean { return false }

    final override fun mouseMoved(mouseX: Double, mouseY: Double) {
        mouseMoved(getMouseX(), getMouseY())
        super.mouseMoved(mouseX, mouseY)
    }

    protected open fun mouseMoved(mouseX: Float, mouseY: Float) {}

    /**
     * Defaults this screen to not pause the game.
     */
    override fun shouldPause(): Boolean {
        return false
    }

    private fun displayPerformance() {
//        frames++
//        perfUpdater.run()
//        renderer.push()
//        renderer.reset()
//        renderer.text(performance, mc.window.width - 2f, mc.window.height - 2f, -1, 16f, textAlign = TextAlign.RIGHT_BOTTOM)
//        renderer.pop()
    }

    fun getMouseX(): Float = mc.mouse.x.toFloat() / scale

    fun getMouseY(): Float = mc.mouse.y.toFloat() / scale
}
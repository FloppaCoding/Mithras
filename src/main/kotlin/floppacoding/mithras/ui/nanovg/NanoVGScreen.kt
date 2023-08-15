package floppacoding.mithras.ui.nanovg

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.ui.nanovg.NVGR.beginFrame
import floppacoding.mithras.ui.nanovg.NVGR.endFrame
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
 * @author Aton
 */
abstract class NanoVGScreen(
    title: Text,
    private val scale: Float = 1f
) : Screen(title) {

    constructor(title: String, scale: Float = 1f) : this(MutableText.of(LiteralTextContent(title)), scale)

    /**
     * Sets up the frame and scaling.
     */
    final override fun render(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        beginFrame()
        NVGR.scale(scale, scale)
        render(mc.mouse.x / scale, mc.mouse.y / scale, partialTicks)
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
    abstract fun render(mouseX: Double, mouseY: Double, partialTicks: Float)
}
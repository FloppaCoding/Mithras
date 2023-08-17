package floppacoding.mithras.ui.nanovg

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras
import floppacoding.mithras.ui.nanovg.NVGR.beginFrame
import floppacoding.mithras.ui.nanovg.NVGR.endFrame
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3

/**
 * # NanoVG Renderer - 2D Rendering Library
 *
 * This library uses nanoVG to greatly simplify rendering 2D elements for GUIs and HUDs.
 * It cannot be used for 3D rendering.
 * Note also that all the methods in here are independent of the usual modifiers in the game's rendering matrix stack.
 *
 * ## Usage
 * All rendering related instructions from this library have to be placed in between [beginFrame] and [endFrame].
 * The coordinate system has its origin in the top left corner of the screen with x going to the right and y towards the bottom.
 * The coordinates scale 1 to 1 to pixels on the screen.
 *
 * ## State changes
 * NanoVG disables the depth test, this is not compensated for.
 *
 * @author Aton
 */
@Suppress("unused")
object NVGR {
    val nanoContext: Long = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS)

    private val nanoColor: NVGColor = NVGColor.calloc()

    /**
     * Begins drawing a new frame.
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    fun beginFrame() {
        nvgBeginFrame(
            nanoContext,
            Mithras.mc.window.width.toFloat(),
            Mithras.mc.window.height.toFloat(),
            1f
        )
        RenderSystem.disableCull()
    }


    /**
     * Ends drawing the frame.
     *
     * All rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    fun endFrame() {
        nvgEndFrame(nanoContext)
    }

    /**
     * Translates the origin of the current coordinate system.
     */
    fun translate(x: Float, y: Float) = nvgTranslate(nanoContext, x, y)

    /**
     * Scales the current coordinate system.
     */
    fun scale(x: Float, y: Float) = nvgScale(nanoContext, x, y)

    /**
     * Pushes the current rendering state to a stack.
     * [pop] must be used to restore that state.
     */
    fun push() = nvgSave(nanoContext)

    /**
     * Restores the previous rendering state.
     */
    fun pop() = nvgRestore(nanoContext)

    /**
     * Draws a line from point 1 to point 2.
     * @param capStyle can be [NVG_ROUND] or [NVG_SQUARE]
     */
    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int, capStyle: Int = NVG_ROUND) {
        nvgBeginPath(nanoContext)
        nvgStrokeWidth(nanoContext, width)
        strokeColor(color)
        nvgLineCap(nanoContext, capStyle)
        nvgMoveTo(nanoContext, x1, y1)
        nvgLineTo(nanoContext, x2, y2)
        nvgStroke(nanoContext)
        nvgClosePath(nanoContext)
    }

    /**
     * Draws a rectangle with the given dimensions and color.
     */
    fun rect(x: Float, y: Float, width: Float, height: Float, color: Int) {
        nvgBeginPath(nanoContext)
        nvgRect(nanoContext, x, y, width, height)
        fillColor(color)
        nvgFill(nanoContext)
    }

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext, x, y, width, height, radius)
        fillColor(color)
        nvgFill(nanoContext)
    }

    /**
     * Renders text aligned with the left bottom corner to the given coordinates.
     */
    fun text(
        text: String,
        x: Float,
        y: Float,
        fontSize: Float,
        color: Int,
        font: NVGFontManager.Font = NVGFontManager.ROBOTO,
        textAlign: TextAlign = TextAlign.LEFT
    ) {
        nvgBeginPath(nanoContext)
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        nvgTextAlign(nanoContext, textAlign.nvg)
        fillColor(color)
        nvgText(nanoContext, x, y, text)
    }

    /**
     * Sets up a scissor rectangle.
     *
     */
    fun scissor(x: Float, y: Float, width: Float, height: Float) = nvgScissor(nanoContext, x, y, width, height)

    /**
     * Disables scissoring.
     */
    fun endScissor() = nvgResetScissor(nanoContext)

    /**
     * Sets fill style for [nvgFill] to the specified color.
     */
    private fun fillColor(color: Int) {
        updateColor(color)
        nvgFillColor(nanoContext ,nanoColor)
    }

    /**
     * Sets stroke style for [nvgStroke] to the specified color.
     */
    private fun strokeColor(color: Int) {
        updateColor(color)
        nvgStrokeColor(nanoContext ,nanoColor)
    }

    private fun updateColor(color: Int) = nvgRGBA(
            (color shr 16 and 0xFF).toByte(),
            (color shr 8 and 0xFF).toByte(),
            (color and 0xFF).toByte(),
            (color shr 24 and 0xFF).toByte(),
            nanoColor
        )

    enum class TextAlign(
        val nvg: Int
    ) {
        LEFT(NVG_ALIGN_LEFT),
        RIGHT(NVG_ALIGN_RIGHT),
        MIDDLE(NVG_ALIGN_MIDDLE)
    }
}

typealias TextAlign = NVGR.TextAlign
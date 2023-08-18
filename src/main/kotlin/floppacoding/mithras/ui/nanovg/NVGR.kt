package floppacoding.mithras.ui.nanovg

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.ui.nanovg.NVGR.beginFrame
import floppacoding.mithras.ui.nanovg.NVGR.endFrame
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3
import java.nio.FloatBuffer

typealias TextAlign = NVGR.TextAlign

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

    /**
     * Variables for storing temporary draw style data.
     */
    private val nanoColor: NVGColor = NVGColor.calloc()
    private val imgPaint: NVGPaint = NVGPaint.calloc()

    /**
     * Begins drawing a new frame.
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    fun beginFrame() {
        nvgBeginFrame(
            nanoContext,
            mc.window.width.toFloat(),
            mc.window.height.toFloat(),
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
     * Translates the origin of the current coordinate system.
     */
    fun translate(x: Double, y: Double) = nvgTranslate(nanoContext, x.toFloat(), y.toFloat())

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
        setStrokeColor(color)
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
        setFillColor(color)
        nvgFill(nanoContext)
    }

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext, x, y, width, height, radius)
        setFillColor(color)
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
        font: NVGFont = NVGFontManager.ROBOTO,
        textAlign: TextAlign = TextAlign.LEFT
    ) {
        nvgBeginPath(nanoContext)
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        nvgTextAlign(nanoContext, textAlign.nvg)
        setFillColor(color)
        nvgText(nanoContext, x, y, text)
    }

    /**
     * Returns the width of the given [text].
     */
    fun textWidth(text: String, fontSize: Float, font: NVGFont = NVGFontManager.ROBOTO): Float {
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
//        nvgTextAlign(nanoContext, TextAlign.LEFT.nvg)
//        val buffer = ByteBuffer.allocateDirect(4*4).asFloatBuffer()
        return nvgTextBounds(nanoContext, 0f, 0f, text, null as FloatBuffer?)
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun image(image: NVGImage, x: Float, y: Float, width: Float, height: Float, radius: Float = 0f, alpha: Float = 1f) {
        nvgImagePattern(nanoContext, 0f, 0f, width, height, 0f, image.id, alpha, imgPaint)
        push()
        translate(x, y)
        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext,0f, 0f, width, height, radius)
        nvgFillPaint(nanoContext, imgPaint)
        nvgFill(nanoContext)
        pop()
    }

    /**
     * Draws a chroma border with rounded corner and the given dimensions.
     */
    fun chromaBorder(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float) {
        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext,x, y, width, height, radius)
        strokeWithChroma(lineWidth)
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
    private fun setFillColor(color: Int) {
        updateColor(color)
        nvgFillColor(nanoContext, nanoColor)
    }

    /**
     * Fills the current path with the given color.
     */
    fun fillWithColor(color: Int) {
        setFillColor(color)
        nvgFill(nanoContext)
    }

    /**
     * Sets stroke style for [nvgStroke] to the specified color.
     */
    private fun setStrokeColor(color: Int) {
        updateColor(color)
        nvgStrokeColor(nanoContext, nanoColor)
    }

    /**
     * Strokes the current path with the given color.
     */
    fun strokeWithColor(color: Int) {
        setStrokeColor(color)
        nvgStroke(nanoContext)
    }

    /**
     * Sets [imgPaint] to the currently selected chroma pattern.
     */
    private fun chromaPattern() {
        val size = 4000f / MainSettings.chromaSize.coerceAtLeast(0.01f)

        val offset: Float = if(MainSettings.chromaSpeed < 1e-2) {
            0f
        }else {
            val period = (1000 / MainSettings.chromaSpeed).toInt()
            val time = System.currentTimeMillis().mod(period)
            time / period.toFloat() * size / 20f
        }
        nvgImagePattern(nanoContext, -offset, 0f, size, 10f, 0f, NVGImageManager.CHROMA.id, 1f, imgPaint)
    }

    /**
     * Sets [imgPaint] to the chroma pattern and sets up the coordinate transform for the chroma angle.
     *
     * This has to appear in between [push] and [pop] or things will break.
     */
    private fun setupChroma() {
        chromaPattern()
        nvgReset(nanoContext)
        val angle: Float = MainSettings.chromaAngle
        when (angle) {
            in 0f..90f -> {} // chroma goes i direction of top left corner
            in 90f..180f -> nvgTranslate(nanoContext, mc.window.width.toFloat(), 0f) // top right corner
            in 180f..270f -> nvgTranslate(nanoContext, mc.window.width.toFloat(), mc.window.height.toFloat()) // botom right corner
            in 270f..360f -> nvgTranslate(nanoContext, 0f, mc.window.height.toFloat()) // bottom left corner
        }
        nvgRotate(nanoContext, nvgDegToRad(angle))
    }

    /**
     * Fills the current path with the chroma pattern
     */
    fun fillWithChroma() {
        push()
        setupChroma()
        nvgFillPaint(nanoContext, imgPaint)
        nvgFill(nanoContext)
        pop()
    }

    /**
     * Strokes the current path with the chroma pattern.
     */
    fun strokeWithChroma(lineWidth: Float) {
        push()
        setupChroma()
        nvgStrokePaint(nanoContext, imgPaint)
        nvgStrokeWidth(nanoContext, lineWidth)
        nvgStroke(nanoContext)
        pop()
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
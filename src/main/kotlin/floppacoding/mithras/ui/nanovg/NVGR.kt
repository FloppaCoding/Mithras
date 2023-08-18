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
 * ## Some tips
 * There are some NonoVG methods which use Buffers for returning data.
 * You have to be very careful with how you initialize those buffers, or you will hard crash the Java Runtime Environment.
 * A try-catch clause will not be able to catch that. Or you might get unusable results.
 *
 * To work properly, the buffer has to be a direct buffer, so that NanoVG can write to it. Also, the byte order has to be
 * correct. The following example will create a FloatBuffer that works with NanoVG methods.
 *
 * @author Aton
 */
@Suppress("unused")
object NVGR {
    val nanoContext: Long = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS)

    /**
     * The font height that will be used for text rendering unless specified otherwise.
     */
    const val DEFAULT_FONT_HEIGHT = 9f

    /**
     * Variables for storing temporary draw style data.
     */
    private val nanoColor: NVGColor = NVGColor.calloc()
    private val nanoColor2: NVGColor = NVGColor.calloc()
    private val nanoPaint: NVGPaint = NVGPaint.calloc()

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
     * @param text The text to be rendered.
     * @param x Yhe text x-coordinate.
     * @param y Yhe text y-coordinate.
     * @param color The text color.
     * @param fontSize Height for the letters.
     * @param font Font to use.
     * @param textAlign The align type for the text.
     * @param splitWidth The width at which the test will be split into a new line. If this value is null, the text will
     * not be split.
     */
    fun text(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        fontSize: Float = DEFAULT_FONT_HEIGHT,
        font: NVGFont = NVGFontManager.ROBOTO,
        textAlign: TextAlign = TextAlign.LEFT
        textAlign: TextAlign = TextAlign.TOP_LEFT,
        splitWidth: Float? = null
    ) {
        nvgBeginPath(nanoContext)
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        nvgTextAlign(nanoContext, textAlign.nvg)
        setFillColor(color)
        if (splitWidth == null) {
            nvgText(nanoContext, x, y, text)
        }else {
            nvgTextBox(nanoContext, x, y, splitWidth, text)
        }
    }

    /**
     * Returns the width of the given [text].
     */
    fun textWidth(text: String, fontSize: Float = DEFAULT_FONT_HEIGHT, font: NVGFont = NVGFontManager.ROBOTO): Float {
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        return nvgTextBounds(nanoContext, 0f, 0f, text, null as FloatBuffer?)
    }

    /**
     * Returns the bounding box of the given [text] if it were drawn at 0,0 in the current coordinate system.
     * @param width If width is null then the text will be considered as one line. Otherwise
     */
    fun textBounds(text: String, width: Float? = null, fontSize: Float = DEFAULT_FONT_HEIGHT, font: NVGFont = NVGFontManager.ROBOTO) : BoundingBox {
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        val buffer = ByteBuffer.allocateDirect(4 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        if (width == null) {
            nvgTextBounds(nanoContext, 0f, 0f, text, buffer)
        }else {
            nvgTextBoxBounds(nanoContext, 0f, 0f, width, text, buffer)
        }
        return BoundingBox(buffer[0], buffer[1], buffer[2], buffer[3])
    }

    /**
     * Draws the [image] at [x],[y].
     * If [width] and [height] don't match the images aspect ratio, the image will get stretched accordingly.
     * @param radius radius of the corner radius.
     */
    fun image(image: NVGImage, x: Float, y: Float, width: Float, height: Float, radius: Float = 0f, alpha: Float = 1f) {
        nvgImagePattern(nanoContext, 0f, 0f, width, height, 0f, image.id, alpha, nanoPaint)
        push()
        translate(x, y)
        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext,0f, 0f, width, height, radius)
        nvgFillPaint(nanoContext, nanoPaint)
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
     * Sets [nanoPaint] to the currently selected chroma pattern.
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
        nvgImagePattern(nanoContext, -offset, 0f, size, 10f, 0f, NVGImageManager.CHROMA.id, 1f, nanoPaint)
    }

    /**
     * Sets [nanoPaint] to the chroma pattern and sets up the coordinate transform for the chroma angle.
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
        nvgFillPaint(nanoContext, nanoPaint)
        nvgFill(nanoContext)
        pop()
    }

    /**
     * Strokes the current path with the chroma pattern.
     */
    fun strokeWithChroma(lineWidth: Float) {
        push()
        setupChroma()
        nvgStrokePaint(nanoContext, nanoPaint)
        nvgStrokeWidth(nanoContext, lineWidth)
        nvgStroke(nanoContext)
        pop()
    }

    private fun updateColor(color: Int, result: NVGColor = nanoColor) = nvgRGBA(
            (color shr 16 and 0xFF).toByte(),
            (color shr 8 and 0xFF).toByte(),
            (color and 0xFF).toByte(),
            (color shr 24 and 0xFF).toByte(),
            result
        )

    class BoundingBox(var xmin: Float, var ymin: Float, var xmax: Float, var ymax: Float) {
        fun width(): Float = xmax - xmin

        fun height() : Float = ymax - ymin
    }

    enum class TextAlign(
        val nvg: Int
    ) {
        TOP_LEFT(NVG_ALIGN_LEFT or NVG_ALIGN_TOP),
        BOTTOM_LEFT(NVG_ALIGN_LEFT or NVG_ALIGN_BOTTOM),
        BOTTOM_RIGHT(NVG_ALIGN_BOTTOM or NVG_ALIGN_RIGHT),
        TOP_RIGHT(NVG_ALIGN_TOP or NVG_ALIGN_RIGHT),
        CENTER_BOTTOM(NVG_ALIGN_BOTTOM or NVG_ALIGN_CENTER),
        CENTER_MIDDLE(NVG_ALIGN_MIDDLE or NVG_ALIGN_CENTER),
        CENTER_TOP(NVG_ALIGN_TOP or NVG_ALIGN_CENTER),
        LEFT_MIDDLE(NVG_ALIGN_MIDDLE or NVG_ALIGN_LEFT),
        RIGHT_MIDDLE(NVG_ALIGN_MIDDLE or NVG_ALIGN_RIGHT),
        LEFT(NVG_ALIGN_LEFT),
        RIGHT(NVG_ALIGN_RIGHT),
        MIDDLE(NVG_ALIGN_MIDDLE)
    }
}
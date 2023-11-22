package floppacoding.mithras.utils.render.nanovg

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.aurora.core.BoundingBox
import floppacoding.aurora.core.CapStyle
import floppacoding.aurora.core.TextAlign
import floppacoding.aurora.core.font.Font
import floppacoding.aurora.core.images.Image
import floppacoding.aurora.mc_modern.Renderer2DMC
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.utils.render.nanovg.NVGR.beginFrame
import floppacoding.mithras.utils.render.nanovg.NVGR.endFrame
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import org.joml.Vector2f
import org.joml.Vector4f
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG.*
import org.lwjgl.nanovg.NanoVGGL3
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.atan2


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
object NVGR : Renderer2DMC {
    val nanoContext: Long = NanoVGGL3.nvgCreate(NanoVGGL3.NVG_ANTIALIAS)
    private val mc = MinecraftClient.getInstance()


    override val defaultFont: Font = NVGFontManager.ROBOTO

    /**
     * Variables for storing temporary draw style data.
     */
    private val nanoColor: NVGColor = NVGColor.calloc()
    private val nanoColor2: NVGColor = NVGColor.calloc()
    private val nanoPaint: NVGPaint = NVGPaint.calloc()

    private var oldCull: Boolean = true
    private var oldSrcAlpha: Int = 0
    private var oldBlend: Boolean = true
    private var oldProgramm: Int = 0
    private var oldCullMode: Int = 0
    private var oldFrontFace: Int = 0
    private var oldDepth: Boolean = true
    private var oldStencilMask: Int = -1
    private var oldStencilFunc: Int = 0
    private var oldStencilRef: Int = 0
    private var oldStencilValueMask: Int = 0
    private var oldTexture: Int = GL20.GL_TEXTURE0
    private var oldUniformBuffer = 0
    private var oldArrayBuffer = 0
    private var oldTexture2D = 0

    /**
     * Begins drawing a new frame.
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    override fun beginFrame() {
//        saveCurrentState()
        nvgBeginFrame(nanoContext, mc.window.width.toFloat(), mc.window.height.toFloat(), 1f)
        //Cull has to be disabled, otherwise sprites (grass, etc.) will be rendered incorrectly.
        RenderSystem.disableCull()
//        RenderSystem.disableBlend() // Do not disable blend or thing might not show up correctly
    }

    override fun setTransform(context: DrawContext) {
        val posMat  = context.matrices.peek().positionMatrix
        nvgReset(nanoContext)
        nvgScale(nanoContext, mc.window.scaleFactor.toFloat(), mc.window.scaleFactor.toFloat())
        nvgTransform(nanoContext, posMat.m00(), posMat.m01(), posMat.m10(), posMat.m11(), posMat.m30(), posMat.m31())
    }

    /**
     * Ends drawing the frame.
     *
     * All rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    override fun endFrame() {
        nvgEndFrame(nanoContext)

//        restoreOldState()
    }

    override fun cancelFrame() {
        nvgCancelFrame(nanoContext)
    }

    override fun reset() {
        nvgReset(nanoContext)
    }

    /**
     * Saves the current rendering attributes.
     * THis should cover pretty much every state which may be changed by NanoVG.
     */
    private fun saveCurrentState() {
        oldCull     = GL11.glGetBoolean(GL20.GL_CULL_FACE)
        oldSrcAlpha = GL11.glGetInteger(GL20.GL_BLEND_SRC_ALPHA)
        oldBlend    = GL11.glGetBoolean(GL20.GL_BLEND)
        oldProgramm = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)
        oldCullMode = GL11.glGetInteger(GL20.GL_CULL_FACE_MODE)
        oldFrontFace = GL11.glGetInteger(GL20.GL_FRONT_FACE)
        oldDepth = GL11.glGetBoolean(GL20.GL_DEPTH_TEST)
        // NOT checking
        // glDisable(GL_SCISSOR_TEST);
        //	glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        oldStencilMask = GL11.glGetInteger(GL20.GL_STENCIL_WRITEMASK)
        // Skipping glStencilOP
        oldStencilFunc = GL11.glGetInteger(GL20.GL_STENCIL_FUNC)
        oldStencilRef = GL11.glGetInteger(GL20.GL_STENCIL_REF)
        oldStencilValueMask = GL11.glGetInteger(GL20.GL_STENCIL_VALUE_MASK)
        oldTexture = GL11.glGetInteger(GL20.GL_ACTIVE_TEXTURE)
        oldUniformBuffer = GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_BINDING)
        // Skipping glBindVertexArray
        oldArrayBuffer = GL11.glGetInteger(GL31.GL_ARRAY_BUFFER_BINDING)
        oldTexture2D = GL11.glGetInteger(GL20.GL_TEXTURE_BINDING_2D)
    }

    /**
     * Restores the states set in [saveCurrentState].
     */
    private fun restoreOldState() {
        if (oldCull) GL11.glEnable(GL20.GL_CULL_FACE) else GL11.glDisable(GL20.GL_CULL_FACE)
        GL11.glBlendFunc(GL20.GL_SRC_ALPHA, oldSrcAlpha)
        if (oldBlend) GL11.glEnable(GL20.GL_BLEND) else GL11.glDisable(GL20.GL_BLEND)
        GL20.glUseProgram(oldProgramm)
        GL11.glCullFace(oldCullMode)
        GL11.glFrontFace(oldFrontFace)
        if (oldDepth) GL11.glEnable(GL20.GL_DEPTH_TEST) else GL11.glDisable(GL20.GL_DEPTH_TEST)
        GL11.glStencilMask(oldStencilMask)
        GL11.glStencilFunc(oldStencilFunc, oldStencilRef, oldStencilValueMask)
        GL13.glActiveTexture(oldTexture)
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, oldUniformBuffer)
        GL15.glBindBuffer(GL31.GL_ARRAY_BUFFER, oldArrayBuffer)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture2D)
    }

    /**
     * Translates the origin of the current coordinate system.
     */
    override fun translate(x: Float, y: Float) = nvgTranslate(nanoContext, x, y)

    /**
     * Scales the current coordinate system.
     */
    override fun scale(x: Float, y: Float) = nvgScale(nanoContext, x, y)

    /**
     * Rotates by the given [angle] in degrees.
     */
    override fun rotate(angle: Float) = nvgRotate(nanoContext, nvgDegToRad( angle ))

    override fun rotateRadians(angle: Float) = nvgRotate(nanoContext, angle)

    /**
     * Pushes the current rendering state to a stack.
     * [pop] must be used to restore that state.
     */
    override fun push() = nvgSave(nanoContext)

    /**
     * Restores the previous rendering state.
     */
    override fun pop() = nvgRestore(nanoContext)

    /**
     * Draws a line from point 1 to point 2.
     * @param capStyle can be [NVG_ROUND] or [NVG_SQUARE]
     */
    override fun line(x0: Float, y0: Float, x1: Float, y1: Float, width: Float, color: Int, capStyle: CapStyle) {
        nvgBeginPath(nanoContext)
        nvgStrokeWidth(nanoContext, width)
        setStrokeColor(color)
        nvgLineCap(nanoContext, capStyle.id)
        nvgMoveTo(nanoContext, x0, y0)
        nvgLineTo(nanoContext, x1, y1)
        nvgStroke(nanoContext)
        nvgClosePath(nanoContext)
    }

    /**
     * Draws a rectangle with the given dimensions and color.
     */
    override fun rect(x: Float, y: Float, width: Float, height: Float, color: Int) {
        nvgBeginPath(nanoContext)
        nvgRect(nanoContext, x, y, width, height)
        setFillColor(color)
        nvgFill(nanoContext)
    }

    /**
     * Draws a rectangle with rounded corners.
     */
    override fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext, x, y, width, height, radius)
        setFillColor(color)
        nvgFill(nanoContext)
    }

    /**
     * Draws a rectangle with rounded corners.
     */
    override fun roundedRect(x: Float, y: Float, width: Float, height: Float, radii: Vector4f, color: Int) {
        nvgBeginPath(nanoContext)
        nvgRoundedRectVarying(nanoContext, x, y, width, height, radii.x, radii.w, radii.z, radii.y)
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
     * not be split. If this value is set, the alignment will be relative to a box from [x],[y] to [x]+[splitWidth],[y]+hieght.
     */
    override fun text(
        text: CharSequence,
        x: Float,
        y: Float,
        color: Int,
        fontSize: Float,
        font: Font,
        textAlign: TextAlign,
        splitWidth: Float?
    ) {
        if (font !is NVGFontManager.NVGFont) throw Error("Invalid Font")
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

    override fun textBox(text: CharSequence, x: Float, y: Float, color: Int, width: Float, fontSize: Float, font: Font, textAlign: TextAlign, boxAlign: TextAlign) {
        if (font !is NVGFontManager.NVGFont) throw Error("Invalid Font")
        nvgBeginPath(nanoContext)
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        nvgTextAlign(nanoContext, textAlign.nvg)
        setFillColor(color)

        val buffer = MemoryUtil.memAllocFloat(4)
        nvgTextBoxBounds(nanoContext, 0f, 0f, width, text, buffer)
        val yShift = when(boxAlign.vertical) {
            TextAlign.Vertical.TOP -> 0f
            TextAlign.Vertical.MIDDLE -> -(buffer[3]- buffer[1]) / 2
            TextAlign.Vertical.BOTTOM -> -buffer[3]- buffer[1]
            TextAlign.Vertical.BASELINE -> -buffer[3]- buffer[1]
        }
        val xShift = when(boxAlign.horizontal) {
            TextAlign.Horizontal.LEFT -> 0f
            TextAlign.Horizontal.CENTER -> - width /2
            TextAlign.Horizontal.RIGHT -> -width
        }

        nvgTextBox(nanoContext, x + xShift, y + yShift, width, text)
        MemoryUtil.memFree(buffer)

    }

    override fun textLine(text: CharSequence, x: Float, y: Float, color: Int, fontSize: Float, font: Font, textAlign: TextAlign) {
        if (font !is NVGFontManager.NVGFont) throw Error("Invalid Font")
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
    override fun textWidth(text: CharSequence, fontSize: Float, font: Font): Float {
        if (font !is NVGFontManager.NVGFont) throw Error("Invalid Font")
        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        return nvgTextBounds(nanoContext, 0f, 0f, text, null as FloatBuffer?)
    }

    /**
     * Returns the bounding box of the given [text] if it were drawn at 0,0 in the current coordinate system.
     * @param width If width is null then the text will be considered as one line. Otherwise
     */
    override fun textBounds(text: CharSequence, width: Float?, fontSize: Float, font: Font) : BoundingBox {
        if (font !is NVGFontManager.NVGFont) throw Error("Invalid Font")
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
    override fun roundedImage(image: Image, x: Float, y: Float, width: Float, height: Float, radius: Float, imageX: Float, imageY: Float, imageWidth: Float, imageHeight: Float, alpha: Float) {
        if (image !is NVGImage) return
        val xScale = width / imageWidth
        val yScale = height / imageHeight
        val scaledImageWidth = image.width * xScale
        val scaledImageHeight = image.height * yScale
        val scaledImageX = imageX * xScale
        val scaledImageY = imageY * yScale
        nvgImagePattern(nanoContext, -scaledImageX, -scaledImageY, scaledImageWidth, scaledImageHeight, 0f, image.id, alpha, nanoPaint)
        push()
        translate(x, y)
        nvgBeginPath(nanoContext)
        if (radius > 0f) {
            nvgRoundedRect(nanoContext, 0f, 0f, width, height, radius)
        }else {
            nvgRect(nanoContext, 0f, 0f, width, height)
        }
        nvgFillPaint(nanoContext, nanoPaint)
        nvgFill(nanoContext)
        pop()
    }

    /**
     * Draws a chroma border with rounded corner and the given dimensions.
     * @param color does nothing but gives this method the same signature as [border], so that both can be used with the
     * same syntax through a KFunction.
     */
    override fun chromaBorder(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int) {
        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext,x, y, width, height, radius)
        strokeWithChroma(lineWidth)
    }

    override fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int) {
        nvgBeginPath(nanoContext)
        if (radius == 0f) {
            nvgRect(nanoContext, x, y, width, height)
        } else {
            nvgRoundedRect(nanoContext, x, y, width, height, radius)
        }
        strokeWithColor(lineWidth, color)
    }

    override fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radii: Vector4f?, color: Int) {
        nvgBeginPath(nanoContext)
        if(radii == null || radii.x == 0f && radii.y == 0f && radii.z == 0f && radii.w == 0f) {
            nvgRect(nanoContext, x, y, width, height)
        }else {
            nvgRoundedRectVarying(nanoContext, x, y, width, height, radii.x, radii.w, radii.z, radii.y)
        }
        strokeWithColor(lineWidth, color)
    }

    override fun textField(text: String,
                           x: Float,
                           y: Float,
                           width: Float,
                           color: Int,
                           fontSize: Float,
                           radius: Float,
                           font: Font
    ) {
        if (font !is NVGFontManager.NVGFont) throw Error("Invalid Font")
        val height = fontSize* 1.5f
        nvgBeginPath(nanoContext)
        nvgRGBA(255.toByte(), 255.toByte(),255.toByte(), 32.toByte(), nanoColor)
        nvgRGBA(23.toByte(), 32.toByte(),32.toByte(), 32.toByte(), nanoColor2)
        nvgBoxGradient(nanoContext, x +1f, y+1f, width - 2f, height-4f, radius, 4f, nanoColor, nanoColor2, nanoPaint)
        nvgRoundedRect(nanoContext, x+ 1f, y+1f, width-2f, height-4f, radius)
        nvgFillPaint(nanoContext, nanoPaint)
        nvgFill(nanoContext)

        nvgBeginPath(nanoContext)
        nvgRoundedRect(nanoContext, x+0.5f, y+0.5f, width-1f, height -1f, (radius-1f).coerceAtLeast(0f))
        nvgRGBA( 0.toByte(), 0.toByte(),0.toByte(), 48.toByte(), nanoColor)
        nvgStrokeColor(nanoContext, nanoColor)
        nvgStroke(nanoContext)

        nvgFontSize(nanoContext, fontSize)
        nvgFontFaceId(nanoContext, font.id)
        nvgTextAlign(nanoContext, NVG_ALIGN_LEFT or NVG_ALIGN_MIDDLE)
        setFillColor(color)
        nvgText(nanoContext, x + height * 0.3f, y + height*0.5f, text)
    }

    override fun circle(x: Float, y: Float, radius: Float, color: Int) {
        nvgBeginPath(nanoContext)
        nvgCircle(nanoContext, x, y, radius)
        setFillColor(color)
        nvgFill(nanoContext)
    }

    override fun ellipse(x: Float, y: Float, a: Vector2f, b: Float, color: Int) {
        nvgBeginPath(nanoContext)
        push()
        translate(x, y)
        rotateRadians(atan2(a.y, a.x))
        nvgEllipse(nanoContext, 0f, 0f, a.length(), b)
        setFillColor(color)
        nvgFill(nanoContext)
        pop()
    }

    /**
     * Sets up a scissor rectangle.
     *
     */
    override fun scissor(x: Float, y: Float, width: Float, height: Float) = nvgScissor(nanoContext, x, y, width, height)

    /**
     * Disables scissoring.
     */
    override fun endScissor() = nvgResetScissor(nanoContext)

    /**
     * Sets fill style for [nvgFill] to the specified color.
     */
    fun setFillColor(color: Int) {
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
    fun strokeWithColor(width: Float, color: Int) {
        setStrokeColor(color)
        nvgStrokeWidth(nanoContext, width)
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

    fun updateColor(color: Int, result: NVGColor = nanoColor) = nvgRGBA(
            (color shr 16 and 0xFF).toByte(),
            (color shr 8 and 0xFF).toByte(),
            (color and 0xFF).toByte(),
            (color shr 24 and 0xFF).toByte(),
            result
        )

    private val TextAlign.nvg: Int
        get() = when(this) {
            TextAlign.LEFT_TOP -> NVG_ALIGN_LEFT or NVG_ALIGN_TOP
            TextAlign.LEFT_BOTTOM -> NVG_ALIGN_LEFT or NVG_ALIGN_BOTTOM
            TextAlign.RIGHT_BOTTOM -> NVG_ALIGN_BOTTOM or NVG_ALIGN_RIGHT
            TextAlign.RIGHT_TOP -> NVG_ALIGN_TOP or NVG_ALIGN_RIGHT
            TextAlign.CENTER_BOTTOM -> NVG_ALIGN_BOTTOM or NVG_ALIGN_CENTER
            TextAlign.CENTER_MIDDLE -> NVG_ALIGN_MIDDLE or NVG_ALIGN_CENTER
            TextAlign.CENTER_TOP -> NVG_ALIGN_TOP or NVG_ALIGN_CENTER
            TextAlign.LEFT_MIDDLE -> NVG_ALIGN_MIDDLE or NVG_ALIGN_LEFT
            TextAlign.RIGHT_MIDDLE -> NVG_ALIGN_MIDDLE or NVG_ALIGN_RIGHT
            TextAlign.LEFT_BASELINE -> NVG_ALIGN_LEFT or NVG_ALIGN_BASELINE
            TextAlign.CENTER_BASELINE -> NVG_ALIGN_CENTER or NVG_ALIGN_BASELINE
            TextAlign.RIGHT_BASELINE -> NVG_ALIGN_RIGHT or NVG_ALIGN_BASELINE
        }
}
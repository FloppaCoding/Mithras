package floppacoding.mithras.utils.render

import com.google.common.collect.ImmutableMap
import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.shaders.impl.*
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormatElement
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis
import org.joml.*
import org.lwjgl.opengl.GL46
import java.awt.Color
import kotlin.math.round

// TODO consider not using the position matrix on the cpu when creating vertices and instead let the model view matrix handle that.
//   so instead of using VertexConsumer.vertex(matrix4f, x, y, z) using VertexConsumer(x,y,z)
//   and letting all transformation methods act on RenderSystem.getModelViewMatrix instead.
//   Or maybe even use a new matrix stack,  to prevent compatibility issues.
//
// TODO also consider adding option for the antialising, to disable / enable it or to change the samples.
//
// TODO also consider not using other global shader settings like line witdh.
//   pro of using global:  the result is consistent with vanilla rendering
//   con the state of vanilla rendering is changed.
//
//   Doing so is more efficient since it reduces teh cpu load. (might not be relevant tho)
//   Also consider better buffering so that everything which uses the same shader gets drawn at once.

object GLR: Renderer2D {

    private var matrices: MatrixStack = MatrixStack()
    val projectionMatrix: Matrix4f = Matrix4f().setOrtho(0.0f, 1920f, 1080f, 0.0f, 1000.0f, 21000.0f)

    private val msaaBuffer: MSAAFramebuffer = MSAAFramebuffer.getInstance(8)
    private val mainBuffer: Framebuffer = MinecraftClient.getInstance().framebuffer

    private val mc = MinecraftClient.getInstance()
    override val defaultFont: Font
        get() = TODO("Not yet implemented")

    override fun beginFrame() {
        this.matrices = MatrixStack()
        projectionMatrix.setOrtho(0.0f, mc.window.framebufferWidth.toFloat(), mc.window.framebufferHeight.toFloat(), 0.0f, 1000.0f, 21000.0f)
        msaaBuffer.useBuffer(mainBuffer)
    }

    override fun beginFrame(context: DrawContext) {
        beginFrame()
        setTransform(context)
    }

    override fun setTransform(context: DrawContext) {
        matrices.loadIdentity()
        matrices.scale(mc.window.scaleFactor.toFloat(), mc.window.scaleFactor.toFloat(), 1f)
        matrices.peek().positionMatrix.mul(context.matrices.peek().positionMatrix)
        matrices.peek().normalMatrix.mul(context.matrices.peek().normalMatrix)
    }

    override fun endFrame() {
        msaaBuffer.endUsingBuffer(mainBuffer)
    }

    override fun reset() {
        matrices.loadIdentity()
    }

    /**
     * Translates the origin of the current coordinate system.
     */
    override fun translate(x: Float, y: Float) = matrices.translate(x, y, 0f)

    /**
     * Translates the origin of the current coordinate system.
     */
    override fun translate(x: Double, y: Double) = matrices.translate(x, y, 0.0)

    /**
     * Scales the current coordinate system.
     */
    override fun scale(x: Float, y: Float) = matrices.scale(x, y, 1f)

    /**
     * Rotates clockwise by the given [angle] in degrees.
     */
    override fun rotate(angle: Float) = matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle))

    /**
     * Pushes the current rendering state to a stack.
     * [pop] must be used to restore that state.
     */
    override fun push() = matrices.push()

    /**
     * Restores the previous rendering state.
     */
    override fun pop() = matrices.pop()

    override fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float, color: Int, capStyle: CapStyle) {
        RenderSystem.assertOnRenderThread()
        RenderSystem.enableBlend()
        RenderSystem.lineWidth(width)
        Lines.setLinesMode()
        Lines.setCapStyle(capStyle)

        val positionMatrix = matrices.peek().positionMatrix
        val normalMatrix = matrices.peek().normalMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES)

        val lineNormal = Vector3f(x2-x1, y2-y1,0f).mul(normalMatrix).normalize()

        bufferBuilder.vertex(positionMatrix, x1, y1, 0f).color(color).normal(lineNormal.x, lineNormal.y, 0f).next()
        bufferBuilder.vertex(positionMatrix, x2, y2, 0f).color(color).normal(lineNormal.x, lineNormal.y, 0f).next()

        val builtBuffer = bufferBuilder.end()

        Lines.useShader()
        BufferRenderer.draw(builtBuffer)
        Lines.stopShader()
    }

    /**
     * Sets up a scissor rectangle.
     *
     * The coordinates are assumed to be in the current coordinate space and are transformed accordingly.
     * The scissor rectangle will be aligned with the screen coordinate system and will be the bounding box of the given possibly
     * rotated rectangle. If the axis of the current coordinate system are not aligned with screen coordinates the scissor
     * will set up a rectangle *ABCD* as shown in the following example.
     *
     *         A      (x+width,y)  B
     *          ┌─────────────╳───┐
     *          │      __──‾‾  ╲  │
     *    (x,y) │__──‾‾          ╲│ (x+width,y+height)
     *          │╲          __──‾‾│
     *          │  ╲  __──‾‾      │
     *          └───╳─────────────┘
     *         D    (x,y+height)   C
     *
     *
     */
    override fun scissor(x: Float, y: Float, width: Float, height: Float) {
        val bbox = getAbsoluteBoundingBox(x, y, width, height)

        RenderSystem.enableScissor(
            round(bbox.xmin).toInt(),
            round(mc.window.height - bbox.ymax).toInt(),
            round(bbox.width()).toInt(),
            round(bbox.height()).toInt()
        )
    }

    /**
     * Disables scissoring.
     */
    override fun endScissor() = RenderSystem.disableScissor()

    /**
     * Draws a rectangle with rounded corners.
     */
    override fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        RenderSystem.assertOnRenderThread()

        RenderSystem.enableBlend()

        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)

        bufferBuilder.vertex(positionMatrix, x,             y,        0f).color(color).texture(0f, 0f).next()
        bufferBuilder.vertex(positionMatrix, x,          y+height, 0f).color(color).texture(0f, height).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).color(color).texture(width, height).next()
        bufferBuilder.vertex(positionMatrix, x+width,    y,        0f).color(color).texture(width, 0f).next()

        RoundedRectangle.setDimensions(width, height)
        RoundedRectangle.setRadius(radius)

        RoundedRectangle.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        RoundedRectangle.stopShader()
    }

    // TODO this is a test method, remove it!
    fun roundedRect2(x: Float, y: Float, width: Float, height: Float, radii: Vector4f, color: Int) {
        RenderSystem.assertOnRenderThread()

        RenderSystem.enableBlend()

        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        bufferBuilder.vertex(positionMatrix, x,             y,        0f).color(Color(255,0,0, 100).rgb).next()
        bufferBuilder.vertex(positionMatrix, x,          y+height, 0f).color(Color(0,255,0,100).rgb).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).color(Color(0,0,255,100).rgb).next()
        bufferBuilder.vertex(positionMatrix, x+width,    y,        0f).color(Color(255,255,0,100).rgb).next()

        RoundedRectangle2.setRadii(radii)
        RoundedRectangle2.setTransform(positionMatrix)

        RoundedRectangle2.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        RoundedRectangle2.stopShader()
    }

    override fun text(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        fontSize: Float,
        font: Font,
        textAlign: TextAlign,
        splitWidth: Float?
    ) {
        TODO("Not yet implemented")
    }

    override fun textWidth(text: String, fontSize: Float, font: Font): Float {
        TODO("Not yet implemented")
    }

    override fun textBounds(text: String, width: Float?, fontSize: Float, font: Font): BoundingBox {
        TODO("Not yet implemented")
    }

    override fun image(
        image: Image,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        imageX: Float,
        imageY: Float,
        imageWidth: Float,
        imageHeight: Float,
        alpha: Float
    ) {
        RenderSystem.assertOnRenderThread()
        if (image !is GLImageManager.GLImage) return

        val u1 = imageX / image.width
        val u2 = u1 + imageWidth / image.width
        var v1 = imageY / image.height
        var v2 = v1 + imageHeight / image.height
        if (image.flags.contains(Image.Flags.FLIPY)) {
            v1 = 1-v1
            v2 = 1-v2
        }


        RenderSystem.enableBlend()
        GL46.glActiveTexture(GL46.GL_TEXTURE0)
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, image.id)
        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE)

        bufferBuilder.vertex(positionMatrix, x,             y,        0f).texture(u1, v1).next()
        bufferBuilder.vertex(positionMatrix, x,          y+height, 0f).texture(u1, v2).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).texture(u2, v2).next()
        bufferBuilder.vertex(positionMatrix, x+width,    y,        0f).texture(u2, v1).next()

        Texture.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        Texture.stopShader()
    }

    override fun chromaBorder(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        lineWidth: Float,
        radius: Float,
        color: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun border(x: Float, y: Float, width: Float, height: Float, lineWidth: Float, radius: Float, color: Int) {
        TODO("Not yet implemented")
    }

    override fun textField(
        text: String,
        x: Float,
        y: Float,
        width: Float,
        color: Int,
        fontSize: Float,
        radius: Float,
        font: Font
    ) {
        TODO("Not yet implemented")
    }

    override fun rect(x: Float, y: Float, width: Float, height: Float, color: Int) {
        RenderSystem.assertOnRenderThread()

        RenderSystem.enableBlend()

        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

        bufferBuilder.vertex(positionMatrix, x,             y,        0f).color(color).next()
        bufferBuilder.vertex(positionMatrix, x,          y+height, 0f).color(color).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).color(color).next()
        bufferBuilder.vertex(positionMatrix, x+width,    y,        0f).color(color).next()

        GUIShader.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        GUIShader.stopShader()
    }

    fun circle(x: Float, y: Float, radius: Float, color: Int) {
        ellipse(x, y, Vector2f(radius, 0f), radius, color)
    }

    /**
     * Draws an ellipse centered at [[x],[y]] with semi-axes [a] and [b].
     *
     * @param a Is one of the semi-axes of the ellipse and determines the orientation of the ellipse.
     * @param b Is the length of the other semi-axis of the ellipse. It is oriented internally.
     */
    fun ellipse(x: Float, y: Float, a: Vector2f, b: Float, color: Int) {
        RenderSystem.assertOnRenderThread()

        val posMat = matrices.peek().positionMatrix
        val transform = Matrix2f(posMat.m00(), posMat.m10(), posMat.m01(), posMat.m11())

        val aVec = a.mul(transform)
        val bVec = Vector2f(-a.y, a.x).normalize(b).mul(transform)
        RenderSystem.enableBlend()

        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        bufferBuilder.begin(POINTS, POSITION_COLOR_TEX_TEX)

        bufferBuilder.vertex(posMat, x, y, 0f).color(color).texture(aVec.x, aVec.y).texture(bVec.x, bVec.y).next()
//        bufferBuilder.vertex(positionMatrix, x, y, 0f).color(color).texture(aVec.x, aVec.y).texture(bVec.x, bVec.y).next()

        Ellipse.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        Ellipse.stopShader()
    }

    val POSITION_COLOR_TEX_TEX = VertexFormat(ImmutableMap.builder<String, VertexFormatElement>().put("Position", VertexFormats.POSITION_ELEMENT).put("Color", VertexFormats.COLOR_ELEMENT).put("UV0", VertexFormats.TEXTURE_ELEMENT).put("UV1", VertexFormats.TEXTURE_ELEMENT).build())
    val POINTS = VertexFormat.DrawMode.valueOf("POINTS")

    /**
     * Returns the bounding box of the given rectangle in screen coordinates to be used with the Scissor test.
     *
     * The given input coordinates are assumed to span a rectangle in the current coordinate system.
     * The returned bounding box is axis aligned with the window coordinate system.
     *
     */
    private fun getAbsoluteBoundingBox(x: Float, y: Float, width: Float, height: Float): BoundingBox {
        val mat = matrices.peek().positionMatrix
        val transform = Matrix3f(mat).m20(mat.m30()).m21(mat.m31()).m22(1f)//.m02(0f).m12(0f) // These values will anyway be discarded.

        val corner = Vector3f()

        corner.set(x, y, 1f).mul(transform)
        val boundingBox = BoundingBox(corner.x, corner.y, corner.x, corner.y)

        listOf(0 to 1, 1 to 1, 1 to 0).forEach {
            corner.set(x+width*it.first, y + height * it.second, 1f).mul(transform)

            if (corner.x < boundingBox.xmin) boundingBox.xmin = corner.x
            else if (corner.x > boundingBox.xmax) boundingBox.xmax = corner.x
            if (corner.y < boundingBox.ymin) boundingBox.ymin = corner.y
            else if (corner.y > boundingBox.ymax) boundingBox.ymax = corner.y
        }

        return boundingBox
    }
}
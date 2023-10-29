package floppacoding.mithras.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.shaders.impl.GUIShader
import floppacoding.mithras.shaders.impl.RoundedRectangle
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.Framebuffer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.math.round

// TODO consider not using the position matrix on the cpu when creating vertices and instead let the model view matrix handle that.
//   so instead of using VertexConsumer.vertex(matrix4f, x, y, z) using VertexConsumer(x,y,z)
//   and letting all transformation methods act on RenderSystem.getModelViewMatrix instead.
//   Or maybe even use a new matrix stack,  to prevent compatibility issues.
//
// TODO also consider adding option for the antialising, to disable / enable it or to change the samples.
//
//
//   Doing so is more efficient since it reduces teh cpu load. (might not be relevant tho)
//   Also consider better buffering so that everything which uses the same shader gets drawn at once.

object GLR {

    private var matrices: MatrixStack = MatrixStack()
    val projectionMatrix: Matrix4f = Matrix4f().setOrtho(0.0f, 1920f, 1080f, 0.0f, 1000.0f, 21000.0f)

    private val msaaBuffer: MSAAFramebuffer = MSAAFramebuffer.getInstance(8)
    private val mainBuffer: Framebuffer = MinecraftClient.getInstance().framebuffer

    private val mc = MinecraftClient.getInstance()

    fun beginFrame() {
        this.matrices = MatrixStack()
        projectionMatrix.setOrtho(0.0f, mc.window.framebufferWidth.toFloat(), mc.window.framebufferHeight.toFloat(), 0.0f, 1000.0f, 21000.0f)
        msaaBuffer.useBuffer(mainBuffer)
    }

    fun beginFrame(context: DrawContext) {
        beginFrame()
        setTransform(context)
    }

    fun setTransform(context: DrawContext) {
        matrices.loadIdentity()
        matrices.scale(mc.window.scaleFactor.toFloat(), mc.window.scaleFactor.toFloat(), 0f)
        matrices.peek().positionMatrix.mul(context.matrices.peek().positionMatrix)
        matrices.peek().normalMatrix.mul(context.matrices.peek().normalMatrix)
    }

    fun endFrame() {
        msaaBuffer.endUsingBuffer(mainBuffer)
    }

    /**
     * Translates the origin of the current coordinate system.
     */
    fun translate(x: Float, y: Float) = matrices.translate(x, y, 0f)

    /**
     * Translates the origin of the current coordinate system.
     */
    fun translate(x: Double, y: Double) = matrices.translate(x, y, 0.0)

    /**
     * Scales the current coordinate system.
     */
    fun scale(x: Float, y: Float) = matrices.scale(x, y, 1f)

    /**
     * Rotates clockwise by the given [angle] in degrees.
     */
    fun rotate(angle: Float) = matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle))

    /**
     * Pushes the current rendering state to a stack.
     * [pop] must be used to restore that state.
     */
    fun push() = matrices.push()

    /**
     * Restores the previous rendering state.
     */
    fun pop() = matrices.pop()

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
    fun scissor(x: Float, y: Float, width: Float, height: Float) {
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
    fun endScissor() = RenderSystem.disableScissor()

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
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

    fun rect(x: Float, y: Float, width: Float, height: Float, color: Int) {
        RenderSystem.assertOnRenderThread()

        RenderSystem.enableBlend()

        val positionMatrix = matrices.peek().positionMatrix
        val bufferBuilder = RenderSystem.renderThreadTesselator().buffer
        // For some stupid reason this just does not work with the POSITION_COLOR vertexformat.
        // But it does work with POOSITION_COLOR_TEXTURE.
        // I have no idea why that would be, since it works fine for DrawContext.fill.
        // I tried to use the same buffer and shader as that uses, but it always results in open gl errors and I have no
        // idea why. My best guess at this point is that it is some weird bug.
        // TODO FIX this without losing your sanity!
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)

        bufferBuilder.vertex(positionMatrix, x,             y,        0f).color(color).texture(0f, 0f).next()
        bufferBuilder.vertex(positionMatrix, x,          y+height, 0f).color(color).texture(0f, height).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).color(color).texture(width, height).next()
        bufferBuilder.vertex(positionMatrix, x+width,    y,        0f).color(color).texture(width, 0f).next()

        GUIShader.useShader()
        BufferRenderer.draw(bufferBuilder.end())
        GUIShader.stopShader()
    }

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
package floppacoding.mithras.utils.render

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras
import floppacoding.mithras.shaders.impl.RoundedRectangle
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.RotationAxis

object GLR {

    private var matrices: MatrixStack = MatrixStack()

    fun beginDraw(matrices: MatrixStack) {
        this.matrices = matrices
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
     */
    fun scissor(x: Float, y: Float, width: Float, height: Float, scale: Double = Mithras.mc.window.scaleFactor) = RenderSystem.enableScissor(
        (x * scale).toInt(),
        (Mithras.mc.window.height - (height + y) *scale).toInt(),
        (width*scale).toInt(),
        (height * scale).toInt()
    )

    /**
     * Disables scissoring.
     */
    fun endScissor() = RenderSystem.disableScissor()

    /**
     * Draws a rectangle with rounded corners.
     */
    fun roundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Int) {
        RenderSystem.assertOnRenderThread()
        val positionMatrix = matrices.peek().positionMatrix
        val tessellator = RenderSystem.renderThreadTesselator()
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE)

        bufferBuilder.vertex(positionMatrix, x,             y,        0f).color(color).texture(0f, 0f).next()
        bufferBuilder.vertex(positionMatrix, x,          y+height, 0f).color(color).texture(0f, height).next()
        bufferBuilder.vertex(positionMatrix, x+width, y+height, 0f).color(color).texture(width, height).next()
        bufferBuilder.vertex(positionMatrix, x+width,    y,        0f).color(color).texture(width, 0f).next()

        RoundedRectangle.setDimensions(width, height)
        RoundedRectangle.setRadius(radius)

        RoundedRectangle.useShader()
        RenderSystem.enableBlend()
        BufferRenderer.draw(tessellator.buffer.end())
        RoundedRectangle.stopShader()
    }


}
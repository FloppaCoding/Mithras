package floppacoding.mithras.ui.other

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.shaders.impl.FractalShader
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.BufferRenderer
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.text.Text
import kotlin.math.pow

object FractalScreen : Screen(Text.literal("Fractal")) {

    val DEFAULT_WIDTH = 4f

    var corner_x = -2f
    var corner_y = -1.2f

    var fractal_width = DEFAULT_WIDTH

    override fun init() {
        FractalShader.setCorner(corner_x, corner_y)
        FractalShader.setWidth(fractal_width)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val positionMatrix = context.matrices.peek().positionMatrix
        val tessellator = RenderSystem.renderThreadTesselator()
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        bufferBuilder.vertex(positionMatrix, 0f, 0f, 0f).next()
        bufferBuilder.vertex(positionMatrix, 0f, height.toFloat(), 0f).next()
        bufferBuilder.vertex(positionMatrix, width.toFloat(), height.toFloat(), 0f).next()
        bufferBuilder.vertex(positionMatrix, width.toFloat(),  0f, 0f).next()

        FractalShader.useShader()
        BufferRenderer.draw(tessellator.buffer.end())
        FractalShader.stopShader()
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        val zoom = 1.3f.pow(-amount.toFloat())

        corner_x -= fractal_width*(zoom - 1) * mouseX.toFloat()/width
        corner_y -= fractal_width*(zoom - 1) * mouseY.toFloat()/width

        fractal_width *= zoom

        FractalShader.setCorner(corner_x, corner_y)
        FractalShader.setWidth(fractal_width)
        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (button != 0) return false

        corner_x -= (deltaX / width * fractal_width).toFloat()
        corner_y -= (deltaY / width * fractal_width).toFloat()

        FractalShader.setCorner(corner_x, corner_y)

        return true
    }

}
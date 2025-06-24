package floppacoding.mithras.ui.other

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.vertex.VertexFormat
import floppacoding.mithras.shaders.impl.FractalShader
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase.LineWidth
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.*
import kotlin.math.pow


object FractalScreen : Screen(Text.literal("Fractal")) {

    val DEFAULT_WIDTH = 4f

    var corner_x = -2f
    var corner_y = -1.2f

    var fractal_width = DEFAULT_WIDTH

    private val FRACTAL_LAYER: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POST_EFFECT_PROCESSOR_SNIPPET)
            .withLocation("pipeline/fractal_layer")
            .withVertexShader(Identifier.of("mithras", "shaders/fractal/fractal.vert"))
            .withFragmentShader(Identifier.of("mithras", "shaders/fractal/fractal.frag"))
            .withCull(true)
            .withoutBlend()
            .build()
    )

    private val renderLayer: RenderLayer = RenderLayer.of(
        "fractal_layer",
        1536,  // Not sure how this should be calculated (i used the same as the other built-in render layers.
        FRACTAL_LAYER,
        RenderLayer.MultiPhaseParameters.builder().lineWidth(LineWidth(OptionalDouble.of(1.0))).build(false)
    )

    override fun init() {
        FractalShader.setCorner(corner_x, corner_y)
        FractalShader.setWidth(fractal_width)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        val positionMatrix = context.matrices.peek().positionMatrix
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION)

        bufferBuilder.vertex(positionMatrix, 0f, 0f, 0f)
        bufferBuilder.vertex(positionMatrix, 0f, height.toFloat(), 0f)
        bufferBuilder.vertex(positionMatrix, width.toFloat(), height.toFloat(), 0f)
        bufferBuilder.vertex(positionMatrix, width.toFloat(),  0f, 0f)

        FractalShader.useShader()
        renderLayer.draw(bufferBuilder.end())
        FractalShader.stopShader()
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double, verticalAmount: Double): Boolean {
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
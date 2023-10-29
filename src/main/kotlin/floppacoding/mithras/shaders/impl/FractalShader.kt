package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.shaders.uniforms.impl.Uniform2f
import net.minecraft.client.render.VertexFormats
import org.joml.Vector2f

object FractalShader : Shader("fractal/fractal", VertexFormats.POSITION_COLOR_TEXTURE) {
    private val width = Uniform1f(this.programID, "width")
    private val corner = Uniform2f(this.programID, "corner")

    fun setWidth(width: Float) {
        this.width.updateValue(width)
    }
    fun setCorner(x: Float, y: Float) {
        corner.updateValue(Vector2f(x, y))
    }

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.windowSize,
            width,
            corner
        )

        width.updateValue(4f)
        corner.updateValue(Vector2f(-2f, -2f))
    }
}
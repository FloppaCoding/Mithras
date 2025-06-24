package floppacoding.mithras.shaders.impl

import floppacoding.aurora.core.shader.Shader
import floppacoding.aurora.core.shader.uniforms.impl.Uniform1f
import floppacoding.aurora.core.shader.uniforms.impl.Uniform2f
import floppacoding.mithras.Mithras
import org.joml.Vector2f

object FractalShader : Shader(
    listOf("Position", "Color", "UV0"),
    "/assets/${Mithras.RESOURCE_DOMAIN}/shaders/",
    "fractal/fractal.vert",
    "fractal/fractal.frag"
) {
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
            this.projectionMat,
            this.windowSize,
            width,
            corner
        )

        width.updateValue(4f)
        corner.updateValue(Vector2f(-2f, -2f))
    }
}
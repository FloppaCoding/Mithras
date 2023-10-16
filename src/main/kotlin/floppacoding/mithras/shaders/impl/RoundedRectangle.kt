package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Uniform2f
import floppacoding.mithras.shaders.uniforms.impl.Uniform4f
import net.minecraft.client.render.VertexFormats
import org.joml.Vector2f
import org.joml.Vector4f

object RoundedRectangle : Shader("rounded_rect/rounded_rect", VertexFormats.POSITION_COLOR_TEXTURE) {

    private var cornerRadii: Vector4f = Vector4f(0f,0f,0f,0f)

    private var boxSize: Vector2f = Vector2f(10f, 10f)

    private val radiusUniform = Uniform4f(this.programID, "radius") { cornerRadii }
    private val dimensions = Uniform2f(this.programID, "halfDimensions") { boxSize.mul(0.5f) }

    fun setRadius(radius: Float) {
        cornerRadii = Vector4f(radius, radius, radius, radius)
    }

    fun setRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        cornerRadii = Vector4f(topRight, bottomRight, topLeft, bottomLeft)
    }

    fun setDimensions(width: Float, height: Float) {
        boxSize = Vector2f(width, height)
    }

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            radiusUniform,
            dimensions
        )
    }
}
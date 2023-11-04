package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Redefine
import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Uniform4f
import floppacoding.mithras.shaders.uniforms.impl.UniformMatrix2f
import floppacoding.mithras.shaders.uniforms.withValue
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix2f
import org.joml.Matrix4f
import org.joml.Vector4f

/**
 * Single color version of [RoundedRectangle2].
 *
 * This shader will not interpolate the colors of the vertices.
 *
 * @author Aton
 */
object RoundedRectangle2SingleColor : Shader(
    VertexFormats.POSITION_COLOR,
    listOf(Redefine("INTERPOLATE_COLOR", "0")),
    "core/pos_color.vert",
    "core/color.frag",
    "rounded_rect/rounded_rect.geom"
) {
    private var cornerRadii: Vector4f = Vector4f(0f,0f,0f,0f)

    private val radiusUniform = Uniform4f(this.programID, "radius").withValue( cornerRadii )

    private val transformUniform = UniformMatrix2f(this.programID, "UnitTransform").withValue(Matrix2f())

    fun setTransform(posMat: Matrix4f) {
        transformUniform.updateValue(Matrix2f(posMat.m00(), posMat.m10(), posMat.m01(), posMat.m11()))
    }

    fun setRadius(radius: Float) {
        radiusUniform.updateValue(Vector4f(radius, radius, radius, radius))
    }

    fun setRadii(radii: Vector4f) {
        radiusUniform.updateValue(radii)
    }

    fun setRadii(topLeft: Float, bottomLeft: Float, bottomRight: Float, topRight: Float) {
        radiusUniform.updateValue(Vector4f(topLeft, bottomLeft, bottomRight, topRight))
    }

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.windowSize,
            transformUniform,
            radiusUniform,
        )
    }
}
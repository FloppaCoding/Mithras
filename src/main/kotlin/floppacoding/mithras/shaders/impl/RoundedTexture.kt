package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Redefine
import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.shaders.uniforms.impl.Uniform4f
import floppacoding.mithras.shaders.uniforms.impl.UniformMatrix2f
import floppacoding.mithras.shaders.uniforms.withValue
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix2f
import org.joml.Matrix4f
import org.joml.Vector4f

/**
 * ## A Shader for drawing textured rounded rectangles.
 *
 * Texture version of [RoundedRectangle].
 *
 * @author Aton
 */
object RoundedTexture : Shader(
    VertexFormats.POSITION_TEXTURE,
    listOf(Redefine("TEXTURE_MODE", "1")),
    "core/pos_tex.vert",
    "core/tex_alpha.frag",
    "rect/rounded_rect.geom"
) {
    private var cornerRadii: Vector4f = Vector4f(0f,0f,0f,0f)
    private val radiusUniform = Uniform4f(this.programID, "radius").withValue( cornerRadii )
    private val transformUniform = UniformMatrix2f(this.programID, "UnitTransform").withValue(Matrix2f()).apply { transpose = true }
    private val alphaUniform = Uniform1f(this.programID, "alpha").withValue(1f)

    fun setTransform(posMat: Matrix4f) {
        transformUniform.updateValues(posMat.m00(), posMat.m10(), posMat.m01(), posMat.m11())
    }

    fun setTransform(transform: Matrix2f) {
        transformUniform.updateValue(transform)
    }

    fun setRadius(radius: Float) {
        radiusUniform.updateValues(radius, radius, radius, radius)
    }

    fun setRadii(radii: Vector4f) {
        radiusUniform.updateValue(radii)
    }

    fun setRadii(topLeft: Float, bottomLeft: Float, bottomRight: Float, topRight: Float) {
        radiusUniform.updateValues(topLeft, bottomLeft, bottomRight, topRight)
    }

    fun setAlpha(alpha: Float) {
        alphaUniform.updateValue(alpha)
    }

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.windowSize,
            transformUniform,
            radiusUniform,
            alphaUniform
        )
    }
}
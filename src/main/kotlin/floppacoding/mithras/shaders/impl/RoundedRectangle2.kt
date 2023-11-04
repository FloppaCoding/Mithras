package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Redefine
import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.impl.RoundedRectangle2.setTransform
import floppacoding.mithras.shaders.uniforms.impl.Uniform4f
import floppacoding.mithras.shaders.uniforms.impl.UniformMatrix2f
import floppacoding.mithras.shaders.uniforms.withValue
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix2f
import org.joml.Matrix4f
import org.joml.Vector4f

/**
 * ## A Shader for drawing colored rounded rectangles.
 *
 * This shader will round the corners of rectangles.
 *
 * The input rectangles are assumed to be drawn from 2 triangles in the following order:
 *
 *           2
 *         0 ┌──────────┐ 1     ─> x
 *           │ ╲        │       ↓
 *           │   ╲      │       y
 *           │     ╲    │
 *           │       ╲  │
 *         1 └──────────┘ 0
 *                      2
 *
 * Where the bottom left triangle is drawn first. This layout is in respect to the current local coordinate system.
 *
 * The shader allows for 4 different corner radii for the individual corners.
 * These are ordered: top left, bottom left, bottom right, top right.
 *
 * Before the shader is used it is important to pass it the x and y unit vectors in the current coordinate system.
 * This can be done with [setTransform].
 *
 * @see RoundedRectangle2SingleColor
 *
 * @author Aton
 */
object RoundedRectangle2 : Shader(
    VertexFormats.POSITION_COLOR,
    listOf(Redefine("INTERPOLATE_COLOR", "1")),
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

    fun setTransform(transform: Matrix2f) {
        transformUniform.updateValue(transform)
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
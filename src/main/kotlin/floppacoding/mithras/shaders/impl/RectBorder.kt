package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.impl.RectBorder.setLineWidth
import floppacoding.mithras.shaders.impl.RectBorder.setTransform
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.shaders.uniforms.impl.UniformMatrix2f
import floppacoding.mithras.shaders.uniforms.withValue
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix2f
import org.joml.Matrix4f

/**
 * ## A shader for drawing a rectangle border with square corners.
 *
 * This shader expects an OpenGL line strip enclosing a rectangle.
 * Said line strip is expected to start in the top left corner and go in counter-clockwise direction as shown below,
 * where the numbers denote the index of the corresponding vertex.
 *
 *           4
 *         0 ┌──────────┐ 3     ─> x
 *           │          │       ↓
 *           │          │       y
 *           │          │
 *         1 └──────────┘ 2
 *
 * **NOTE:** The draw mode for an OpenGL line strip is [DEBUG_LINE_STRIP][VertexFormat.DrawMode.DEBUG_LINE_STRIP] and *not* LINE_STRIP.
 *
 * To get the desired result you need to update the shader about your desired line width through [setLineWidth],
 * as well as inform it about the scale and orientation of your current coordinate system through [setTransform].
 *
 * @author Aton
 */
object RectBorder : Shader(VertexFormats.POSITION_COLOR, "core/pos_color.vert", "core/color_chroma.frag", "rect/rect_border.geom") {
    private val widthUniform = Uniform1f(this.programID, "HalfWidth").withValue(1f)

    private val transformUniform = UniformMatrix2f(this.programID, "UnitTransform").withValue(Matrix2f())

    fun setLineWidth(width: Float) {
        widthUniform.updateValues(width * 0.5f)
    }

    fun setTransform(posMat: Matrix4f) {
        transformUniform.updateValues(posMat.m00(), posMat.m10(), posMat.m01(), posMat.m11())
    }

    fun setTransform(transform: Matrix2f) {
        transformUniform.updateValue(transform)
    }

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.windowSize,
            this.colorEffect,
            this.chromaTime,
            this.chromaAngle,
            this.chromaSize,
            transformUniform,
            widthUniform
        )
    }
}
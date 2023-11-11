package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.impl.Lines.setCapStyle
import floppacoding.mithras.shaders.uniforms.impl.Uniform1i
import floppacoding.mithras.utils.render.CapStyle
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

/**
 * ## A shader for drawing 2D lines with finite width.
 *
 * This shader turns input triangles of 0 width into triangles with the desired line width.
 *
 * It will also draw line caps as defined by [setCapStyle].
 *
 * The shader can be used for lines as well as for line strips.
 *
 * @author Aton
 */
object Lines: Shader(VertexFormats.LINES, "lines/lines.vert", "core/color_chroma.frag", "lines/lines.geom") {

    fun setCapStyle(style: CapStyle) {
        styleUniform.updateValue(style.id)
    }

    /**
     * Tells the shader that the current draw mode is [VertexFormat.DrawMode.LINE_STRIP].
     * This is required so that line caps are only drawn for the first and last segment.
     * @see setLinesMode
     */
    fun setSegmentsCount(segments: Int) {
        // The number of triangles used for a triangle strip representing a line strip is twice the number of segments.
        // Indexing starts at 0. So the index of the last triangle will be 2*segments - 1
        lengthUniform.updateValue(2*segments-1)
    }

    /**
     * Tells the shader that the current draw mode is [VertexFormat.DrawMode.LINES].
     * This is required so that line caps are drawn for all segments.
     * @see setSegmentsCount
     */
    fun setLinesMode() {
        lengthUniform.updateValue(0)
    }

    private val lengthUniform = Uniform1i(this.programID, "lastSegment")
    private val styleUniform = Uniform1i(this.programID, "style")

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.lineWidth,
            this.windowSize,
            this.colorEffect,
            this.chromaTime,
            this.chromaAngle,
            this.chromaSize,
            lengthUniform,
            styleUniform
        )
    }
}
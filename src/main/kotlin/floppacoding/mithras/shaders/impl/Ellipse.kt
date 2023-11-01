package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.utils.render.GLR

/**
 * ## A Shader for drawing 2D Ellipses from Points.
 *
 * ### Expected Draw Mode
 *
 * The shader expects the GL draw mode *POINTS*, which is supplied by the
 * [DrawModeMixin][floppacoding.mithras.mixin.render.DrawModeMixin] and can be accessed through
 * [DrawMode.valueOf("POINTS")][net.minecraft.client.render.VertexFormat.DrawMode.valueOf] or [GLR.POINTS].
 *
 * ### Expected Vertex Format
 *
 * The shader expects vertices with the following four attributes:
 * + Position (vec3) at index 0,
 * + Color (vec4) at index 1,
 * + UV0 (vec2) at index 2,
 * + UV1 (vec2) at index 3,
 *
 * as defined by [GLR.POSITION_COLOR_TEX_TEX].
 *
 * The dimensions of the ellipse are defined through the two UV elements, which are assumed to be the two semi-axes of
 * the ellipse. These are assumed to be in the same coordinate space as the Position, which is the center of the ellipse.
 *
 * @author Aton
 */
object Ellipse : Shader(
    GLR.POSITION_COLOR_TEX_TEX,
    "core/pos_color_tex_tex.vert",
    "core/color.frag",
    "ellipse/ellipse.geom"
) {

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.windowSize
        )
    }
}
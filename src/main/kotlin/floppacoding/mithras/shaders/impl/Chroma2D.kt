package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import net.minecraft.client.render.VertexFormats

/**
 * This shader will put a 2d chroma effect over anything that is rendered.
 * The color of the rendered vertices is overwritten.
 */
object Chroma2D: Shader(VertexFormats.POSITION_COLOR, "core/pos_color.vert","chroma/chroma2d.frag") {


    init {
        this.registerUniforms(
            this.chromaSize,
            this.chromaTime,
            this.chromaAngle,
            this.modelViewMat,
            this.projectionMat
        )
    }
}
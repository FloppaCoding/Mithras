package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import net.minecraft.client.render.VertexFormats

object Text2 : Shader(VertexFormats.POSITION_COLOR_TEXTURE, "core/pos_color_tex.vert", "text/text2.frag") {
    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.sampler0,
        )
    }
}
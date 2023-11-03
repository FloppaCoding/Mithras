package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import net.minecraft.client.render.VertexFormats

object Texture : Shader(VertexFormats.POSITION_TEXTURE,"core/pos_tex.vert", "core/tex.frag") {
    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.sampler0
        )
    }
}
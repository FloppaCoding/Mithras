package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.shaders.uniforms.withValue
import net.minecraft.client.render.VertexFormats

object Texture : Shader(VertexFormats.POSITION_TEXTURE,"core/pos_tex.vert", "core/tex_alpha.frag") {
    private val alphaUniform = Uniform1f(this.programID, "alpha").withValue(1f)

    fun setAlpha(alpha: Float) {
        alphaUniform.updateValue(alpha)
    }

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.sampler0,
            alphaUniform
        )
    }
}
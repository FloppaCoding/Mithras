package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import net.minecraft.client.render.VertexFormats

object GUIShader : Shader(VertexFormats.POSITION_COLOR, "core/pos_color.vert", "core/color.frag") {

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
        )
    }
}
package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import net.minecraft.client.render.VertexFormats

object GUIShader : Shader(VertexFormats.POSITION_COLOR, "gui/gui") {

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
        )
    }
}
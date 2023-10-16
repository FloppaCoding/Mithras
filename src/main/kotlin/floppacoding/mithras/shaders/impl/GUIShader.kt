package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import net.minecraft.client.render.VertexFormats

object GUIShader : Shader("gui/gui", VertexFormats.POSITION_COLOR) {

    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
        )
    }
}
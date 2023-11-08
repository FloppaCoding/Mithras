package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.shaders.uniforms.withValue
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix4f
import kotlin.math.sqrt

object Text : Shader(VertexFormats.POSITION_COLOR_TEXTURE, "core/pos_color_tex.vert", "text/text.frag") {

    private val aaUniform = Uniform1f(this.programID, "AAWidth").withValue(0.05f)

    fun adjustAAwidth(posMat: Matrix4f) {
        val rms = sqrt((posMat.m00()*posMat.m00() + posMat.m10()*posMat.m10() + posMat.m01()*posMat.m01() + posMat.m11()*posMat.m11())*0.5f)
        aaUniform.updateValue(0.12f/rms)
    }

    fun adjustAAwidth(fonstScale: Float) {
        aaUniform.updateValue(0.05f/fonstScale)
    }


    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.sampler0,
            aaUniform
        )
    }
}
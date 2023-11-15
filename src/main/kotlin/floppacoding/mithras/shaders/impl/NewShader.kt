package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Sampler
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.shaders.uniforms.impl.Uniform1i
import floppacoding.mithras.shaders.uniforms.impl.UniformMatrix4f
import floppacoding.mithras.shaders.uniforms.withValue
import floppacoding.mithras.utils.render.RenderCall
import org.joml.Matrix4f
import kotlin.math.sqrt

object NewShader : Shader(listOf("Position", "Color", "UV0"), "new/pos_col_tex_2d.vert", "new/glr.frag") {

    /**
     * This value determines the width of text antialiasing.
     * It should be proportional to the derivative dSDF / dr of the SDF with respect to the distance in texels.
     * That makes it inversely proportional to the padding used for the SDF glyphs.
     * So if changes are made to that this value has to be adjusted accordingly.
     */
    private const val SCALE_FACTOR = 0.18f

    private val projectionMatrix = UniformMatrix4f(this.programID, "ProjMat").withValue(Matrix4f())
    private val sampler = Sampler(this.programID, "Sampler0").withValue(0)
    private val aaUniform = Uniform1f(this.programID, "AAWidth").withValue(0.05f)
    private val modeUniform = Uniform1i(this.programID, "Mode").withValue(0)

    fun setAAwidth(posMat: Matrix4f) {
        val rms = sqrt((posMat.m00()*posMat.m00() + posMat.m10()*posMat.m10() + posMat.m01()*posMat.m01() + posMat.m11()*posMat.m11())*0.5f)
        aaUniform.updateValue(SCALE_FACTOR/rms)
    }

    fun setAAwidth(fontScale: Float) {
        aaUniform.updateValue(SCALE_FACTOR/fontScale)
    }

    fun setColorMode(mode: RenderCall.ColorMode) {
        modeUniform.updateValue(mode.id)
    }

    fun setTextureUnit(unit: Int) {
        sampler.updateValue(unit)
    }

    fun setProjectionMatrix(matrix4f: Matrix4f) {
        projectionMatrix.updateValue(matrix4f)
    }

    fun uploadColorMode() {
        modeUniform.update()
    }

    fun uploadTextureUnit() {
        sampler.update()
    }

    fun uploadAAwidth() {
        aaUniform.update()
    }



    init {
        this.registerUniforms(
            projectionMatrix,
            sampler,
            aaUniform,
            modeUniform
        )
    }
}
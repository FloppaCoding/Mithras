package floppacoding.aurora.core.shader.impl

import floppacoding.aurora.core.RenderCall
import floppacoding.aurora.core.shader.Shader
import floppacoding.aurora.core.shader.uniforms.impl.Uniform1f
import floppacoding.aurora.core.shader.uniforms.impl.Uniform1i
import floppacoding.aurora.core.shader.uniforms.withValue

/**
 * The main shader for the aurora library.
 *
 * @author Aton
 */
object MainShader : Shader(listOf("Position", "Color", "UV0"), "core/pos_col_tex_2d.vert", "core/main.frag") {

    /**
     * This value determines the width of text antialiasing.
     * It should be proportional to the derivative dSDF / dr of the SDF with respect to the distance in texels.
     * That makes it inversely proportional to the padding used for the SDF glyphs.
     * So if changes are made to that this value has to be adjusted accordingly.
     */
    private const val SCALE_FACTOR = 0.18f

    private val aaUniform = Uniform1f(this.programID, "AAWidth").withValue(0.05f)
    private val modeUniform = Uniform1i(this.programID, "Mode").withValue(0)

    fun setAAwidth(fontScale: Float) {
        aaUniform.updateValue(SCALE_FACTOR /fontScale)
    }

    fun setColorMode(mode: RenderCall.ColorMode) {
        modeUniform.updateValue(mode.id)
    }

    fun setColorMode(id: Int) {
        modeUniform.updateValue(id)
    }

    fun setTextureUnit(unit: Int) {
        this.sampler0.updateValue(unit)
    }

    fun uploadColorMode() {
        modeUniform.update()
    }

    fun uploadTextureUnit() {
        this.sampler0.update()
    }

    fun uploadAAwidth() {
        aaUniform.update()
    }

    init {
        this.registerUniforms(
            this.projectionMat,
            this.windowSize,
            this.chromaTime,
            this.chromaAngle,
            this.chromaSize,
            this.sampler0,
            aaUniform,
            modeUniform
        )
    }
}
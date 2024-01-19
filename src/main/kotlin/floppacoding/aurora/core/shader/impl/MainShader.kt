package floppacoding.aurora.core.shader.impl

import floppacoding.aurora.core.RenderCall
import floppacoding.aurora.core.shader.Shader
import floppacoding.aurora.core.shader.uniforms.impl.Uniform1f
import floppacoding.aurora.core.shader.uniforms.impl.Uniform1i
import floppacoding.aurora.core.shader.uniforms.impl.Uniform4f
import floppacoding.aurora.core.shader.uniforms.withValue
import org.joml.Vector4f

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
    private const val COLOR_SCALE = 1 / 255f

    private val aaUniform = Uniform1f(this.programID, "AAWidth").withValue(0.05f)
    private val modeUniform = Uniform1i(this.programID, "Mode").withValue(0)

    private val topLeftColor     = Uniform4f(this.programID, "TopLeftColor"    ).withValue(Vector4f(1f))
    private val topRightColor    = Uniform4f(this.programID, "TopRightColor"   ).withValue(Vector4f(1f))
    private val bottomLeftColor  = Uniform4f(this.programID, "BottomLeftColor" ).withValue(Vector4f(1f))
    private val bottomRightColor = Uniform4f(this.programID, "BottomRightColor").withValue(Vector4f(1f))

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

    fun setColor(position: ColorPosition, color: Int) {
        setColor(position, (color shr 16) and 0xff, (color shr 8) and 0xff, (color shr 0) and 0xff, (color shr 24) and 0xff)
    }

    fun setColor(position: ColorPosition, red: Int, green: Int, blue: Int, alpha: Int) {
        position.uniform.updateValues(red * COLOR_SCALE, green* COLOR_SCALE, blue * COLOR_SCALE, alpha * COLOR_SCALE)
    }

    fun uploadColor(position: ColorPosition) {
        position.uniform.update()
    }

    fun uploadColors() {
        topLeftColor.update()
        topRightColor.update()
        bottomLeftColor.update()
        bottomRightColor.update()
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
            modeUniform,
            topLeftColor,
            topRightColor,
            bottomLeftColor,
            bottomRightColor
        )
    }

    enum class ColorPosition(val uniform: Uniform4f) {
        TOP_LEFT(topLeftColor),
        TOP_RIGHT(topRightColor),
        BOTTOM_LEFT(bottomLeftColor),
        BOTTOM_RIGHT(bottomRightColor)
    }
}
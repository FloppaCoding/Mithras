package floppacoding.mithras.shaders.impl

import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.impl.TextShader.adjustAAwidth
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.shaders.uniforms.withValue
import net.minecraft.client.render.VertexFormats
import org.joml.Matrix4f
import kotlin.math.sqrt

/**
 * ## SDF based glyph rendering shader
 *
 * This shader meant to be used to render colored glyphs from a Signed Distance Function (SDF) font atlas.
 *
 * It expects such an atlas to be bound to GL_TEXTURE0.
 * The atlas is expected to store the distance encoded in the red channel, with 0.5 (or 128 as byte) being the on edge
 * value of the SDF. The antialiasing expects the gradient of the SDF to be 0.25 (64 as byte) per pixel
 * with values > 0.5 being inside the glyph.
 *
 * For the antialiasing to work correctly the shader has to be updated on the current scale of the text.
 * This can be done through [adjustAAwidth]. The matrix version will automatically determine the scale from the current
 * position matrix.
 *
 * @author Aton
 */
object TextShader : Shader(VertexFormats.POSITION_COLOR_TEXTURE, "core/pos_color_tex.vert", "text/text_chroma.frag") {

    /**
     * This value determines the width of the antialiasing.
     * It should be proportional to the derivative dSDF / dr of the SDF with respect to the distance in texels.
     * That makes it inversely proportional to the padding used for the SDF glyphs.
     * So if changes are made to that this value has to be adjusted accordingly.
     */
    private const val SCALE_FACTOR = 0.18f

    private val aaUniform = Uniform1f(this.programID, "AAWidth").withValue(0.05f)

    fun adjustAAwidth(posMat: Matrix4f) {
        val rms = sqrt((posMat.m00()*posMat.m00() + posMat.m10()*posMat.m10() + posMat.m01()*posMat.m01() + posMat.m11()*posMat.m11())*0.5f)
        aaUniform.updateValue(SCALE_FACTOR/rms)
    }

    fun adjustAAwidth(fonstScale: Float) {
        aaUniform.updateValue(SCALE_FACTOR/fonstScale)
    }


    init {
        this.registerUniforms(
            this.modelViewMat,
            this.projectionMat,
            this.sampler0,
            this.windowSize,
            this.colorEffect,
            this.chromaTime,
            this.chromaAngle,
            this.chromaSize,
            aaUniform
        )
    }
}
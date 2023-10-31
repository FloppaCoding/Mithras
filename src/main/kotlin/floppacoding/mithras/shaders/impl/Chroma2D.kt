package floppacoding.mithras.shaders.impl

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.shaders.Shader
import floppacoding.mithras.shaders.uniforms.impl.Uniform1f
import floppacoding.mithras.utils.Extensions.renderTickCounter
import net.minecraft.client.render.VertexFormats
import kotlin.math.exp

/**
 * This shader will put a 2d chroma effect over anything that is rendered.
 * The color of the rendered vertices is overwritten.
 */
object Chroma2D: Shader(VertexFormats.POSITION_COLOR, "chroma2d/chroma2d") {

    /*
    The scaling for the values is not super straightforward, but makes configuring the chroma effect very smooth.
     */

    private val chromaSize = Uniform1f(this.programID, "chromaSize") {
        (exp( MainSettings.chromaSize * 0.5f ) -1f)  / 30f
    }
    private val chromaSpeed = Uniform1f(this.programID, "chromaTime") {
        if (MainSettings.chromaSpeed == 0f)
            0f
        else
            ((Mithras.totalTicks + mc.renderTickCounter.tickDelta) / 20.0 * (1- exp(MainSettings.chromaSpeed) )).toFloat()
    }
    private val chromaAngle = Uniform1f(this.programID, "chromaAngle") {
        (MainSettings.chromaAngle + 90f) * 0.017453292f
    }

    init {
        this.registerUniforms(
            chromaSize,
            chromaSpeed,
            chromaAngle,
            this.modelViewMat,
            this.projectionMat
        )
    }
}
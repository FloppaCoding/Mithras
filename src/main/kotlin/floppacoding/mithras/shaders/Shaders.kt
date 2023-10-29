package floppacoding.mithras.shaders

import floppacoding.mithras.shaders.impl.Chroma2D
import floppacoding.mithras.shaders.impl.FractalShader
import floppacoding.mithras.shaders.impl.GUIShader
import floppacoding.mithras.shaders.impl.RoundedRectangle


object Shaders {
    val shaders: MutableList<Shader> = mutableListOf(
        GUIShader,
        Chroma2D,
        FractalShader,
        RoundedRectangle,
    )


    @Throws(Exception::class)
    fun reloadShaders() {
        shaders.forEach {
            it.reloadShader()
        }
    }
}
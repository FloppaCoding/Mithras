package floppacoding.mithras.shaders

import floppacoding.mithras.shaders.impl.*


object Shaders {
    val shaders: MutableList<Shader> = mutableListOf(
        GUIShader,
        Chroma2D,
        FractalShader,
        RoundedRectangle,
        Lines,
    )


    @Throws(Exception::class)
    fun reloadShaders() {
        shaders.forEach {
            it.reloadShader()
        }
    }
}
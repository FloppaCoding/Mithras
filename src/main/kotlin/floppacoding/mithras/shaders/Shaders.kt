package floppacoding.mithras.shaders

import floppacoding.mithras.shaders.impl.*


object Shaders {
    val shaders: MutableList<Shader>
        get() = mutableListOf(
            FractalShader,
            NewShader,
        )


    @Throws(Exception::class)
    fun reloadShaders() {
        Shader.clearLoadBuffers()
        shaders.forEach {
            it.reloadShader()
        }
    }
}
package floppacoding.mithras.shaders

import floppacoding.aurora.core.shader.Shader
import floppacoding.aurora.core.shader.impl.MainShader
import floppacoding.mithras.shaders.impl.*


object Shaders {
    val shaders: MutableList<Shader>
        get() = mutableListOf(
            FractalShader,
            MainShader,
        )


    @Throws(Exception::class)
    fun reloadShaders() {
        Shader.clearLoadBuffers()
        shaders.forEach {
            it.reloadShader()
        }
    }
}
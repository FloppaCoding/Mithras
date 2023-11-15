package floppacoding.mithras.shaders

import floppacoding.mithras.shaders.impl.*


object Shaders {
    val shaders: MutableList<Shader>
        get() = mutableListOf(
            GUIShader,
            Chroma2D,
            FractalShader,
            RoundedRectangleLegacy,
            RoundedRectangle,
            Lines,
            Ellipse,
            Texture,
            RoundedTexture,
            TextShader,
            RectBorder,
            RoundedRectBorder,
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
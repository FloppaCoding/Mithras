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
            Text,
            RectBorder,
        )


    @Throws(Exception::class)
    fun reloadShaders() {
        shaders.forEach {
            it.reloadShader()
        }
    }
}
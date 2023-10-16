package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.Uniform
import org.lwjgl.opengl.GL20

class Uniform1f(programID: Int, name: String, private val source: () -> Float) : Uniform<Float>(programID, name) {

    override var lastValue: Float? = null

    override fun update() {
        val newVal = source()
        if (newVal != lastValue) {
            GL20.glUniform1f(this.uninformID, newVal)
            lastValue = newVal
        }
    }
}
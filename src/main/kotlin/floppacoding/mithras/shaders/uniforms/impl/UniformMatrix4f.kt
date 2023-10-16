package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.Uniform
import org.joml.Matrix4f
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class UniformMatrix4f(programID: Int, name: String, private val source: (() -> Matrix4f)?) : Uniform<Matrix4f>(programID, name) {

    private val floatData: FloatBuffer = MemoryUtil.memAllocFloat(16)


    override var lastValue: Matrix4f? = null
    override fun update() {
        val newVal = source?.let { it() } ?: return
        if (newVal != lastValue) {
            floatData.position(0)
            newVal.get(floatData)
            GL20.glUniformMatrix4fv(this.uninformID, false, floatData)
            lastValue = newVal
        }
    }
}
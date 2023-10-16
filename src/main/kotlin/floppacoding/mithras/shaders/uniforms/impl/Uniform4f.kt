package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.Uniform
import org.joml.Vector4f
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class Uniform4f(programID: Int, name: String, private val source: (() -> Vector4f)?) : Uniform<Vector4f>(programID, name) {
    private val floatData: FloatBuffer = MemoryUtil.memAllocFloat(4)

    override var lastValue: Vector4f? = null
    override fun update() {
        val newVal = source?.let { it() } ?: return
        if (newVal != lastValue) {
            floatData.position(0)
            floatData.put(0, newVal.x)
            floatData.put(1, newVal.y)
            floatData.put(2, newVal.z)
            floatData.put(3, newVal.w)
            floatData.rewind()
            GL20.glUniform4fv(this.uninformID, floatData)
            lastValue = newVal
        }
    }
}
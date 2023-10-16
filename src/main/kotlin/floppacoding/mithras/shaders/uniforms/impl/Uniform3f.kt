package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.Uniform
import org.joml.Vector3f
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class Uniform3f(programID: Int, name: String, private val source: (() -> Vector3f)?) : Uniform<Vector3f>(programID, name) {
    private val floatData: FloatBuffer = MemoryUtil.memAllocFloat(3)

    override var lastValue: Vector3f? = null
    override fun update() {
        val newVal = source?.let { it() } ?: return
        if (newVal != lastValue) {
            floatData.position(0)
            floatData.put(0, newVal.x)
            floatData.put(1, newVal.y)
            floatData.put(2, newVal.z)
            floatData.rewind()
            GL20.glUniform3fv(this.uninformID, floatData)
            lastValue = newVal
        }
    }
}
package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.Uniform
import org.joml.Vector2f
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class Uniform2f(programID: Int, name: String, private val source: (() -> Vector2f)?) : Uniform<Vector2f>(programID, name) {
    private val floatData: FloatBuffer = MemoryUtil.memAllocFloat(2)

    override var lastValue: Vector2f? = null
    override fun update() {
        val newVal = source?.let { it() } ?: return
        if (newVal != lastValue) {
            floatData.position(0)
            floatData.put(0, newVal.x)
            floatData.put(1, newVal.y)
            floatData.rewind()
            GL20.glUniform2fv(this.uninformID, floatData)
            lastValue = newVal
        }
    }
}
package floppacoding.mithras.shaders.uniforms.impl

import floppacoding.mithras.shaders.uniforms.Uniform
import org.joml.Matrix3f
import org.lwjgl.opengl.GL20
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

class UniformMatrix3f(programID: Int, name: String, private val source: (() -> Matrix3f)?) : Uniform<Matrix3f>(programID, name) {
    private val floatData: FloatBuffer = MemoryUtil.memAllocFloat(9)


    override var lastValue: Matrix3f? = null
    override fun update() {
        val newVal = source?.let { it() } ?: return
        if (newVal != lastValue) {
            floatData.position(0)
            newVal.get(floatData)
            GL20.glUniformMatrix3fv(this.uninformID, false, floatData)
            lastValue = newVal
        }
    }
}
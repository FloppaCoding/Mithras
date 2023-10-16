package floppacoding.mithras.shaders

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.shaders.uniforms.Uniform
import floppacoding.mithras.shaders.uniforms.impl.Uniform2f
import floppacoding.mithras.shaders.uniforms.impl.UniformMatrix3f
import floppacoding.mithras.shaders.uniforms.impl.UniformMatrix4f
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexFormat
import net.minecraft.util.Identifier
import org.joml.Vector2f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL40

/**
 * Shaders can be used to transform the image that is being drawn.
 * There are multiple types of shaders. So far vertex and fragment shaders are supported.
 * The vertex shader acts on all the vertices that are being drawn and allows to modify those.
 * The fragment shader acts on the pixels that are being drawn and can be used to adjust the color of those.
 *
 * This class offers a framework to use those shaders.
 * The shader code itself has to be written in GLSL and placed in the resources package.
 * You will always need both a vertex adn a fragment shader.
 * To use a shader program it first has to be compiled and register on the GPU. All of that is automatically done for
 * you simply by creating an instance of this class.
 * The best way to do this is to create an object that inherits from this class.
 * It is recommended that you do that in an object file within ./impl/ .
 *
 * To then use the shader (let's call it SomeShader) simply invoke SomeShader.useShader() before rendering and
 * SomeShader.stopShader() after rendering the part that should be affected.
 *
 * @see Uniform
 */
open class Shader(vertexFile: String, fragmentFile: String, protected val format: VertexFormat) {

    constructor(name: String, format: VertexFormat) : this("$name.vert", "$name.frag", format)

    val programID: Int
    private val vertexShaderID: Int
    private val fragmentShaderID: Int

    private val uniforms: ArrayList<Uniform<*>> = arrayListOf()

    protected val modelViewMat: UniformMatrix4f
    protected val projectionMat: UniformMatrix4f
    protected val viewRotationMat: UniformMatrix3f
    protected val screenSize: Uniform2f

    init {
        vertexShaderID = loadShader(vertexFile, GL40.GL_VERTEX_SHADER)
        fragmentShaderID = loadShader(fragmentFile, GL40.GL_FRAGMENT_SHADER)
        programID = GL40.glCreateProgram()
        GL40.glAttachShader(programID, vertexShaderID)
        GL40.glAttachShader(programID, fragmentShaderID)
        GL40.glLinkProgram(programID)
        GL40.glValidateProgram(programID)

        modelViewMat = UniformMatrix4f(programID, "ModelViewMat") {RenderSystem.getModelViewMatrix()}
        projectionMat = UniformMatrix4f(programID, "ProjMat") {RenderSystem.getProjectionMatrix()}
        viewRotationMat = UniformMatrix3f(programID, "IViewRotMat") {RenderSystem.getInverseViewRotationMatrix()}
        screenSize = Uniform2f(programID, "ScreenSize") {
            val window = MinecraftClient.getInstance().window
            Vector2f(window.framebufferWidth.toFloat(), window.framebufferHeight.toFloat())
        }
    }

    /**
     * Activates this shader.
     */
    fun useShader() {
        GL40.glUseProgram(programID)
        updateUniforms()
    }

    /**
     * Deactivates this shader by setting the current gl program to 0.
     */
    fun stopShader() {
        GL40.glUseProgram(0)
    }

    /**
     * Deletes this Program from the GPU Memory.
     */
    fun unloadShader() {
        GL40.glDeleteProgram(programID)
    }

    /**
     * Registers the given uniforms to this shader.
     *
     * To be used in the Implementations.
     */
    protected fun registerUniforms(vararg uniformArgs: Uniform<*>) {
        uniformArgs.forEach { uniforms.add(it) }
    }

    /**
     * Updates all the impl values to the GPU.
     */
    private fun updateUniforms() {
        uniforms.forEach {
            it.update()
        }
    }

    /**
     * Loads the shader with the given file name relative to RESOURCE_DOMAIN\shaders.
     *
     * @return The Id of the created shader.
     * @throws Error when there was an error with compiling the shader
     * @throws IOException when the shader could not be read.
     */
    @Throws(Error::class, Exception::class)
    private fun loadShader(file: String, type: Int): Int {
        val builder = java.lang.StringBuilder()
        try {
            val reader = mc.resourceManager.getResource(Identifier(Mithras.RESOURCE_DOMAIN, "shaders/$file")).get()
                .inputStream.bufferedReader()
            var line: String? = reader.readLine()
            while (line != null) {
                builder.append(line).append("//\n")
                line = reader.readLine()
            }
            reader.close()
        }catch (e: Exception) {
            e.printStackTrace()
            throw  e
        }
        val shaderId = GL40.glCreateShader(type)
        GL40.glShaderSource(shaderId, builder)
        GL40.glCompileShader(shaderId)

        if (GL40.glGetShaderi(shaderId, GL40.GL_COMPILE_STATUS) == GL11.GL_FALSE){
            Mithras.logger.error(GL40.glGetShaderInfoLog(shaderId, 1000))
            throw Error("Failed loading shader")
        }
        return shaderId
    }
}
package floppacoding.mithras.shaders

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras
import floppacoding.mithras.shaders.uniforms.Uniform
import floppacoding.mithras.shaders.uniforms.UniformGL
import floppacoding.mithras.shaders.uniforms.impl.*
import floppacoding.mithras.shaders.uniforms.withValue
import floppacoding.mithras.utils.render.GLR
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexFormat
import org.joml.Vector2f
import org.lwjgl.opengl.GL46
import java.io.IOException
import kotlin.properties.Delegates

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
 * @param format The vertex format expected by this shader.
 * @param vertexFile The file name containing the vertex shader. This is assumed to be relative to
 * [Mithras.RESOURCE_DOMAIN]/shaders.
 * @param fragmentFile The file name containing the fragment shader. This is assumed to be relative to
 *  [Mithras.RESOURCE_DOMAIN]/shaders.
 *  @param extraFiles A list of additional shader files paired together with their GL shader type.
 *
 * @see UniformGL
 */
open class Shader(
    protected val format: VertexFormat,
    private val vertexFile: String,
    private val fragmentFile: String,
    extraFiles: List<Pair<String, Int>>,
    preprocessorArgs: Collection<PreprocessorArgument>
) : AutoCloseable {

    constructor(format: VertexFormat, vertexFile: String, fragmentFile: String): this(format, vertexFile, fragmentFile, emptyList(), emptyList())
    constructor(format: VertexFormat, preprocessorArgs: Collection<PreprocessorArgument>, vertexFile: String, fragmentFile: String):
            this(format, vertexFile, fragmentFile, emptyList(), preprocessorArgs)
    constructor(format: VertexFormat, vertexFile: String, fragmentFile: String, vararg extraFiles: Pair<String,Int>):
            this(format, vertexFile, fragmentFile, extraFiles.toList(), emptyList())
    constructor(format: VertexFormat, vertexFile: String, fragmentFile: String, vararg extraFiles: String):
            this(format, vertexFile, fragmentFile, extraFiles.mapNotNull { getShaderType(it)?.let { type -> Pair(it, type) }  }, emptyList())
    constructor(format: VertexFormat, preprocessorArgs: Collection<PreprocessorArgument>, vertexFile: String, fragmentFile: String, vararg extraFiles: String):
            this(format, vertexFile, fragmentFile, extraFiles.mapNotNull { getShaderType(it)?.let { type -> Pair(it, type) }  }, preprocessorArgs)
    constructor(format: VertexFormat, name: String) : this(format, "$name.vert", "$name.frag")

    var programID: Int by Delegates.notNull()
    private var vertexShaderID: Int
    private var fragmentShaderID: Int
    private var extraShaders: List<ShaderFile>

    private val uniforms: ArrayList<Uniform> = arrayListOf()
    // TODO add support to change this so that this can be used as an external library
    private val resourceDomain: String = Mithras.RESOURCE_DOMAIN
    private val preprocessor: ShaderPreprocessor = ShaderPreprocessor(resourceDomain, preprocessorArgs)



    /**
     * The [model view matrix][RenderSystem.getModelViewMatrix]
     * combines teh transformation from object coordinates to world coordinates and from those
     * to camera coordinates.
     *
     * For Hud elements this should always consist of the Identity combined with a translation by z = -11000.
     *
     *  1 | 0 | 0 | 0
     *  | ---: | ---: | ---: | ---:
     *  0 | 1 | 0 | 0
     *  0 | 0 | 1 | -11000
     *  0 | 0 | 0 | 1
     *
     */
    protected val modelViewMat: UniformMatrix4f by lazy {
        UniformMatrix4f(programID, "ModelViewMat") {RenderSystem.getModelViewMatrix()}
    }
    /**
     * The [projection matrix][RenderSystem.getProjectionMatrix] used by vanilla rendering.
     * This **DOES** include the GUI Scale.
     *
     * This uniform is exclusive with [projectionMat].
     *
     * @see projectionMat
     */
    protected val vanillaProjectionMat: UniformMatrix4f by lazy {
        UniformMatrix4f(programID, "ProjMat") {RenderSystem.getProjectionMatrix()}
    }
    /**
     * The [projection matrix][GLR.projectionMatrix] used for custom rendering.
     * This does **NOT** include the GUI Scale.
     *
     * This uniform is exclusive with [vanillaProjectionMat].
     *
     * @see vanillaProjectionMat
     */
    protected val projectionMat: UniformMatrix4f by lazy {
        UniformMatrix4f(programID, "ProjMat") { GLR.projectionMatrix }
    }
    protected val viewRotationMat: UniformMatrix3f by lazy {
        UniformMatrix3f(programID, "IViewRotMat") {RenderSystem.getInverseViewRotationMatrix()}
    }
    protected val windowSize: Uniform2f by lazy { Uniform2f(programID, "ScreenSize") {
        val window = MinecraftClient.getInstance().window
        Vector2f(window.framebufferWidth.toFloat(), window.framebufferHeight.toFloat())
    } }
    protected val lineWidth: Uniform1f by lazy { Uniform1f(programID, "LineWidth") { RenderSystem.getShaderLineWidth()} }
    protected val sampler0: Sampler by lazy { Sampler(programID, "Sampler0").withValue(0) }
    protected val sampler1: Sampler by lazy { Sampler(programID, "Sampler1").withValue(1) }
    protected val sampler2: Sampler by lazy { Sampler(programID, "Sampler2").withValue(2) }

    init {
        vertexShaderID = loadShader(vertexFile, GL46.GL_VERTEX_SHADER)
        fragmentShaderID = loadShader(fragmentFile, GL46.GL_FRAGMENT_SHADER)

        val newShaders = mutableListOf<ShaderFile>()
        extraFiles.forEach {
            val glId = loadShader(it.first, it.second)
            newShaders.add(ShaderFile(it.first, it.second, glId))
        }
        extraShaders = newShaders

        programID = GL46.glCreateProgram()
        // bind Attributes
        format.attributeNames.withIndex().forEach {
            GL46.glBindAttribLocation(programID, it.index, it.value)
        }

        GL46.glAttachShader(programID, vertexShaderID)
        GL46.glAttachShader(programID, fragmentShaderID)
        extraShaders.forEach {
            GL46.glAttachShader(programID, it.id)
        }
        GL46.glLinkProgram(programID)
        GL46.glValidateProgram(programID)
    }

    /**
     * Activates this shader.
     */
    fun useShader() {
        GL46.glUseProgram(programID)
        updateUniforms()
    }

    /**
     * Attempts to reload and recompile the shader from the corresponding files.
     *
     * If there is an error the old shader will persist and the error is printed to the logs.
     * @throws Exception when there was an error with compiling the shader or validation the program.
     * @throws IOException when the shader could not be read.
     */
    @Throws(Exception::class, IOException::class)
    fun reloadShader() {
        shaderBuffer.clear()
        val newVertexShaderID = loadShader(vertexFile, GL46.GL_VERTEX_SHADER)
        val newFragmentShaderID = loadShader(fragmentFile, GL46.GL_FRAGMENT_SHADER)
        val newShaders = mutableListOf<ShaderFile>()
        extraShaders.forEach {
            val glId = loadShader(it.fileName, it.type)
            newShaders.add(ShaderFile(it.fileName, it.type, glId))
        }
        val newProgramID = GL46.glCreateProgram()
        // bind Attributes
        format.attributeNames.withIndex().forEach {
            GL46.glBindAttribLocation(newProgramID, it.index, it.value)
        }
        GL46.glAttachShader(newProgramID, newVertexShaderID)
        GL46.glAttachShader(newProgramID, newFragmentShaderID)
        newShaders.forEach {
            GL46.glAttachShader(newProgramID, it.id)
        }
        GL46.glLinkProgram(newProgramID)
        if (GL46.glGetProgrami(newProgramID, GL46.GL_LINK_STATUS) != GL46.GL_TRUE) {
            val errorMessage = GL46.glGetProgramInfoLog(newProgramID, 10000)
            Mithras.logger.error(errorMessage)
            throw Exception("Program linking failed for ${this::class.simpleName}.", Exception(errorMessage))
        }

        GL46.glValidateProgram(newProgramID)
        if (GL46.glGetProgrami(newProgramID, GL46.GL_VALIDATE_STATUS) != GL46.GL_TRUE) {
            val errorMessage = GL46.glGetProgramInfoLog(newProgramID, 10000)
            Mithras.logger.error(errorMessage)
            throw Exception("Program validation failed for ${this::class.simpleName}.", Exception(errorMessage))
        }


        // glDeleteShader flags the shader for deletion for when it is no longer attached to a program.
        GL46.glDeleteShader(vertexShaderID)
        GL46.glDeleteShader(fragmentShaderID)
        extraShaders.forEach {
            GL46.glDeleteShader(it.id)
        }
        GL46.glDeleteProgram(programID)

        programID = newProgramID
        vertexShaderID = newVertexShaderID
        fragmentShaderID = newFragmentShaderID
        extraShaders = newShaders


        // Update the IDs of the uniforms to the new program
        uniforms.forEach { it.updateID(programID) }
    }

    /**
     * Deactivates this shader by setting the current gl program to 0.
     */
    fun stopShader() {
        GL46.glUseProgram(0)
    }

    /**
     * Registers the given uniforms to this shader.
     *
     * To be used in the Implementations.
     */
    protected fun registerUniforms(vararg uniformArgs: Uniform) {
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
     * @throws Exception when there was an error with compiling the shader.
     * @throws IOException when the shader could not be read.
     */
    @Throws(Exception::class, IOException::class)
    private fun loadShader(file: String, type: Int): Int {
        return shaderBuffer.getOrPut(ShaderSource(preprocessor, file)) putShader@{
            val builder: StringBuilder
            try {
                val stream = this.javaClass.getResourceAsStream("/assets/$resourceDomain/shaders/$file") ?:
                    throw Exception("Shader file not found: '/assets/$resourceDomain/shaders/$file'")
                builder = preprocessor.processShader(stream)
            }catch (e: Exception) {
                e.printStackTrace()
                throw  e
            }
            val shaderId = GL46.glCreateShader(type)
            GL46.glShaderSource(shaderId, builder)
            GL46.glCompileShader(shaderId)

            if (GL46.glGetShaderi(shaderId, GL46.GL_COMPILE_STATUS) == GL46.GL_FALSE){
                val errorMessage = GL46.glGetShaderInfoLog(shaderId, 10000)
                Mithras.logger.error(errorMessage)
                Mithras.logger.debug(GL46.glGetShaderSource(shaderId))
                throw Exception("Failed loading shader $file", Exception(errorMessage))
            }

            return@putShader shaderId
        }
    }

    override fun close() {
        GL46.glDeleteShader(vertexShaderID)
        GL46.glDeleteShader(fragmentShaderID)
        GL46.glDeleteProgram(programID)
        uniforms.forEach { it.close() }
    }

    data class ShaderFile(val fileName: String, val type: Int, val id: Int)

    data class ShaderSource(val processor: ShaderPreprocessor, val path: String)

    companion object {
        /**
         * Already loaded shaders mapped to their gl shader id.
         */
        private val shaderBuffer: MutableMap<ShaderSource, Int> = mutableMapOf()

        private fun getShaderType(fileName: String): Int? {
            return when(fileName.substringAfterLast(".")) {
                "vert" -> GL46.GL_VERTEX_SHADER
                "frag" -> GL46.GL_FRAGMENT_SHADER
                "geom" -> GL46.GL_GEOMETRY_SHADER
                "comp" -> GL46.GL_COMPUTE_SHADER
                "tesc" -> GL46.GL_TESS_CONTROL_SHADER
                "tese" -> GL46.GL_TESS_EVALUATION_SHADER
                else -> null
            }
        }
    }
}
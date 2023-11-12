package floppacoding.mithras.shaders

import com.mojang.blaze3d.systems.RenderSystem
import floppacoding.mithras.Mithras
import floppacoding.mithras.shaders.uniforms.Uniform
import floppacoding.mithras.shaders.uniforms.UniformGL
import floppacoding.mithras.shaders.uniforms.impl.*
import floppacoding.mithras.shaders.uniforms.withValue
import floppacoding.mithras.utils.Extensions.renderTickCounter
import floppacoding.mithras.utils.render.GLR
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexFormat
import org.joml.Vector2f
import org.lwjgl.opengl.GL45
import java.io.IOException
import kotlin.math.exp
import kotlin.properties.Delegates

/**
 * # Super class for OpenGL shader programs.
 *
 * ## What is a shader program
 * Shader programs run on the GPU and determine how the primitives passed to the GPU are to be drawn.
 *
 * Typically, a shader program consists of a vertex and a fragment shader.
 * The vertex shader acts on all the vertices passed to the GPU and may transform those as well as evaluate or
 * generate attributes.
 * The fragment shader acts on the pixels that are being drawn and can be used to adjust the color of those.
 * It also is responsible for filling a shape with a texture.
 *
 * Shaders are written in GLSL. For further information refer to the
 * [Wiki](https://www.khronos.org/opengl/wiki/OpenGL_Shading_Language), or the
 * [Language Specifications](https://registry.khronos.org/OpenGL/specs/gl/GLSLangSpec.4.60.pdf).
 *
 *
 * ## About this class
 *
 * This class offers a framework to use those shaders.
 * The shader code itself has to be written in GLSL and placed in the resources package.
 * You will always need both a vertex adn a fragment shader.
 * To use a shader program it first has to be compiled and registered on the GPU. All of that is automatically done for
 * you simply by creating an instance of this class.
 * The best way to do this is to create a singleton / kotlin object that inherits from this class.
 *
 * To then use the shader (let's call it SomeShader) simply invoke SomeShader.useShader() before your draw call and
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
 * @author Aton
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
        private set
    private var vertexShaderID: Int
    private var fragmentShaderID: Int
    private var extraShaders: List<ShaderFile>

    private val uniforms: ArrayList<Uniform> = arrayListOf()
    // TODO add support to change this so that this can be used as an external library
    private val resourceDomain: String = Mithras.RESOURCE_DOMAIN
    private val preprocessor: ShaderPreprocessor = ShaderPreprocessor(resourceDomain, preprocessorArgs)




    //<editor-fold desc="Default Uniforms">
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

    /**
     * Determines the use of special coloring effects.
     */
    protected val colorEffect: Uniform1i by lazy {
        hasColorEffect = true
        Uniform1i(programID, "ColorEffect").withValue(0)
    }
    protected val chromaSize: Uniform1f by lazy { Uniform1f(programID, "chromaSize") { chromaSizeInternal } }
    protected val chromaTime: Uniform1f by lazy { Uniform1f(programID, "chromaTime") {
        if (chromaSpeedInternal == 0f)
            0f
        else
            ((Mithras.totalTicks % 100_000 + Mithras.mc.renderTickCounter.tickDelta) * chromaSpeedInternal)
    } }
    protected val chromaAngle: Uniform1f by lazy { Uniform1f(programID, "chromaAngle") { chromaAngleInternal } }

    private var hasColorEffect: Boolean = false

    //</editor-fold>

    init {
        vertexShaderID = loadShader(vertexFile, GL45.GL_VERTEX_SHADER)
        fragmentShaderID = loadShader(fragmentFile, GL45.GL_FRAGMENT_SHADER)

        val newShaders = mutableListOf<ShaderFile>()
        extraFiles.forEach {
            val glId = loadShader(it.first, it.second)
            newShaders.add(ShaderFile(it.first, it.second, glId))
        }
        extraShaders = newShaders

        programID = GL45.glCreateProgram()
        // bind Attributes
        format.attributeNames.withIndex().forEach {
            GL45.glBindAttribLocation(programID, it.index, it.value)
        }

        GL45.glAttachShader(programID, vertexShaderID)
        GL45.glAttachShader(programID, fragmentShaderID)
        extraShaders.forEach {
            GL45.glAttachShader(programID, it.id)
        }
        GL45.glLinkProgram(programID)
        GL45.glValidateProgram(programID)
    }

    /**
     * Activates this shader.
     */
    fun useShader() {
        GL45.glUseProgram(programID)
        updateUniforms()
    }

    /**
     * Deactivates this shader by setting the current gl program to 0.
     */
    fun stopShader() {
        if (hasColorEffect) colorEffect.updateValue(ColorEffect.DEFAULT.id)
        GL45.glUseProgram(0)
    }

    /**
     * Sets the shaders special coloring effect.
     *
     * This will only work for shaders which support the desired effect.
     */
    fun setColorEffect(effect: ColorEffect): Boolean {
        if (!hasColorEffect) return false
        colorEffect.updateValue(effect.id)
        return true
    }

    /**
     * Enables or disables the chroma effect.
     *
     * This will only work for shaders which support this effect.
     */
    fun setChroma(newState: Boolean): Boolean {
        if (!hasColorEffect) return false
        colorEffect.updateValue(if (newState) ColorEffect.CHROMA.id else ColorEffect.DEFAULT.id)
        return true
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
     * Attempts to reload and recompile the shader from the corresponding files.
     *
     * If there is an error the old shader will persist and the error is printed to the logs.
     * @throws Exception when there was an error with compiling the shader or validation the program.
     * @throws IOException when the shader could not be read.
     */
    @Throws(Exception::class, IOException::class)
    fun reloadShader() {
        shaderBuffer.clear()
        val newVertexShaderID = loadShader(vertexFile, GL45.GL_VERTEX_SHADER)
        val newFragmentShaderID = loadShader(fragmentFile, GL45.GL_FRAGMENT_SHADER)
        val newShaders = mutableListOf<ShaderFile>()
        extraShaders.forEach {
            val glId = loadShader(it.fileName, it.type)
            newShaders.add(ShaderFile(it.fileName, it.type, glId))
        }
        val newProgramID = GL45.glCreateProgram()
        // bind Attributes
        format.attributeNames.withIndex().forEach {
            GL45.glBindAttribLocation(newProgramID, it.index, it.value)
        }
        GL45.glAttachShader(newProgramID, newVertexShaderID)
        GL45.glAttachShader(newProgramID, newFragmentShaderID)
        newShaders.forEach {
            GL45.glAttachShader(newProgramID, it.id)
        }
        GL45.glLinkProgram(newProgramID)
        if (GL45.glGetProgrami(newProgramID, GL45.GL_LINK_STATUS) != GL45.GL_TRUE) {
            val errorMessage = GL45.glGetProgramInfoLog(newProgramID, 10000)
            Mithras.logger.error(errorMessage)
            throw Exception("Program linking failed for ${this::class.simpleName}.", Exception(errorMessage))
        }

        GL45.glValidateProgram(newProgramID)
        if (GL45.glGetProgrami(newProgramID, GL45.GL_VALIDATE_STATUS) != GL45.GL_TRUE) {
            val errorMessage = GL45.glGetProgramInfoLog(newProgramID, 10000)
            Mithras.logger.error(errorMessage)
            throw Exception("Program validation failed for ${this::class.simpleName}.", Exception(errorMessage))
        }


        // glDeleteShader flags the shader for deletion for when it is no longer attached to a program.
        GL45.glDeleteShader(vertexShaderID)
        GL45.glDeleteShader(fragmentShaderID)
        extraShaders.forEach {
            GL45.glDeleteShader(it.id)
        }
        GL45.glDeleteProgram(programID)

        programID = newProgramID
        vertexShaderID = newVertexShaderID
        fragmentShaderID = newFragmentShaderID
        extraShaders = newShaders


        // Update the IDs of the uniforms to the new program
        uniforms.forEach { it.updateID(programID) }
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
            val shaderId = GL45.glCreateShader(type)
            GL45.glShaderSource(shaderId, builder)
            GL45.glCompileShader(shaderId)

            if (GL45.glGetShaderi(shaderId, GL45.GL_COMPILE_STATUS) == GL45.GL_FALSE){
                val errorMessage = GL45.glGetShaderInfoLog(shaderId, 10000)
                Mithras.logger.error(errorMessage)
                Mithras.logger.debug(GL45.glGetShaderSource(shaderId))
                throw Exception("Failed loading shader $file of type $type" , Exception(errorMessage))
            }

            return@putShader shaderId
        }
    }

    override fun close() {
        GL45.glDeleteShader(vertexShaderID)
        GL45.glDeleteShader(fragmentShaderID)
        GL45.glDeleteProgram(programID)
        uniforms.forEach { it.close() }
    }

    data class ShaderFile(val fileName: String, val type: Int, val id: Int)

    data class ShaderSource(val processor: ShaderPreprocessor, val path: String)

    /**
     * A coloring effect for the fragment shader.
     */
    enum class ColorEffect(val id: Int) {
        /**
         * The default coloring behaviour.
         * This will usually be the interpolated vertex color attribute.
         */
        DEFAULT(0),
        CHROMA(1)
    }

    companion object {
        /**
         * Already loaded shaders mapped to their gl shader id.
         */
        private val shaderBuffer: MutableMap<ShaderSource, Int> = mutableMapOf()

        fun clearLoadBuffers() {
            shaderBuffer.clear()
            ShaderPreprocessor.clearIncludeBuffer()
        }

        //chroma settings
        private var chromaSizeInternal: Float = 0f
        private var chromaAngleInternal: Float = 0f
        private var chromaSpeedInternal: Float = 0f

        /**
         * Expects a [size] between 0 and 1.
         */
        @JvmStatic
        fun setChromaSize(size: Float) {
            chromaSizeInternal = (exp( size * 0.5f ) -1f)  / 30f
        }

        /**
         * Angle of the chroma effect in degrees.
         */
        @JvmStatic
        fun setChromaAngle(angle: Float) {
            chromaAngleInternal = (angle + 90f) * 0.017453292f
        }

        /**
         * Expects a [speed] value between 0 and 1.
         */
        @JvmStatic
        fun setChromaSpeed(speed: Float) {
            chromaSpeedInternal = (1 - exp(speed) ) / 20f
        }


        private fun getShaderType(fileName: String): Int? {
            return when(fileName.substringAfterLast(".")) {
                "vert" -> GL45.GL_VERTEX_SHADER
                "frag" -> GL45.GL_FRAGMENT_SHADER
                "geom" -> GL45.GL_GEOMETRY_SHADER
                "comp" -> GL45.GL_COMPUTE_SHADER
                "tesc" -> GL45.GL_TESS_CONTROL_SHADER
                "tese" -> GL45.GL_TESS_EVALUATION_SHADER
                else -> null
            }
        }
    }
}
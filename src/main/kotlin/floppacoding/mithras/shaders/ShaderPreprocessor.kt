package floppacoding.mithras.shaders

import floppacoding.mithras.Mithras
import java.io.InputStream

/**
 * # A custom preprocessor for GLSL shader programs.
 *
 * This preprocessor can be used before the Open GL preprocessor when loading in the shader source from an input stream.
 * To do so use [processShader].
 *
 * ## Features
 * ### Includes
 * This preprocessor allows you to use include statements that will load in other shader files.
 * The include statement has to look like one of the following:
 *
 *      #include "filename"
 *      #include "path/filename"
 * The filename may contain 'a-zA-Z_.' but no '/'. If path is omitted it will be assumed to be 'shaders/include/'.
 * The preprocessor will then attempt to load the file "/assets/<resourceDomain>/<>path><filename>".
 * If it can be loaded successfully it will be inserted in place of the include statement.
 * Otherwise, the include statement ets removed and skipped.
 *
 * The included file will not be affected by the preprocessor.
 *
 * ### Redefining GLSL preprocessor constants.
 * You can also redeclare the value of GLSL's '#define' preprocessor constants.
 * For this pass teh corresponding [Redefine] to the constructor.
 *
 * @author Aton
 */
class ShaderPreprocessor(private val resourceDomain: String) {

    constructor(resourceDomain: String, args: Collection<PreprocessorArgument>) : this(resourceDomain) {
        args.forEach {
            when(it) {
                is Redefine -> redeclarations[it.name] = it.redeclaredValue
            }
        }
    }

    private val redeclarations = mutableMapOf<String, String>()

    fun processShader(input: InputStream): StringBuilder {
        val builder = StringBuilder()
        input.bufferedReader().useLines {
            it.forEach { line -> builder.append(processLine(line)) }
        }
        return builder
    }

    fun processLine(line: String): String {
        val includeMatch = includePattern.matchEntire (line)
        if (includeMatch != null) {
            val path = includeMatch.groups["path"]?.value ?: "shaders/include/"
            val filename = includeMatch.groups["filename"]?.value ?: run {
                // This should be impossible to reach.
                Mithras.logger.error("Failed parsing filename for shader include statement for: $line")
                return ""
            }
            val fullPath = "/assets/$resourceDomain/${path}$filename"
            val source = includeBuffer.getOrPut(fullPath) {
                val stream = this.javaClass.getResourceAsStream(fullPath) ?: run{
                    Mithras.logger.error("Failed resolving file for shader include statement. File not found: '$fullPath' for include statement '$line'")
                    return ""
                }
                val builder = StringBuilder()
                stream.bufferedReader().useLines {
                    it.forEach { line -> if (!line.matches(versionPattern)) builder.append(line).append("\n") }
                }
                return builder.toString()
            }
            return source
        }

        val defineMatch = definePattern.matchEntire (line)
        if (defineMatch != null) run redeclare@{
            val name = defineMatch.groups["name"]?.value ?: return@redeclare
            val newDeclaration = redeclarations[name] ?: return@redeclare
            return "#define $name $newDeclaration\n"
        }

        return  "$line\n"
    }

    companion object {
        private val includeBuffer = mutableMapOf<String, String>()

        fun clearIncludeBuffer() {
            includeBuffer.clear()
        }

        private val includePattern = Regex("^\\s*#include\\s*\"(?<path>[\\w.]+/)?(?<filename>[\\w.]+)\"\\s*\$")
        private val versionPattern = Regex("^\\s*#version\\s*(?<version>\\d+)\\s*\$")
        private val definePattern = Regex("^\\s*#define\\s*(?<name>\\w+)\\s[\\s\\w.]*\$")
    }

    override fun equals(other: Any?): Boolean {
        return other is ShaderPreprocessor && resourceDomain == other.resourceDomain
                && redeclarations.size == other.redeclarations.size
                && redeclarations.all { other.redeclarations[it.key] == it.value }
    }

    override fun hashCode(): Int {
        var result = resourceDomain.hashCode()
        result = 31 * result + redeclarations.hashCode()
        return result
    }
}
package floppacoding.mithras.shaders

sealed class PreprocessorArgument

/**
 * Allows you to redefine preprocessor constants.
 * Example:
 *
 *      Redefine("MAX_VERTICES", "128")
 * will replace
 *
 *      #define MAX_VERTICES 67
 *
 * with
 *
 *      #define MAX_VERTICES 128
 */
class Redefine(val name: String, val redeclaredValue: String) : PreprocessorArgument() {
    override fun equals(other: Any?): Boolean {
        return other is Redefine && name == other.name && redeclaredValue == other.redeclaredValue
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + redeclaredValue.hashCode()
        return result
    }
}
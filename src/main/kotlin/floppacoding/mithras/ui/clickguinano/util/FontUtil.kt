package floppacoding.mithras.ui.clickguinano.util

import java.util.*

/**
 * Provides methods for rending text.
 *
 * @author Aton
 */
object FontUtil {
    /**
     * Returns a copy of the String where the only first letter is capitalized.
     */
    fun String.capitalizeOnlyFirst(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length).lowercase()
    }
}
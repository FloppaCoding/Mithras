package floppacoding.mithras.ui.clickgui.util

import java.util.*

/**
 * Provides methods for rending text.
 *
 * @author Aton
 */
object FontUtil {
    // TODO remove this. The only method in here is redundant as it already exists in Extensions.
    /**
     * Returns a copy of the String where the only first letter is capitalized.
     */
    fun String.capitalizeOnlyFirst(): String {
        return this.substring(0, 1).uppercase(Locale.getDefault()) + this.substring(1, this.length).lowercase()
    }
}
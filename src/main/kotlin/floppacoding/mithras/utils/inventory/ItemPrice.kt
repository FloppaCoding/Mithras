package floppacoding.mithras.utils.inventory

import floppacoding.mithras.utils.inventory.ItemPrice.Format.*
import java.text.NumberFormat
import java.util.*

object ItemPrice {

    fun Double?.formatPrice(format: Format = DEFAULT_FORMAT): String {
        return if (this != null) format(this) else "null"
    }

    fun formatSkyblock(price: Double): String {
        return skyblock.format(price).replace("$","")
    }

    /**
     * Expresses the formatting for price values.
     * - [SKYBLOCK]: 4,784,851.0
     * - [SHORT]: 4.7B
     * - [LONG]: 4.7 billion
     */
    enum class Format(val formatter: (Double) -> String) {
        /**
         * Should make the price look like "852,173,387.0"
         */
        SKYBLOCK(::formatSkyblock),
        SHORT(shortFormat::format),
        LONG(longFormat::format);

        operator fun invoke(p1: Double): String {
            return formatter(p1)
        }
    }

    private val skyblock = NumberFormat.getCurrencyInstance(Locale.US)
    private val shortFormat = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)
    private val longFormat = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG)

    val DEFAULT_FORMAT = SKYBLOCK

    init {
        skyblock.maximumFractionDigits = 1
        longFormat.minimumFractionDigits = 1
        shortFormat.minimumFractionDigits = 1
    }
}
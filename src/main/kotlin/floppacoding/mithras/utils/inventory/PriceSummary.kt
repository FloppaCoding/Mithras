package floppacoding.mithras.utils.inventory

import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.ChatUtils.setHoverItem
import floppacoding.mithras.utils.ChatUtils.setHoverText
import floppacoding.mithras.utils.inventory.PriceSummary.Format.*
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import java.text.NumberFormat
import java.util.*

/**
 * A container class for tracking and formatting the price data of an item.
 *
 * @author Aton
 */
class PriceSummary @JvmOverloads constructor(
    /**
     * Underlying stack for which this is the price summary.
     * Is not required to be set.
     */
    val stack: ItemStack? = null
) {
    /**
     * The value a single item of the stack.
     * @see totalPrice
     */
    var singlePrice: Double = 0.0
        private set

    /**
     * Number of identical items (in the stack).
     */
    var count: Int = stack?.count ?: 1

    /**
     * Total price of the stack accounting for the number of items.
     * @see singlePrice
     */
    val totalPrice: Double
        get() = singlePrice * count

    /**
     * A list of contributions to the total price.
     */
    val summary: MutableList<Triple<String, Double, Int>> = mutableListOf()

    /**
     * Adds another contribution to the total price.
     */
    fun addContribution(name: String, price: Double, count: Int = 1) {
        summary.add(Triple(name, price, count))
        singlePrice += price * count
    }

    /**
     * Sorts the price contributions in descending order.
     */
    fun sortSummary() {
        summary.sortBy { -it.second * it.third }
    }

    /**
     * Returns a Text element consisting of a hoverable preview of the underlying [stack] followed by its total value.
     * The total value will show all contributions when hovered.
     */
    @JvmOverloads
    fun createHoverableText(messageFormat: Format = DEFAULT_FORMAT, hoverFormat: Format = DEFAULT_FORMAT) : MutableText {
        val baseText: MutableText = stack?.let { it.name.copy().setHoverItem(it) } ?: ChatUtils.literalText("Stack")
        baseText.append(" §ris Worth ")
        baseText.append(createHoverablePriceBreakDown(messageFormat, hoverFormat))
        return baseText
    }

    /**
     * Returns a hover able text displaying the items price as well as the breakdown of contributions.
     */
    @JvmOverloads
    fun createHoverablePriceBreakDown(messageFormat: Format = DEFAULT_FORMAT, hoverFormat: Format = DEFAULT_FORMAT) : MutableText {
        sortSummary()
        val totalWorth = toString(messageFormat)
        val hoverTopLine = ChatUtils.literalText("${ChatUtils.GREEN}${count}${ChatUtils.GRAY}x ")
            .append(stack?.name ?: ChatUtils.literalText("${ChatUtils.DARK_AQUA}Total"))
            .append(" ${ChatUtils.DARK_GRAY}- ${ChatUtils.GOLD}$totalWorth§r\n")
        return ChatUtils.literalText("${ChatUtils.GOLD}$totalWorth").setHoverText(hoverTopLine.append(summary.joinToString("§r\n") {
                    "${ChatUtils.GREEN}${it.third}${ChatUtils.GRAY}x §r${it.first} ${ChatUtils.DARK_GRAY}- ${ChatUtils.GOLD}${hoverFormat(it.second)}"
                })
            )
    }

    /**
     * Converts the total price to a string.
     */
    override fun toString(): String {
        return this.toString(DEFAULT_FORMAT)
    }

    /**
     * Converts the total price to a string using the supplied [format].
     */
    fun toString(format: Format) : String{
        return format(totalPrice)
    }

    /**
     * Expresses the formatting for price values.
     *
     * - [SKYBLOCK]: 4,784,851.0
     * - [SHORT]: 4.7B
     * - [LONG]: 4.7 billion
     */
    enum class Format(val formatter: (Double) -> String) {
        /**
         * Should make the price look like "852,173,387.0"
         */
        SKYBLOCK(PriceSummary::formatSkyblock),
        SHORT(shortFormat::format),
        LONG(longFormat::format);

        operator fun invoke(p1: Double): String {
            return formatter(p1)
        }
    }

    companion object {
        private val skyblock = NumberFormat.getCurrencyInstance(Locale.US)
        private val shortFormat = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)
        private val longFormat = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG)

        val DEFAULT_FORMAT = SKYBLOCK

        fun formatSkyblock(price: Double): String {
            return skyblock.format(price).replace("$","")
        }

        init {
            skyblock.maximumFractionDigits = 1
            longFormat.minimumFractionDigits = 1
            shortFormat.minimumFractionDigits = 1
        }
    }
}
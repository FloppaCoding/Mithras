package floppacoding.mithras.utils.inventory

import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.ChatUtils.setHoverItem
import floppacoding.mithras.utils.ChatUtils.setHoverText
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText

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
    fun createHoverableText(messageFormat: ItemPrice.Format = DEFAULT_FORMAT, hoverFormat: ItemPrice.Format = DEFAULT_FORMAT) : MutableText {
        val baseText: MutableText = stack?.let { it.name.copy().setHoverItem(it) } ?: ChatUtils.literalText("Stack")
        baseText.append(" §ris Worth ")
        baseText.append(createHoverablePriceBreakDown(messageFormat, hoverFormat))
        return baseText
    }

    /**
     * Returns a hover able text displaying the items price as well as the breakdown of contributions.
     */
    @JvmOverloads
    fun createHoverablePriceBreakDown(messageFormat: ItemPrice.Format = DEFAULT_FORMAT, hoverFormat: ItemPrice.Format = DEFAULT_FORMAT) : MutableText {
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
    fun toString(format: ItemPrice.Format) : String{
        return format(totalPrice)
    }


    companion object {

        val DEFAULT_FORMAT = ItemPrice.Format.SKYBLOCK
    }
}
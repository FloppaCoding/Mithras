package floppacoding.mithras.utils.inventory

import floppacoding.mithras.utils.Extensions.capitalizeOnlyFirst
import floppacoding.mithras.utils.Extensions.snakeCaseToFirstCapitalized
import floppacoding.mithras.utils.inventory.ItemUtils.abilityScrolls
import floppacoding.mithras.utils.inventory.ItemUtils.dye
import floppacoding.mithras.utils.inventory.ItemUtils.extraAttributes
import floppacoding.mithras.utils.inventory.ItemUtils.gems
import floppacoding.mithras.utils.inventory.ItemUtils.hasArtOfWar
import floppacoding.mithras.utils.inventory.ItemUtils.hasEtherwarp
import floppacoding.mithras.utils.inventory.ItemUtils.hotPotatoBooks
import floppacoding.mithras.utils.inventory.ItemUtils.isRarityUpgraded
import floppacoding.mithras.utils.inventory.ItemUtils.itemID
import floppacoding.mithras.utils.inventory.ItemUtils.masterStars
import floppacoding.mithras.utils.inventory.ItemUtils.matchesItem
import floppacoding.mithras.utils.inventory.ItemUtils.powerAbilityScroll
import floppacoding.mithras.utils.inventory.ItemUtils.runes
import floppacoding.mithras.utils.inventory.ItemUtils.skyblockEnchantments
import floppacoding.mithras.utils.inventory.ItemUtils.transmissionTuners
import floppacoding.mithras.utils.network.BazaarAPI
import floppacoding.mithras.utils.network.LowestBinAPI
import net.minecraft.item.ItemStack
import kotlin.math.pow

/**
 * # Utilities for calculating item values.
 *
 * **The following attributes are accounted for**
 *
 *  * Base Price
 *      * The items base price gets retrieved from the bazaar or lowestbin api with its itemID
 *  * Enchantments
 *      * Enchantment values get taken directly from tbe bazaar if possible. This does account for efficiency requiring silexes.
 *      It also accounts for stacking enchants and combine price.
 *  * Recombobulator
 *  * Art of War
 *  * Gemstones
 *  * Hot and Fuming potato Books
 *  * Book of Stats
 *  * Wither Scrolls
 *  * Power Ability Scroll (gemstone scroll)
 *  * Transmission tuners
 *  * Etherwarp
 *  * Runes
 *  * Master Stars
 *  * Armor Dye
 *
 *  **The following attributes are ***NOT*** accounted for**
 *
 *  * Reforge
 *  * Attribute shards
 *  * Non-master stars
 *  * Skins
 *  * Pet items, level, candy
 *  * Exotics
 *
 *
 * @author Aton
 */
object ItemValueCalculator {
    // TODO
    //  account for attributes,
    //  (exotic?)
    //  skins
    //  stars
    //  pets - rarity, items, candy?
    //  reforge
    //

    /**
     * Calculates the values of the provided [itemStack].
     *
     * For a breakdown of which item properties get accounted for and how see [ItemValueCalculator].
     */
    fun getStackValue(itemStack: ItemStack) : PriceSummary {
        val priceSummary = PriceSummary(itemStack)
        val specialItem = identifySpecialItem(itemStack)

        // BaseItemValue
        priceSummary.addContribution(itemStack.itemID.snakeCaseToFirstCapitalized(), getBasePrice(itemStack.itemID))
        // Recomb
        if (itemStack.isRarityUpgraded) priceSummary.addContribution("Recombobulator 3000", getBasePrice("RECOMBOBULATOR_3000"))

        //If stacked item return here.
        if (itemStack.count > 1) return priceSummary

        // Only applicable to non-stackable items:
        // Enchants
        getItemEnchantmentsValue(itemStack, priceSummary, specialItem)
        // Potato Boobs
        getPotatoBookValue(itemStack, priceSummary)
        // Art of War
        if (itemStack.hasArtOfWar) priceSummary.addContribution("The Art of War", getBasePrice("THE_ART_OF_WAR"))
        // Gems
        getGemsValue(itemStack, priceSummary)
        // Ability Scroll
        itemStack.powerAbilityScroll?.let { priceSummary.addContribution(it.snakeCaseToFirstCapitalized(), LowestBinAPI.getPrice(it) ?: 0.0) }
        itemStack.abilityScrolls?.let { it.forEach { scroll -> priceSummary.addContribution(scroll.snakeCaseToFirstCapitalized(), LowestBinAPI.getPrice(scroll) ?: 0.0) } }
        // Transmission Tuner
        itemStack.transmissionTuners?.let { priceSummary.addContribution("Transmission Tuner", getBasePrice("TRANSMISSION_TUNER"), it) }
        // Etherwarp
        if (itemStack.hasEtherwarp) {
            priceSummary.addContribution("Etherwarp", LowestBinAPI.getPrice("ETHERWARP_CONDUIT") ?: 0.0)
            priceSummary.addContribution("Etherwarp Merger", LowestBinAPI.getPrice("ETHERWARP_MERGER") ?: 0.0)
        }
        // Runes
        itemStack.runes.forEach{ priceSummary.addContribution("${it.key.capitalizeOnlyFirst()} Rune ${it.value}", LowestBinAPI.getPrice("RUNE-${it.key}-${it.value}") ?: 0.0) }
        // Book of stats
        if (itemStack.extraAttributes?.keys?.contains("stats_book") == true ) priceSummary.addContribution("Book of Stats", getBasePrice("BOOK_OF_STATS"))
        // MasterStars
        getMasterStarValue(itemStack, priceSummary)
        // Dye
        itemStack.dye?.let { priceSummary.addContribution(it.snakeCaseToFirstCapitalized(), LowestBinAPI.getPrice(it) ?: 0.0) }




        return priceSummary
    }

    fun getPotatoBookValue(stack: ItemStack, summary: PriceSummary? = null): Double {
        val books = stack.hotPotatoBooks
        if (books == 0) return 0.0
        var price = getBasePrice("HOT_POTATO_BOOK")
        summary?.addContribution("Hot Potato Book", price, books.coerceAtMost(10))
        price *= books.coerceAtMost(10)
        if (books > 10) {
            val fumingBookPrice = getBasePrice("FUMING_POTATO_BOOK")
            summary?.addContribution("Fuming Potato Book", fumingBookPrice, books - 10)
            price += fumingBookPrice * (books - 10)
        }
        return price

    }

    fun getMasterStarValue(stack: ItemStack, summary: PriceSummary? = null): Double {
        val masterStars = stack.masterStars ?: return  0.0
        var price = 0.0
        for (ii in 1..masterStars.coerceAtMost(5)) {
            val ordinal = ordinals[ii]!!
            val id = "${ordinal}_MASTER_STAR"
            val starPrice = BazaarAPI.getPrice(id) ?: 0.0
            summary?.addContribution(id.snakeCaseToFirstCapitalized(), starPrice)
            price += starPrice
        }
        return price
    }

    fun getItemEnchantmentsValue(stack: ItemStack, summary: PriceSummary? = null, specialItem: SpecialItem? = null) : Double {
        var value = 0.0
        stack.skyblockEnchantments.forEach {
            value += getValueOfEnchant(it.key, it.value, summary, specialItem)
        }
        return value
    }

    fun getValueOfEnchant(name: String, level: Int, summary: PriceSummary? = null, specialItem: SpecialItem? = null): Double {
        // Stacking enchants
        if (stackableEnchants.contains(name)) {
            val basePrice = BazaarAPI.getPrice(ItemUtils.convertToEnchantID(name,1)) ?: 0.0
            summary?.addContribution(name.capitalizeOnlyFirst(),basePrice)
            return basePrice
        }

        // First try directly from BZ
        var id = ItemUtils.convertToEnchantID(name, level)
        var price = BazaarAPI.getPrice(id)
        if (price != null && price > 0) {
            summary?.addContribution(name.snakeCaseToFirstCapitalized() + " $level", price)
            return price
        }

        // Account for efficiency not being from books but silex.
        if (name == "efficiency" && level > 5 && specialItem != SpecialItem.STONK ) {
            price = BazaarAPI.getPrice("SIL_EX")
            if (price == null || price <= 0.01)
                price = LowestBinAPI.getPrice("SILEX") ?: 0.0
            summary?.addContribution("Silex", price, level - 5)
            return price * (level - 5)
        }

        // Check price of corresponding tier 1 enchant.
        val baseLevel = (level/6)*5 + 1
        id = ItemUtils.convertToEnchantID(name, baseLevel)
        price = BazaarAPI.getPrice(id)
        if (price != null && price > 0) {
            summary?.addContribution(name.snakeCaseToFirstCapitalized() + " $baseLevel", price, 2.0.pow(level - baseLevel).toInt())
            return price * 2.0.pow(level - baseLevel)
        }

        // If for some reason not found in the BZ data, check the lowest bin data which might also include BZ.
        id = "ENCHANTED_BOOK-${name.uppercase()}-$level"
        price =  LowestBinAPI.getPrice(id) ?: 0.0
        summary?.addContribution(name.snakeCaseToFirstCapitalized() + " $level", price)
        return price
    }

    fun getGemsValue(stack: ItemStack, summary: PriceSummary? = null) : Double{
        return stack.gems.toList().sumOf {
            val gemPrice = getBasePrice(it.first.itemID)
            summary?.addContribution(it.first.type.capitalizeOnlyFirst(), gemPrice, it.second)
            return@sumOf gemPrice * it.second
        }

    }

    /**
     * First checks the [BazaarAPI] and if no value found the [LowestBinAPI].
     * The item id for some items is different for the lowest bin api.
     */
    private fun getBasePrice(itemID: String): Double {
        return BazaarAPI.getPrice(itemID) ?: LowestBinAPI.getPrice(itemID) ?: 0.0
    }

    private fun identifySpecialItem(stack: ItemStack): SpecialItem? {
        return when {
            stack.matchesItem(SkyblockItems.STONK) -> SpecialItem.STONK
            else -> null
        }
    }

    private val stackableEnchants = listOf(
        "champion",
        "compact",
        "cultivating",
        "expertise",
        "hecatomb",
        )

    private val ordinals = mapOf<Int, String>(
        1 to "FIRST",
        2 to "SECOND",
        3 to "THIRD",
        4 to "FOURTH",
        5 to "FIFTH"
    )

    enum class SpecialItem {
        STONK,
    }
}
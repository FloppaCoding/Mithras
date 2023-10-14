package floppacoding.mithras.utils.inventory

import floppacoding.mithras.module.settings.impl.SelectorOptions
import net.minecraft.util.Formatting

/**
 * Skyblock item rarity enum.
 *
 * @author Aton
 */
enum class ItemRarity(val inGameName: String, val colorCode: Formatting): SelectorOptions {
    NONE("NONE", Formatting.GRAY),
    COMMON("COMMON", Formatting.WHITE),
    UNCOMMON("UNCOMMON", Formatting.GREEN),
    RARE("RARE", Formatting.BLUE),
    EPIC("EPIC", Formatting.DARK_PURPLE),
    LEGENDARY("LEGENDARY", Formatting.GOLD),
    MYTHIC("MYTHIC", Formatting.LIGHT_PURPLE),
    DIVINE("DIVINE", Formatting.AQUA),
    SPECIAL("SPECIAL", Formatting.RED),
    VERY_SPECIAL("VERY SPECIAL", Formatting.RED);

    /**
     * Makes it possible to use this for Selector Settings.
     * Is the same as [inGameName]
     */
    override val displayName: String = inGameName

    companion object {
        /**
         * Matches the unformatted line containing the items rarity in the lore.
         */
        val RARITY_PATTERN = Regex("^(?:a )?(?:SHINY )?(?<rarity>${entries.joinToString("|") { it.inGameName }})(?: (?<type>[A-Z]+(?: +[A-Z]+)*))?(?: a)?$")
    }
}
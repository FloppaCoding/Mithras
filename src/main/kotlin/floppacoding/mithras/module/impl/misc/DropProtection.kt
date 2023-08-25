package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.inventory.ItemRarity
import floppacoding.mithras.utils.inventory.ItemUtils.itemID
import floppacoding.mithras.utils.inventory.ItemUtils.skyblockRarity
import net.minecraft.item.ItemStack

object DropProtection : Module(
    "Drop Prevention",
    category = Category.MISC,
    description = "Prevents you from dropping your valuable items."
){
    private val rarityOptions = arrayOf(ItemRarity.NONE, ItemRarity.COMMON, ItemRarity.UNCOMMON, ItemRarity.RARE, ItemRarity.EPIC, ItemRarity.LEGENDARY)
    private val rarityLimit by SelectorSetting("Rarity Limit", ItemRarity.RARE, rarityOptions, description = "Items with a rarity equal or greater than this will not be allowed to be dropped.")

    fun shouldPrevntDrop(itemStack: ItemStack, fromHotbar: Boolean): Boolean {
        if (!this.enabled || !LocationManager.inSkyblock || (fromHotbar && LocationManager.inDungeons)) return false
        val itemID = itemStack.itemID
        if (blacklist.contains(itemID)) return true
        val rarity = itemStack.skyblockRarity
        if (rarity.ordinal >= rarityLimit.ordinal) return true
        return false
    }

    /**
     * These items are not allowed to be dropped.
     *
     * Taken from SBA.
     */
    private val blacklist = listOf(
        "PIGGY_BANK",
        "RADIANT_POWER_ORB",
        "GRAPPLING_HOOK",
        "MOODY_GRAPPLESHOOT",
        "WAND_OF_HEALING",
        "WAND_OF_MENDING",
        "WAND_OF_RESTORATION",
        "WAND_OF_ATONEMENT",
        "ENCHANTED_GLOWSTONE",
        "AATROX_BATPHONE",
        "ROYAL_PIGEON",
        "PERSONAL_COMPACTOR_4000",
        "PERSONAL_COMPACTOR_5000",
        "PERSONAL_COMPACTOR_6000",
        "PERSONAL_COMPACTOR_7000",
        "MATHEMATICAL_HOE_BLUEPRINT",
        "THEORETICAL_HOE",
        "THEORETICAL_HOE_CANE_1",
        "THEORETICAL_HOE_CANE_2",
        "THEORETICAL_HOE_CARROT_1",
        "THEORETICAL_HOE_CARROT_2",
        "THEORETICAL_HOE_POTATO_1",
        "THEORETICAL_HOE_POTATO_2",
        "THEORETICAL_HOE_WARTS_1",
        "THEORETICAL_HOE_WARTS_2",
        "THEORETICAL_HOE_WHEAT_1",
        "THEORETICAL_HOE_WHEAT_2",
        "JUNGLE_AXE",
        "TREECAPITATOR",
        "WAND_OF_STRENGTH",
        "ICE_SPRAY_WAND",
        "FIRE_VEIL_WAND",
        "PERFECT_RUBY_GEM",
        "PERFECT_SAPPHIRE_GEM",
        "PERFECT_JADE_GEM",
        "PERFECT_AMETHYST_GEM",
        "PERFECT_TOPAZ_GEM",
        "PERFECT_JASPER_GEM",
        "PERFECT_OPAL_GEM",
        "WARDEN_HEART",
        "WARDEN_HELMET",
        "JUDGEMENT_CORE",
        "ABIPHONE_X_PLUS",
        "ABIPHONE_X_PLUS_SPECIAL_EDITION",
        "ABIPHONE_XI_ULTRA",
        "ABIPHONE_XI_ULTRA_STYLE",
        "ABIPHONE_XII_MEGA",
        "ABIPHONE_XII_MEGA_COLOR",
        "ABIPHONE_XIII_PRO",
        "ABIPHONE_XIII_PRO_GIGA"
    )

    private val whitelist = listOf(
        "RUNE",
        "REMNANT_OF_THE_EYE",
        "DUNGEON_LORE_PAPER",
        "WINTER_DISC",
        "DUNGEON_DISC_1",
        "DUNGEON_DISC_2",
        "DUNGEON_DISC_3",
        "DUNGEON_DISC_4",
        "DUNGEON_DISC_5",
        "DUNGEON_STONE",
    )
}




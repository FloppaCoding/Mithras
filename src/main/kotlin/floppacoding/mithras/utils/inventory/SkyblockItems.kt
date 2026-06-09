package floppacoding.mithras.utils.inventory

/**
 * ## A collection of Skyblock items with data for those items.
 *
 * This class was made to make the use of skyblock [itemIDs][ItemUtils.itemID] easier for identifying items.
 * The [attributes] are also meant to make it easy to group similar items such as shortbows or witherblades,
 * so that those do not always have to be listed individually when it does not matter which one is used.
 *
 * Feel free to expand this list if you need any other items or attributes, or as new items are added to the game.
 *
 * @author Aton
 * @param itemID the Skyblock [itemID][ItemUtils.itemID] of an item
 * @param displayName the default [displayName][net.minecraft.item.ItemStack.getName] of the item without
 * formatting codes and possible extra elements such as reforge.
 * @see InventoryUtils
 * @see ItemUtils
 */
@Suppress("unused")
object SkyblockItems {

    // WEAPONS / TOOLS
    @JvmStatic val AOTV = NamedSkyblockItem("Aspect of the Void", "ASPECT_OF_THE_VOID")
    @JvmStatic val TERMINATOR = NamedSkyblockItem("Terminator", "TERMINATOR", ItemAttribute.SHORTBOW)
    @JvmStatic val JUJU = NamedSkyblockItem("Juju Shortbow", "JUJU_SHORTBOW", ItemAttribute.SHORTBOW)
    @JvmStatic val ARTISANAL_SHORTBOW = NamedSkyblockItem("Artisanal Shortbow", "ARTISANAL_SHORTBOW", ItemAttribute.SHORTBOW)
    @JvmStatic val SPIRIT_BOW = NamedSkyblockItem("Spirit Bow", "ITEM_SPIRIT_BOW", ItemAttribute.SHORTBOW)
    @JvmStatic val ICE_SPRAY = NamedSkyblockItem("Ice Spray Wand", "ICE_SPRAY_WAND")
    @JvmStatic val AOTE = NamedSkyblockItem("Aspect of the End", "ASPECT_OF_THE_END")
    @JvmStatic val NECRONS_BLADE = NamedSkyblockItem("Necron's Blade (Unrefined)", "NECRON_BLADE", ItemAttribute.WITHERBLADE)
    @JvmStatic val ASTRAEA = NamedSkyblockItem("Astraea", "ASTRAEA", ItemAttribute.WITHERBLADE)
    @JvmStatic val SCYLLA = NamedSkyblockItem("Scylla", "SCYLLA", ItemAttribute.WITHERBLADE)
    @JvmStatic val VALKYRIE = NamedSkyblockItem("Valkyrie", "VALKYRIE", ItemAttribute.WITHERBLADE)
    @JvmStatic val HYPERION = NamedSkyblockItem("Hyperion", "HYPERION", ItemAttribute.WITHERBLADE)
    @JvmStatic val AOTD = NamedSkyblockItem("Aspect of the Dragons", "ASPECT_OF_THE_DRAGON")
    @JvmStatic val SOUL_WHIP = NamedSkyblockItem("Soul Whip", "SOUL_WHIP")
    @JvmStatic val WITHER_CLOAK = NamedSkyblockItem("Wither Cloak Sword", "WITHER_CLOAK")
    @JvmStatic val CLAYMORE = NamedSkyblockItem("Dark Claymore", "DARK_CLAYMORE")
    @JvmStatic val GIANTS_SWORD = NamedSkyblockItem("Giant's Sword", "GIANTS_SWORD")
    @JvmStatic val TRIBAL_SPEAR = NamedSkyblockItem("Tribal Spear", "TRIBAL_SPEAR")
    @JvmStatic val BONEMERANG = NamedSkyblockItem("Bonemerang", "BONE_BOOMERANG")
    @JvmStatic val JERRY_GUN = NamedSkyblockItem("Jerry-chine Gun", "JERRY_STAFF")
    /** @see BONZO_STAFF_FRAGGED */
    @JvmStatic val BONZO_STAFF = NamedSkyblockItem("Bonzo's Staff", "BONZO_STAFF")
    /**
     * The actual Item name will be "⚚ Reforge Bonzo's Staff". The ⚚ is omitted here so that the name can still match an item name even when a reforge is present.
     * @see BONZO_STAFF*/
    @JvmStatic val BONZO_STAFF_FRAGGED = NamedSkyblockItem("Bonzo's Staff", "STARRED_BONZO_STAFF")
    @JvmStatic val LEAPING_SWORD = NamedSkyblockItem("Leaping Sword", "LEAPING_SWORD")
    @JvmStatic val SILK_EDGE_SWORD = NamedSkyblockItem("Silk-Edge Sword", "SILK_EDGE_SWORD")
    @JvmStatic val AOTS = NamedSkyblockItem("Axe of the Shredded", "AXE_OF_THE_SHREDDED")

    // TOOLS
    @JvmStatic val STONK = NamedSkyblockItem("Stonk", "STONK_PICKAXE")

    //ARMOR
    @JvmStatic val SPRING_BOOTS = NamedSkyblockItem("Spring Boots", "SPRING_BOOTS", ItemAttribute.ARMOR)

    //MISC
    @JvmStatic val SPIRIT_LEAP = NamedSkyblockItem("Spirit Leap", "SPIRIT_LEAP")
    @JvmStatic val INFINILEAP = NamedSkyblockItem("Infinileap", "INFINITE_SPIRIT_LEAP")
    @JvmStatic val INFLATABLE_JERRY = NamedSkyblockItem("Inflatable Jerry", "INFLATABLE_JERRY")

    @JvmStatic val ASPECT_OF_THE_VOID = AOTV
    @JvmStatic val ASPECT_OF_THE_END = AOTE
    @JvmStatic val ASPECT_OF_THE_DRAGON = AOTD
    @JvmStatic val AXE_OF_THE_SHREDDED = AOTS

}
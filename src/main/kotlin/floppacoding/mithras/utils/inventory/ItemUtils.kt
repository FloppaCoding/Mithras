package floppacoding.mithras.utils.inventory

import net.minecraft.item.ItemStack
import net.minecraft.nbt.AbstractNbtNumber
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.text.Text

/**
 * ## A collection of methods for accessing NBT data of Skyblock items.
 *
 * Parts of this class are based on
 * [SBC by Harry282](https://github.com/Harry282/Skyblock-Client/blob/main/src/main/kotlin/skyblockclient/utils/Utils.kt) -
 * [APGL-3.0 license](https://github.com/Harry282/Skyblock-Client/blob/main/LICENSE)
 *
 * @author Aton
 */
@Suppress("unused")
object ItemUtils {
    val ItemStack.extraAttributes: NbtCompound?
        get() = this.getSubNbt("ExtraAttributes")

    val ItemStack.isDungeonMobDrop: Boolean
        get() {
            val attributes = this.extraAttributes
            return attributes.hasKey("baseStatBoostPercentage") && !attributes.hasKey("dungeon_item_level")
        }

    /**
     * The stat boost of dungeon drops. Value should be in between 0 and 50.
     */
    val ItemStack.statBoost: Int?
        get() {
            return this.extraAttributes?.getInteger("baseStatBoostPercentage")
        }

    /**
     * Returns true if the Skbylock item is recombobulated, false otherwise.
     */
    val ItemStack.isRarityUpgraded: Boolean
        get() {
            return (this.extraAttributes?.getInt("rarity_upgrades") ?: 0) > 0
        }

    val ItemStack.isStarred: Boolean
        get() {
            return (this.extraAttributes?.getInt("upgrade_level") ?: 0) > 0
        }

    /**
     * The skyblock item ID
     *
     * See [SkyblockItem] for an incomplete list of itemIDs.
     */
    val ItemStack.itemID: String
        get() {
            return this.extraAttributes?.getString("id") ?: ""
        }

    val ItemStack.reforge : String
        get() {
            return this.extraAttributes?.getString("modifier") ?: ""
        }

    /**
     * Gets the lore attribute of the item.
     * The strings will **NOT** contain formatting codes.
     * Example:
     *
     *      LEGENDARY SWORD
     * @see formattedLore
     */
    val ItemStack.lore: List<String>
        get() {
            val display = this.getSubNbt("display") ?: return emptyList()
            if (display.contains("Lore", NbtElement.LIST_TYPE.toInt())) {
                val nbtList = display.getList("Lore", NbtElement.STRING_TYPE.toInt())
                val lore = ArrayList<String>()
                for (ii in 0 until nbtList.size) {
                    // Use the following line instead of the try catch to get the formatting. That formatting will look
                    // according to MutableText.toString(), which is very unreadable, but might be required for more
                    // information in the future.
                    // lore.add(nbtList.getString(ii))
                    try {
                        lore.add(Text.Serializer.fromJson(nbtList.getString((ii)))?.string ?: "")
                    } catch (_: Exception) {
                        lore.add(nbtList.getString(ii))
                    }
                }
                return lore
            }
            return emptyList()
        }

    /**
     * Gets the lore attribute of the item.
     * The strings **WILL** contain formatting codes.
     * Example:
     *
     *      {"italic":false,"extra":[{"bold":true,"color":"gold","text":"LEGENDARY SWORD"}],"text":""}
     * @see lore
     */
    val ItemStack.formattedLore: List<String>
        get() {
            val display = this.getSubNbt("display") ?: return emptyList()
            if (display.contains("Lore", NbtElement.LIST_TYPE.toInt())) {
                val nbtList = display.getList("Lore", NbtElement.STRING_TYPE.toInt())
                val lore = ArrayList<String>()
                for (ii in 0 until nbtList.size) {
                     lore.add(nbtList.getString(ii))
                }
                return lore
            }
            return emptyList()
        }

    /**
     * Gets the skyblock item rarity of this item.
     * If none could be found [ItemRarity.NONE] will be returned.
     */
    val ItemStack.skyblockRarity: ItemRarity
        get() {
            this.lore.reversed().forEach { line ->
                // matchEntire and find both work here
                val match = ItemRarity.RARITY_PATTERN.find(line) ?: return@forEach
                return ItemRarity.entries.find { it.inGameName == match.groups["rarity"]?.value } ?: ItemRarity.NONE
            }
            return ItemRarity.NONE
        }

    /**
     * Checks whether the item has a right click ability.
     */
    val ItemStack?.hasAbility: Boolean
        get() {
            return this?.lore?.any {it.contains("Ability:") && it.endsWith("RIGHT CLICK")} == true
        }

/**
 * Checks the item's lore for whether it is a shortbow.
 *
 * See also [SkyblockItem] for a list of skyblock items.
 */
    val ItemStack?.isShortbow: Boolean
        get() {
            return this?.lore?.any { it.contains("Shortbow: Instantly shoots!") } == true
        }

    private fun NbtCompound?.hasKey(key: String) : Boolean {
        return this?.contains(key) ?: false
    }

    /**
     * Returns the interger value associanted with the [key].
     * If the associated value is of a different number type than integer, it will be converted.
     * If the value is not a number or, there is no entry for the key null is returned.
     *
     * This behaves differently than the vanilla method [NbtCompound.getInt], which returns 0 instead of null.
     */
    private fun NbtCompound.getInteger(key: String): Int? {
        try {
            if (this.contains(key, NbtElement.NUMBER_TYPE.toInt())) {
                return (this.get(key) as AbstractNbtNumber).intValue()
            }
        } catch (_: ClassCastException) { }
        return null
    }
}
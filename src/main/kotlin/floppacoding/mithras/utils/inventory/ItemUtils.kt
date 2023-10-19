package floppacoding.mithras.utils.inventory

import floppacoding.mithras.utils.inventory.ItemUtils.powerAbilityScroll
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

    /**
     * Returns true if the Skbylock item has an art of war applied, false otherwise.
     */
    val ItemStack.hasArtOfWar: Boolean
        get() {
            return (this.extraAttributes?.getInt("art_of_war_count") ?: 0) > 0
        }

    val ItemStack.stars: Int
        get() {
            return this.extraAttributes?.getInteger("upgrade_level") ?: this.extraAttributes?.getInteger("dungeon_item_level") ?: 0
        }

    /**
     * Returns the number of master stars on this item or null if none applied.
     */
    val ItemStack.masterStars : Int?
        get() {
            val dungeonStars = this.extraAttributes?.getInteger("dungeon_item_level")?.minus(5) ?: 0
            return if (dungeonStars > 0)
                dungeonStars
            else null
        }

    val ItemStack.dye: String?
        get() {
            return extraAttributes?.getStringOrNull("dye_item")
        }

    val ItemStack.isStarred: Boolean
        get() {
            return this.stars > 0
        }

    val ItemStack.hotPotatoBooks: Int
        get() {
            return this.extraAttributes?.getInt("hot_potato_count") ?: 0
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

    val ItemStack.reforge : String?
        get() {
            return extraAttributes?.getStringOrNull("modifier")
        }

    val ItemStack.hasEtherwarp : Boolean
        get() = this.extraAttributes?.getBoolean("ethermerge") ?: false

    val ItemStack.transmissionTuners : Int?
        get() = this.extraAttributes?.getInteger("tuned_transmission")?.div(2)

    /**
     * The applied power ability Scroll (gemstone scroll).
     * @see abilityScrolls
     */
    val ItemStack.powerAbilityScroll: String?
        get() {
            return extraAttributes?.getStringOrNull("power_ability_scroll")
        }

    /**
     * Returns a list of the ability scrolls present on this item. This applies for wither scrolls.
     * @see powerAbilityScroll
     */
    val ItemStack.abilityScrolls: List<String>?
        get() {
            return this.extraAttributes?.getList("ability_scroll", NbtElement.STRING_TYPE.toInt())?.map { it.asString() }
        }


    // TODO implement text lore.
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
     * Returns a map of enchantment name and level.
     * If no enchantments present returns and empty map.
     *
     * @see [convertToEnchantID]
     * @see [skyblockEnchantmentIDs]
     */
    val ItemStack.skyblockEnchantments: Map<String, Int>
        get() {
            val attributes = this.extraAttributes ?: return emptyMap()
            if (!attributes.contains("enchantments", NbtElement.COMPOUND_TYPE.toInt())) return emptyMap()
            val enchants = attributes.getCompound("enchantments")
            return enchants.keys.associateWith { enchants.getInt(it) }
        }

    /**
     * Returns a list of the IDs of the enchantments on this item.
     * These IDs are what is used by the bazaar.
     *
     * @see [skyblockEnchantments]
     */
    val ItemStack.skyblockEnchantmentIDs: List<String>
        get() {
            return this.skyblockEnchantments.map { convertToEnchantID(it.key, it.value) }
        }

    /**
     * Returns a map of all applied gemstones together with the count.
     */
    val ItemStack.gems: Map<Gemstone, Int>
        get() {
            val attributes = this.extraAttributes ?: return emptyMap()
            if (!attributes.contains("gems", NbtElement.COMPOUND_TYPE.toInt())) return emptyMap()
            val gems = attributes.getCompound("gems")
            val gemMap = mutableMapOf<Gemstone, Int>()
            for (slot in gems.keys) {
                val matcher = gemSlotRegex.matchEntire(slot) ?: continue
                val slotName = matcher.groups["slot"]?.value ?: continue
                val gemInfoName = slot + "_gem"
                val gemType: String = if (gems.contains(gemInfoName, NbtElement.STRING_TYPE.toInt())) {
                    gems.getString(gemInfoName)
                } else {
                    slotName
                }
                val qualityname = if (gems.contains(slot, NbtElement.COMPOUND_TYPE.toInt())) {
                    gems.getCompound(slot).getString("quality")
                } else {
                    gems.getString(slot)
                }
                val quality = try {
                    Gemstone.Quality.valueOf(qualityname)
                } catch (_: Exception) {
                    continue
                }

                // check whether gem alr in the map and if so increment
                gemMap.keys.find { it.type == gemType && it.quality == quality }?.let {
                    gemMap[it] = gemMap[it]?.plus(1) ?: 1
                } ?: let { gemMap[Gemstone(gemType, quality)] = 1 }
            }

            return gemMap
        }

    /**
     * Returns a map of rune name and level.
     * If no runes are present returns and empty map.
     */
    val ItemStack.runes: Map<String, Int>
        get() {
            val attributes = this.extraAttributes ?: return emptyMap()
            if (!attributes.contains("runes", NbtElement.COMPOUND_TYPE.toInt())) return emptyMap()
            val runes = attributes.getCompound("runes")
            return runes.keys.associateWith { runes.getInt(it) }
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

    fun ItemStack.matchesItem(item: SkyblockItem): Boolean = this.itemID == item.itemID

    val ItemStack.skyblockItem : SkyblockItem?
        get() {
            val itemID = this.itemID
            return SkyblockItem.entries.find { it.itemID == itemID }
        }

    /**
     * Maps the key and value of the enchantment in ExtraAttributes -> enchantments to the corresponding enchantment id,
     * as is used by the bazaar.
     */
    fun convertToEnchantID(name: String, level: Int): String {
        return "ENCHANTMENT_${name.uppercase()}_$level"
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

    /**
     * Returns the string value associated with this [key].
     * If the associated value is not of type string or does not exist returns null.
     *
     * This behaves differently than [NbtCompound.getString]
     */
    private fun NbtCompound.getStringOrNull(key: String) : String? {
        return if (this.contains(key, NbtElement.STRING_TYPE.toInt())) {
            this.getString(key)
        }else null
    }

    private val gemSlotRegex = Regex("^(?<slot>[A-Z]+)_[\\d]$")
}
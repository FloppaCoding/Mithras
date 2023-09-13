package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.GuiSlotClickEvent
import floppacoding.mithras.events.HotbarDropEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.module.settings.impl.SelectorSetting
import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.Extensions.containsOneOf
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.SoundManager
import floppacoding.mithras.utils.inventory.ItemRarity
import floppacoding.mithras.utils.inventory.ItemUtils.isDungeonMobDrop
import floppacoding.mithras.utils.inventory.ItemUtils.isRarityUpgraded
import floppacoding.mithras.utils.inventory.ItemUtils.itemID
import floppacoding.mithras.utils.inventory.ItemUtils.lore
import floppacoding.mithras.utils.inventory.ItemUtils.skyblockRarity
import floppacoding.mithras.utils.inventory.ItemUtils.statBoost
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Blocks
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text

/**
 * A module to prevent the loss of valuable items through selling, dropping or salvaging.
 *
 * Parts of this Module are based on
 * [Skytils](https://github.com/Skytils/SkytilsMod) - [APGL-3.0 license](https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE.md)
 * and [SBA](https://github.com/BiscuitDevelopment/SkyblockAddons) - [MIT license](https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/LICENSE).
 *
 * @author Aton
 */
object ItemProtection : Module(
    "Drop Prevention",
    category = Category.MISC,
    description = "Prevents you from dropping, selling and salvaging your valuable items."
) {
    private val rarityOptions = arrayOf(
        ItemRarity.NONE,
        ItemRarity.COMMON,
        ItemRarity.UNCOMMON,
        ItemRarity.RARE,
        ItemRarity.EPIC,
        ItemRarity.LEGENDARY
    )
    private val rarityLimit by SelectorSetting(
        "Rarity Limit",
        ItemRarity.RARE,
        rarityOptions,
        description = "Items with a rarity equal or greater than this will not be allowed to be dropped."
    )
    private val protectRarityUpgraded by BooleanSetting("Protect Rarity Upgraded", true, description = "Protects all rarity upgraded items from being dropped or sold. This does get bypassed for dungeon mob drops.")
    private val protectGoodDungeonDrops by BooleanSetting("Protect +50% drops", true, description = "Protects dungeon mob drops with max stat boost from being salvaged.")

    private val soundNotification by BooleanSetting("Notification Sound", true, description = "Plays a sound when the mod stops you from dropping / selling an item.")
    private val chatInfo by BooleanSetting("Chat Info", false, description = "Shows a chat message when the mod stops you from dropping / selling an item.")
    private val notificationVolume by NumberSetting("Notification Volume", 0.5f, 0f, 1f, 0.01f, description = "The volume of the notification sound.")

    // TODO There is a lot of redundancy for sell and drop protection currently. This is to make it easier to adjust the
    //  individual rules, but it might be good to clean it up later on.

    private fun shouldPrevntDrop(itemStack: ItemStack, fromHotbar: Boolean): Boolean {
        val itemID = itemStack.itemID
        if (blacklist.contains(itemID)) return true
        if (dropWhitelist.contains(itemID)) return false
        if (itemStack.isDungeonMobDrop) {
            if (!protectGoodDungeonDrops || itemStack.statBoost != 50)
                return false
        }
        if (protectRarityUpgraded && itemStack.isRarityUpgraded) return true
        val rarity = itemStack.skyblockRarity
        if (rarity.ordinal >= rarityLimit.ordinal) return true
        return false
    }

    private fun shouldPreventSell(itemStack: ItemStack): Boolean {
        val itemID = itemStack.itemID
        if (blacklist.contains(itemID)) return true
        if (sellWhitelist.contains(itemID)) return false
        if (itemStack.isDungeonMobDrop) {
            if (!protectGoodDungeonDrops || itemStack.statBoost != 50)
                return false
        }
        if (protectRarityUpgraded && itemStack.isRarityUpgraded) return true
        val rarity = itemStack.skyblockRarity
        if (rarity.ordinal >= rarityLimit.ordinal) return true
        return false
    }
    private fun shouldPreventOutOfWindowClickDrop(itemStack: ItemStack): Boolean = shouldPrevntDrop(itemStack, false)
    private fun shouldPreventSalvage(itemStack: ItemStack?): Boolean {
        if (itemStack == null) return false
        if (blacklist.contains(itemStack.itemID)) return true
        if (!itemStack.isDungeonMobDrop) return true
        if (protectGoodDungeonDrops && itemStack.statBoost == 50) return true
        return false
    }

    @EventHandler
    fun onHotbarDrop(event: HotbarDropEvent) {
        // Does not prevent hotbar drops in dungeons so that class abilities can be used.
        // The item will still be dropped client side which looks weird.
        if (!LocationManager.inSkyblock || LocationManager.inDungeons) return
        if (shouldPrevntDrop(event.stack, true)) {
            event.cancel()
            notifyStopped(Text.literal("Stopped you from dropping your ").append(event.stack.name))
            return
        }
    }

    @EventHandler
    fun onSlotCLick(event: GuiSlotClickEvent<*>) {
        if (!LocationManager.inSkyblock) return
        // First check gui specific rules like npc sell and salvaging. Afterward general drop preventions are checked.
        if (event.handler is GenericContainerScreenHandler && event.slot != null && event.slot.hasStack() && event.slot.inventory === mc.player?.inventory) {
            val inventory = event.handler.inventory
            val inventoryName = event.inventoryName.string
            val stack: ItemStack = event.slot.stack ?: return
            if (inventoryName.startsWith("Salvage")) {
                if (shouldPreventSalvage(stack)) {
                    event.cancel()
                    notifyStopped(Text.literal("Stopped you from salvaging your ").append(stack.name))
                    return
                }
            }
            else if (!inventoryName.containsOneOf("Chest", "Auction") && inSalesMenu(inventory)) {
                if (shouldPreventSell(stack)) {
                    event.cancel()
                    notifyStopped(Text.literal("Stopped you from selling your ").append(stack.name))
                    return
                }
            }
        }
        // Tossing item by clicking outside the inventory.
        if (event.slotId == -999 && event.handler.cursorStack != null && event.actionType != SlotActionType.QUICK_CRAFT) {
            val stack = event.handler.cursorStack
            if (shouldPreventOutOfWindowClickDrop(stack)) {
                event.cancel()
                notifyStopped(Text.literal("Stopped you from dropping your ").append(stack.name))
                return
            }
        }
        // Tossing items by pressing the drop key on the item.
        if (event.actionType == SlotActionType.THROW && event.slotId != -999 && event.slot != null && event.slot.hasStack()) {
            val stack = event.slot.stack
            if (shouldPrevntDrop(stack, false)) {
                event.cancel()
                notifyStopped(Text.literal("Stopped you from dropping your ").append(stack.name))
                return
            }
        }
    }

    /**
     * Determines whether the [inventory] can be a Skyblock sales npc menu.
     *
     * Does not check the name of the inventory, since this information is not contained in the [Inventory] class.
     */
    private fun inSalesMenu(inventory: Inventory): Boolean {
        if (inventory.size() != 54) return false
        val sellItem = inventory.getStack(49)
        return sellItem != null
            && (sellItem.item === Blocks.HOPPER.asItem()
            && sellItem.name.string.contains("Sell Item")
            || sellItem.lore.any { s: String -> s.contains("buyback") })
    }

    private fun notifyStopped(message: String) = notifyStopped(Text.literal(message))

    private fun notifyStopped(text: Text) {
        if (soundNotification) SoundManager.playSound(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), notificationVolume, 1f, true)
        if (chatInfo) ChatUtils.modMessage(text)
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

    /**
     * Always allow these items to be dropped.
     *
     */
    private val dropWhitelist = listOf(
        "RUNE",
        "REMNANT_OF_THE_EYE",
        "WINTER_DISC",
        "DUNGEON_DISC_1",
        "DUNGEON_DISC_2",
        "DUNGEON_DISC_3",
        "DUNGEON_DISC_4",
        "DUNGEON_DISC_5",
        "DUNGEON_STONE",

        // Dungeon Drops
        "DUNGEON_LORE_PAPER",
        "REVIVE_STONE",
        "POTION",
        "PREMIUM_FLESH",
        "BEATING_HEART",
        "VITAMIN_DEATH",
    )

    /**
     * Always allow these items to be sold.
     */
    private val sellWhitelist = listOf(
        *dropWhitelist.toTypedArray(),

        // Gems
        "ROUGH_RUBY_GEM",
        "FLAWED_RUBY_GEM",
        "FINE_RUBY_GEM",
        "FLAWLESS_RUBY_GEM",
        "PERFECT_RUBY_GEM",
        "ROUGH_AMBER_GEM",
        "FLAWED_AMBER_GEM",
        "FINE_AMBER_GEM",
        "FLAWLESS_AMBER_GEM",
        "PERFECT_AMBER_GEM",
        "ROUGH_SAPPHIRE_GEM",
        "FLAWED_SAPPHIRE_GEM",
        "FINE_SAPPHIRE_GEM",
        "FLAWLESS_SAPPHIRE_GEM",
        "PERFECT_SAPPHIRE_GEM",
        "ROUGH_JADE_GEM",
        "FLAWED_JADE_GEM",
        "FINE_JADE_GEM",
        "FLAWLESS_JADE_GEM",
        "PERFECT_JADE_GEM",
        "ROUGH_AMETHYST_GEM",
        "FLAWED_AMETHYST_GEM",
        "FINE_AMETHYST_GEM",
        "FLAWLESS_AMETHYST_GEM",
        "PERFECT_AMETHYST_GEM",
        "ROUGH_TOPAZ_GEM",
        "FLAWED_TOPAZ_GEM",
        "FINE_TOPAZ_GEM",
        "FLAWLESS_TOPAZ_GEM",
        "PERFECT_TOPAZ_GEM",
        "ROUGH_JASPER_GEM",
        "FLAWED_JASPER_GEM",
        "FINE_JASPER_GEM",
        "FLAWLESS_JASPER_GEM",
        "PERFECT_JASPER_GEM",
        "ROUGH_OPAL_GEM",
        "FLAWED_OPAL_GEM",
        "FINE_OPAL_GEM",
        "FLAWLESS_OPAL_GEM",
        "PERFECT_OPAL_GEM"
    )
}




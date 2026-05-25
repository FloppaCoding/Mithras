package floppacoding.mithras.module.impl.player

import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.config.StorageConfig
import floppacoding.mithras.events.*
import floppacoding.mithras.mixin.gui.HandledScreenAccessor
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Setting.Companion.onSet
import floppacoding.mithras.module.settings.Setting.Companion.withDependency
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.ui.core.elements.GuiElement
import floppacoding.mithras.utils.ChatUtils.stripControlCodes
import floppacoding.mithras.utils.inventory.ItemUtils.lore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.screen.slot.Slot
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.awt.Color

/**
 * A collection of features concerning inventories.
 *
 * @author Aton
 */
object InventoryTweaks: Module(
    "Inventory Tweaks",
    category = Category.PLAYER,
    description = "A collection of features for inventories."
) {
    private val searchInventories by BooleanSetting("Inventory Search", true, description = "Allows you to search for Items in your inventory.")
    private val searchLore by BooleanSetting("Search Item Lore", true, description = "Also searches in the items lore.").
            withDependency { searchInventories }
    private val hideStatusEffects by BooleanSetting("Hide Status Effects", true, description = "Prevents the rendering of status effects in the inventory.")
    private val hideEffectsHud by BooleanSetting("Hide Effects HUD", true, description = "Prevents the rendering of the Status effects in game HUD.")
    private val storagePreview by BooleanSetting("Storage Preview", true, description = "Shows a preview of the contents of your skyblock storage when hovered.").
        onSet { input ->
            if (input) {
                if (!storageConfig.loaded) Mithras.scope.launch(Dispatchers.IO) {
                    storageConfig.loadConfig()
                }
            }else {
                if (storageConfig.dirty) Mithras.scope.launch(Dispatchers.IO) {
                    storageConfig.saveConfig()
                }
            }
        }

    private val searchFiled: SearchFiled by lazy { SearchFiled() }
    val storageConfig: StorageConfig by lazy { StorageConfig(Mithras.configPath) }

    override fun onInitialize() {
        super.onInitialize()
        if (enabled && storagePreview && !storageConfig.loaded) Mithras.scope.launch(Dispatchers.IO) {
            storageConfig.loadConfig()
        }
    }

    override fun onEnable() {
        super.onEnable()
        if (storagePreview && !storageConfig.loaded) Mithras.scope.launch(Dispatchers.IO) {
            storageConfig.loadConfig()
        }
    }

    override fun onDisable() {
        super.onDisable()
        if (storagePreview && storageConfig.dirty) Mithras.scope.launch(Dispatchers.IO) {
            storageConfig.saveConfig()
        }
    }

    @JvmStatic fun isInventorySearchEnabled(): Boolean {return this.enabled && this.searchInventories}
    @JvmStatic fun shouldHideStatusEffects(): Boolean {return this.enabled && this.hideStatusEffects}
    @JvmStatic fun shouldHideEffectsHud(): Boolean {return this.enabled && this.hideEffectsHud}

    @EventHandler
    private fun onJoinHypixel(event: JoinHypixelEvent) {
        if (storagePreview && !storageConfig.loaded) Mithras.scope.launch(Dispatchers.IO) {
            storageConfig.loadConfig()
        }
    }

    /**
     * Buffer the content of the Skyblock storage.
     */
    @EventHandler
    private fun onInventoryUpdate(event: SimpleInventoryUpdateEvent) {
        if (!storagePreview) return
        val screen = mc.currentScreen ?: return
        if ((screen as? GenericContainerScreen)?.screenHandler?.inventory !== event.inventory) return
        val chestName = screen.title.string.stripControlCodes()
        val match = ECHEST_PATTERN.find(chestName) ?: BACKPACK_PATTERN.find(chestName) ?: return
        var page = match.groups["page"]?.value?.toIntOrNull() ?: return
        val offset = if (chestName.startsWith("Ender")) -1 else 8
        page += offset

        storageConfig.inventories.getOrNull(page)?.let { inventory ->
            inventory.clear()
            for (ii in 9..<event.inventory.size()) {
                event.inventory.getStack(ii).let { inventory.setStack(ii-9, it.copy()) }
            }
            storageConfig.dirty = true
        }
    }

    /**
     * Save storage content.
     */
    @EventHandler
    private fun onGuiClose(event: GuiCloseEvent) {
        if (storagePreview && storageConfig.dirty) Mithras.scope.launch(Dispatchers.IO) {
            storageConfig.saveConfig()
        }
    }

    /**
     * Draw storage preview as tooltip.
     */
    @EventHandler
    private fun onDrawTooltip(event: DrawItemTooltipEvent) {
        if (!storagePreview) return
        if (event.screen !is GenericContainerScreen) return
        if(event.screen.title.string != "Storage") return
        val stackName = event.stack.name.string
        val match = ECHEST_ITEM_PATTERN.find(stackName) ?: BACKPACK_ITEM_PATTERN.find(stackName) ?: return
        var page = match.groups["page"]?.value?.toIntOrNull() ?: return
        val offset = if (stackName.startsWith("Ender")) -1 else 8
        page += offset

        val inventory = storageConfig.inventories.getOrNull(page) ?: return

        val backgroundWidth = (event.screen as HandledScreenAccessor).backgroundWidth
        event.context.matrices.pushMatrix()
        event.context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, event.x, event.y, 0.0F, 0.0F, backgroundWidth, 7, 256, 256)
        event.context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, event.x, event.y+7, 0.0F, 17.0F, backgroundWidth, this.ROWS * 18, 256, 256)
        event.context.drawTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, event.x, event.y + this.ROWS * 18 + 7, 0.0F, 216.0F, backgroundWidth, 6, 256, 256)


        for (i in 0..< ROWS) {
            for (j in 0..< 9) {
                val slot = Slot(inventory, j + i * 9, 8 + j * 18+event.x, 8 + i * 18+event.y)
                drawSlot(event.context, slot, backgroundWidth, mc.textRenderer)
            }
        }
        event.context.matrices.popMatrix()
        event.cancel()
    }

    /**
     * Draw item search highlights.
     */
    @EventHandler
    private fun onDrawSlow(event: DrawSlotEvent<*>) {
        if (!searchInventories) return
        if (searchFiled.searchPhrase == "") return
        if (!event.slot.hasStack()) return
        var highlight = false
        val stack = event.slot.stack
        if (stack.name.string.stripControlCodes().contains(searchFiled.searchPhrase, true)) {
            highlight = true
        }else if (searchLore && stack.lore.joinToString(" ").contains(searchFiled.searchPhrase, true) ) {
            highlight = true
        }
        if (highlight) {
            event.context.fill(event.slot.x, event.slot.y, event.slot.x + 16, event.slot.y + 16,Color(0,255,0).rgb)
        }
    }

    @JvmStatic fun getAndRepositionSearchFiled() : GuiElement {
        searchFiled.reposition()
        return searchFiled
    }

    /**
     * Draw the item slots for the storage preview, optionally with a highlight.
     */
    private fun drawSlot(context: DrawContext, slot: Slot, backgroundWidth: Int, textRenderer: TextRenderer) {
        val i = slot.x
        val j = slot.y
        val itemStack = slot.stack
        var bl2 = false

        context.matrices.pushMatrix()
        if (itemStack.isEmpty && slot.isEnabled) {
            val identifier = slot.backgroundSprite
            if (identifier != null) {
                context.drawGuiTexture(
                    RenderPipelines.GUI_TEXTURED,
                    identifier,
                    i,
                    j,
                    16,
                    16
                )
                bl2 = true
            }
        }

        if (!bl2) {

            run drawSearchHighlight@{

                if (!searchInventories) return@drawSearchHighlight
                if (searchFiled.searchPhrase == "") return@drawSearchHighlight
                var highlight = false
                if (itemStack.name.string.stripControlCodes().contains(searchFiled.searchPhrase, true)) {
                    highlight = true
                } else if (searchLore && itemStack.lore.joinToString(" ").contains(searchFiled.searchPhrase, true)) {
                    highlight = true
                }
                if (highlight) {
                    context.fill(
                        i,
                        j,
                        i + 16,
                        j + 16,
                        Color(0, 255, 0).rgb
                    )
                }
            }

            val k: Int = slot.x + slot.y * backgroundWidth
            if (slot.disablesDynamicDisplay()) {
                context.drawItemWithoutEntity(itemStack, i, j, k)
            } else {
                context.drawItem(itemStack, i, j, k)
            }

            context.drawStackOverlay(textRenderer, itemStack, i, j, null)
        }

        context.matrices.popMatrix()
    }

    /** Inventory name of the Skyblock Ender Chest */
    private val ECHEST_PATTERN = Regex("Ender Chest \\((?<page>\\d)/\\d\\)")
    /** Inventory name of the Skyblock Backpack */
    private val BACKPACK_PATTERN = Regex("(?:\\w+ )?Backpack \\(Slot #(?<page>\\d{1,2})\\)")
    /** Item name of the Skyblock Ender Chest */
    private val ECHEST_ITEM_PATTERN = Regex("Ender Chest Page (?<page>\\d)")
    /** Item name of the Skyblock Backpack */
    private val BACKPACK_ITEM_PATTERN = Regex("Backpack Slot (?<page>\\d{1,2})")

    private val TEXTURE = Identifier.ofVanilla("textures/gui/container/generic_54.png")
    private const val ROWS = 5

    private class SearchFiled : GuiElement() {

        private var listening = false

        var searchPhrase = ""

        init {
            this.width = 400f
            this.height = 30f
        }

        fun reposition() {
            this.x = (mc.window.framebufferWidth - width) / 2f
            this.y = mc.window.framebufferHeight - 50f
        }

        override fun close() {
            listening = false
            super.close()
        }


        override fun render(mouseX: Float, mouseY: Float, delta: Float) {
            renderer.push()
            renderer.translate(x,y)
            val color = if (listening)
                Color(200, 50, 50)
            else
                Color(50,200,50)
            renderer.rect(0f, 0f, width, height, Color(10, 200, 50, 50).rgb)
            renderer.border(0f, 0f, width, height, 2f, color.rgb)
            renderer.text (searchPhrase, 3f, 3f, -1, fontSize = 25f, textAlign = TextAlign.LEFT_TOP)
            renderer.pop()
        }

        /**
         * Handles interaction with this element.
         * Returns true if interacted with the element to cancel further interactions.
         */
        override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
            if (button == 0 && isMouseOver(mouseX, mouseY)) {
                listening = true
                return true
            }
            return super.mouseClicked(mouseX, mouseY, button)
        }

        /**
         * Register key strokes.
         */
        override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
            if (listening) {
                if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ENTER) {
                    listening = false
                    return true
                } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    searchPhrase = searchPhrase.dropLast(1)
                }
                return true
            }
            return super.keyPressed(keyCode, scanCode, modifiers)
        }

        override fun charTyped(chr: Char, modifiers: Int): Boolean {
            if (listening) {
                if (isValidChar(chr)) {
                    searchPhrase += chr.toString()
                    return true
                }
            }
            return super.charTyped(chr, modifiers)
        }

        private fun isValidChar(chr: Char): Boolean {
            return chr.code != 167 && chr >= ' ' && chr.code != 127
        }

    }
}
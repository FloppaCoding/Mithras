package floppacoding.mithras.module.impl.player

import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.Mithras
import floppacoding.mithras.events.DrawSlotEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Setting.Companion.withDependency
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.ui.core.elements.GuiElement
import floppacoding.mithras.utils.ChatUtils.stripControlCodes
import floppacoding.mithras.utils.inventory.ItemUtils.lore
import meteordevelopment.orbit.EventHandler
import org.lwjgl.glfw.GLFW
import java.awt.Color

object InventoryTweaks: Module(
    "Inventory Tweaks",
    category = Category.PLAYER,
    description = "A collection of features for inventories."
) {
    private val searchInventories by BooleanSetting("Inventory Search", true, description = "Allows you to search for Items in your inventory.")
    private val searchLore by BooleanSetting("Search Item Lore", true, description = "Also searches in the items lore.").
            withDependency { searchInventories }



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

    @JvmStatic fun isInventorySearchEnabled(): Boolean {return this.enabled && this.searchInventories}

    private val searchFiled: SearchFiled by lazy { SearchFiled() }

    @JvmStatic fun getAndRepositionSearchFiled() : SearchFiled {
        searchFiled.reposition()
        return searchFiled
    }

    class SearchFiled : GuiElement() {

        private var listening = false

        var searchPhrase = ""

        init {
            this.width = 400f
            this.height = 30f
        }

        fun reposition() {
            this.x = (Mithras.mc.window.framebufferWidth - width) / 2f
            this.y = Mithras.mc.window.framebufferHeight - 50f
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
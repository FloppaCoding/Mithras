package floppacoding.mithras.ui.clickgui

import floppacoding.aurora.core.TextAlign
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.moduleConfig
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.ui.GuiScreen
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.elements.menu.ElementColor
import floppacoding.mithras.ui.clickgui.elements.menu.ElementSlider
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.utils.render.ImageManager
import net.minecraft.client.input.CharInput
import net.minecraft.client.input.KeyInput
import net.minecraft.util.math.MathHelper
import org.lwjgl.glfw.GLFW
import java.io.IOException

/**
 * ## Main class of the Click GUI.
 *
 * Provides the gui which can be viewed in game.
 *
 * This class dispatches all rendering and input actions to the components of the GUI.
 *
 * Structure of the GUI is:
 * [ClickGUI] -> [Panel]s -> [ModuleButton][floppacoding.mithras.ui.clickgui.elements.ModuleButton]s
 * -> [Element][floppacoding.mithras.ui.clickgui.elements.Element]s.
 * Each component of the gui handles it own actions and dispatches them to its subcomponents.
 *
 * Partially based on HeroCode's gui.
 * The only reference to it, and my source for it is [this YouTube video](https://www.youtube.com/watch?v=JPb5rBzUVKE).
 *
 * @author Aton
 */
class ClickGUI : GuiScreen("Mithras GUI", 2f) {
    /**
     * Used to add a delay for closing the gui, so that it does not instantly get closed
     */
    private var openedTime = System.currentTimeMillis()
    /**
     * Used to create the advanced menu for modules
     */
    var advancedMenu: AdvancedMenu? = null

    override var blurBackground: Boolean
        get() = MainSettings.blur.enabled
        set(value) {}

    init {
        setUpPanels()
    }

    fun setUpPanels() {
        /** Create a panel for each module category */
        panels = ArrayList()
        for (category in Category.entries) {
            panels.add(Panel(category, this))
        }
    }

    /**
     * Dispatches all rendering for the GUI.
     */
    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        renderLogo()

        /* Calls all panels to render themselves and their module buttons and elements.
		  * Important to keep in mind: the panel rendered last will be on top.
          * For intuitive behaviour the panels have to be checked in reversed order for clicks.
          * This ensures that interactions will happen with the top panel. */
        for (p in panels) {
            p.drawScreen(mouseX, mouseY, delta)
        }

        if (advancedMenu != null) {
            advancedMenu?.drawScreen(mouseX, mouseY, delta)
        }
    }

    /**
     * Draws the Logo and the title.
     */
    private fun renderLogo() {
        val logoSize = 25f

        renderer.push()
        renderer.translate(windowWidth, windowHeight)

        renderer.scale(2f, 2f)

        renderer.image(ImageManager.ICON, -5f- logoSize, -5f - logoSize, logoSize, logoSize)
        renderer.text(
            MainSettings.clientName.text,
             - 10f - logoSize,
             - 5f - logoSize / 2f,
            ColorUtil.clickGUIColor.rgb,
            textAlign = TextAlign.RIGHT_MIDDLE
            )

        renderer.pop()
    }

    override fun mouseScrolled(mouseX: Float, mouseY: Float, amount: Float): Boolean {
        var i = MathHelper.clamp(amount, -1f, 1f).toInt()
        if (i != 0) {
            if (hasShiftDown()) {
                i *= 7
            }
            // Scroll the advanced gui
            if (advancedMenu?.scroll(i, mouseX, mouseY) == true) return true

            /** Checking all panels for scroll action.
             * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
            for (panel in panels.reversed()) {
                if (panel.scroll(i, mouseX, mouseY)) return true
            }
        }

        return super.mouseScrolled(mouseX, mouseY, amount)
    }

    /**
     * Dispatches mouse clicks to the [panels] and [advancedMenu].
     */
    override fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        // handle the advanced gui first
        if (advancedMenu?.mouseClicked(mouseX, mouseY, button) == true){
            // Update the elements of the corresponding module button
            val module = advancedMenu?.module ?: return true
            panels.find { it.category == module.category }?.moduleButtons?.find { it.module == module }?.updateElements()
            return true
        }

        /** Checking all panels for click action.
          * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
        for (panel in panels.reversed()) {
            if (panel.mouseClicked(mouseX, mouseY, button)) return true
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, button: Int): Boolean {
        // handle mouse release for advanced menu first
        advancedMenu?.mouseReleased(mouseX, mouseY, button)

        /** Checking all panels for mouse release action.
         * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
        for (panel in panels.reversed()) {
            panel.mouseReleased(mouseX, mouseY, button)
        }

        return super.mouseReleased(mouseX, mouseY, button)
    }

    /**
     * Handles key presses. Does not handle text field inputs.
     * @see charTyped
     */
    override fun keyPressed(input: KeyInput): Boolean {
        val keyCode = input.key
        val scanCode = input.scancode
        /** If in an advanced menu only hande that */
        if (advancedMenu != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && !advancedMenu!!.isListening()) {
                advancedMenu = null
            }
            return advancedMenu?.keyPressed(keyCode, scanCode) ?: true
        }

        /** For key registration in the menu elements.
         * Reversed order to check the panel on top first! */
        for (panel in panels.reversed()) {
            if (panel.keyPressed(keyCode, scanCode)) return true
        }

        /** Exits the menu when the toggle key is pressed */
        if (keyCode == MainSettings.keyBind.code && System.currentTimeMillis() - openedTime > 200) {
            this.close()
            return true
        }

        /** keyTyped in GuiScreen gets used to exit the gui on escape */
        return try {
            super.keyPressed(input)
        } catch (e2: IOException) {
            e2.printStackTrace()
            false
        }
    }

    /**
     * Handles text character inputs for text fields.
     * @see keyPressed
     */
    override fun charTyped(input: CharInput): Boolean {
        val chr = input.asString()[0]
        val modifiers =input.modifiers
        /** If in an advanced menu only hande that */
        if (advancedMenu != null) {
            return advancedMenu?.charTyped(chr, modifiers) ?: true
        }

        for (panel in panels.reversed()) {
            if (panel.charTyped(chr, modifiers)) return true
        }

        return super.charTyped(input)
    }

    override fun init()  {
        super.init()
        openedTime = System.currentTimeMillis()
        /** Start blur */
//        if (OpenGlHelper.shadersSupported && mc.renderViewEntity is EntityPlayer && MainSettings.blur.enabled) {
//            mc.entityRenderer.stopUseShader()
//            mc.entityRenderer.loadShader(ResourceLocation("shaders/post/blur.json"))
//        }

        /** update panel positions to make it possible to update the positions
         * this is required for loading the panel positions from the config and for resetting the gui */
        for (panel in panels) {
            panel.x = MainSettings.panelX[panel.category]!!.value.toFloat()
            panel.y = MainSettings.panelY[panel.category]!!.value.toFloat()
            panel.extended = MainSettings.panelExtended[panel.category]!!.enabled
        }
    }

    override fun close() {
        /** End blur */
//        mc.entityRenderer.stopUseShader()

        /** stop sliders from being active */
        for (panel in panels.reversed()) {
            if (panel.extended && panel.visible) {
                for (moduleButton in panel.moduleButtons) {
                    if (moduleButton.extended) {
                        for (menuElement in moduleButton.menuElements) {
                            if (menuElement is ElementSlider) {
                                menuElement.dragging = false
                            }
                            if (menuElement is ElementColor) {
                                menuElement.dragging = null
                            }
                        }
                    }
                }
            }
        }

        /** Save the changes to the config file */
        moduleConfig.saveConfig()

        super.close()
    }

    fun closeAllSettings() {
        for (panel in panels) {
            if (panel.visible && panel.extended && panel.moduleButtons.size > 0) {
                for (moduleButton in panel.moduleButtons) {
                    moduleButton.extended = false
                }
            }
        }
    }

    override var displayPerformance: Boolean = Mithras.DEBUG

    companion object {
        var panels: ArrayList<Panel> = arrayListOf()

    }
}
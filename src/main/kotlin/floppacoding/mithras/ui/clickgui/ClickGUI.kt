package floppacoding.mithras.ui.clickgui

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.Mithras.moduleConfig
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.ui.clickgui.advanced.AdvancedMenu
import floppacoding.mithras.ui.clickgui.elements.menu.ElementColor
import floppacoding.mithras.ui.clickgui.elements.menu.ElementSlider
import floppacoding.mithras.ui.clickgui.util.ColorUtil
import floppacoding.mithras.ui.clickgui.util.FontUtil
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier
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
class ClickGUI : Screen(MutableText.of(LiteralTextContent("Mithras GUI"))) {
    var scale = 2.0
    /**
     * Used to add a delay for closing the gui, so that it does not instantly get closed
     */
    private var openedTime = System.currentTimeMillis()
    /**
     * Used to create the advanced menu for modules
     */
    var advancedMenu: AdvancedMenu? = null

    init {
        FontUtil.setupFontUtils()
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
    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Scale the gui and the mouse coordinates
        // the handling of the mouse coordinates is not nice, since it has to be done in multiple places
        context.matrices.push()
        val window = mc.window
        val prevScale = mc.options.guiScale.value
        scale = CLICK_GUI_SCALE / window.scaleFactor
        mc.options.guiScale.value = 2
        context.matrices.scale(scale.toFloat(), scale.toFloat(), scale.toFloat())

        val scaledMouseX = getScaledMouseX()
        val scaledMouseY = getScaledMouseY()

        renderLogo(context)

        /* Calls all panels to render themselves and their module buttons and elements.
		  * Important to keep in mind: the panel rendered last will be on top.
          * For intuitive behaviour the panels have to be checked in reversed order for clicks.
          * This ensures that interactions will happen with the top panel. */
        for (p in panels) {
            p.drawScreen(context, scaledMouseX, scaledMouseY, partialTicks)
        }

        if(MainSettings.showUsageInfo.enabled) {
            renderUsageInfo(context)
        }

        if (advancedMenu != null) {            advancedMenu?.drawScreen(context, scaledMouseX, scaledMouseY, partialTicks)
        }

        /** Might be needed to use gui buttons */
        super.render(context, scaledMouseX, scaledMouseY, partialTicks)

        mc.options.guiScale.value = prevScale
        context.matrices.push()
    }

    /**
     * Draws the Logo and the title.
     */
    private fun renderLogo(context: DrawContext) {
        val window = mc.window
        val logoSize = 25

        context.matrices.push()
        context.matrices.translate(
            window.width.toDouble() / CLICK_GUI_SCALE,
            window.height.toDouble() / CLICK_GUI_SCALE,
            0.0
        )

        context.matrices.scale(2f, 2f, 2f)
        val titleWidth = FontUtil.getStringWidth(MainSettings.clientName.text)

//        RenderSystem.clearColor(255f, 255f, 255f, 255f)
//        mc.textureManager.bindTexture(LOGO)
        context.drawTexture(LOGO, - 5 - logoSize, -5 - logoSize, 0f, 0f, logoSize, logoSize, logoSize, logoSize)

        FontUtil.drawString(
            context,
            MainSettings.clientName.text,
            -titleWidth.toDouble() - 10.0 - logoSize,
            -FontUtil.fontHeight.toDouble() / 2.0 - 5.0 - logoSize / 2.0,
            ColorUtil.clickGUIColor.rgb
        )
        context.matrices.pop()
    }

    private fun renderUsageInfo(context: DrawContext) {
        val window = mc.window

        val lines = listOf("GUI Usage:",
            "Left click Module Buttons to toggle the Module.",
            "Right click Module Buttons to extend the Settings dropdown.",
            "Middle click Module Buttons to open the Advanced Gui.",
            "Disable this Overlay in the Advanced Settings of the Click Gui Module in the Render Category."
        )

        context.matrices.push()
        context.matrices.translate(
            window.width.toDouble() / CLICK_GUI_SCALE * 0.05,
            window.height.toDouble() / CLICK_GUI_SCALE * 0.7,
            0.0
        )

        context.matrices.scale(1.5f, 1.5f, 1.5f)
        for ((ii, line) in lines.withIndex()) {
            FontUtil.drawString(
                context,
                line,
                0.0,
                FontUtil.fontHeight.toDouble() * ii,
                ColorUtil.clickGUIColor.rgb
            )
        }
        context.matrices.pop()
    }

    /**
     * Handles scrolling.
     */
    @Throws(IOException::class)
    override fun mouseScrolled(mouseX: Double, mouseY: Double, amount: Double): Boolean {
        val scaledMouseX = getScaledMouseX()
        val scaledMouseY = getScaledMouseY()

        var i = MathHelper.clamp(amount, -1.0, 1.0).toInt()
        if (i != 0) {
            if (i > 1) {
                i = 1
            }
            if (i < -1) {
                i = -1
            }
            if (hasShiftDown()) {
                i *= 7
            }
            // Scroll the advanced gui
            if (advancedMenu?.scroll(i, scaledMouseX, scaledMouseY) == true) return true

            /** Checking all panels for scroll action.
             * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
            for (panel in panels.reversed()) {
                if (panel.scroll(i, scaledMouseX, scaledMouseY)) return true
            }
        }
        return super.mouseScrolled(scaledMouseX.toDouble(), scaledMouseY.toDouble(), amount)
    }

    /**
     * Dispatches mouse clicks to the [panels] and [advancedMenu].
     */
    override fun mouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int): Boolean {
        val scaledMouseX = getScaledMouseX()
        val scaledMouseY = getScaledMouseY()

        // handle the advanced gui first
        if (advancedMenu?.mouseClicked(scaledMouseX, scaledMouseY, mouseButton) == true){
            // Update the elements of the corresponding module button
            val module = advancedMenu?.module ?: return true
            panels.find { it.category == module.category }?.moduleButtons?.find { it.module == module }?.updateElements()
            return true
        }

        /** Checking all panels for click action.
          * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
        for (panel in panels.reversed()) {
            if (panel.mouseClicked(scaledMouseX, scaledMouseY, mouseButton)) return true
        }

        return try {
            super.mouseClicked(scaledMouseX.toDouble(), scaledMouseY.toDouble(), mouseButton)
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, state: Int): Boolean {
        val scaledMouseX = getScaledMouseX()
        val scaledMouseY = getScaledMouseY()

        // handle mouse release for advanced menu first
        advancedMenu?.mouseReleased(scaledMouseX, scaledMouseY, state)

        /** Checking all panels for mouse release action.
         * Reversed order is used to guarantee that the panel rendered on top will be handled first. */
        for (panel in panels.reversed()) {
            panel.mouseReleased(scaledMouseX, scaledMouseY, state)
        }

        return super.mouseReleased(scaledMouseX.toDouble(), scaledMouseY.toDouble(), state)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        /** If in an advanced menu only hande that */
        if (advancedMenu != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE && !advancedMenu!!.isListening()) {
                advancedMenu = null
            }
            return advancedMenu?.keyTyped(keyCode, scanCode) ?: true
        }

        /** For key registration in the menu elements. Required for text fields.
         * Reversed order to check the panel on top first! */
        for (panel in panels.reversed()) {
            if (panel.keyTyped(keyCode, scanCode)) return true
        }

        /** Exits the menu when the toggle key is pressed */
        if (keyCode == MainSettings.keyBind.code && System.currentTimeMillis() - openedTime > 200) {
            this.close()
            return true
        }

        /** keyTyped in GuiScreen gets used to exit the gui on escape */
        return try {
            super.keyPressed(keyCode, scanCode, modifiers)
        } catch (e2: IOException) {
            e2.printStackTrace()
            false
        }
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
            panel.x = MainSettings.panelX[panel.category]!!.value.toInt()
            panel.y = MainSettings.panelY[panel.category]!!.value.toInt()
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

    override fun shouldPause(): Boolean {
        return false
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

    fun getScaledMouseX(): Int {
        return MathHelper.ceil(mc.mouse.x / CLICK_GUI_SCALE)
    }
    fun getScaledMouseY(): Int {
        // maybe -1 or floor required here because of the inversion.
//        return MathHelper.ceil( (mc.window.height - mc.mouse.y) / CLICK_GUI_SCALE)
        return MathHelper.ceil( mc.mouse.y/ CLICK_GUI_SCALE)
    }

    companion object {
        const val CLICK_GUI_SCALE = 2.0
        var panels: ArrayList<Panel> = arrayListOf()

        private val LOGO = Identifier(Mithras.RESOURCE_DOMAIN, "gui/icon.png")
    }
}
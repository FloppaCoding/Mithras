package floppacoding.mithras.module.impl.render

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.AlwaysActive
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.*
import org.lwjgl.glfw.GLFW
import java.awt.Color

typealias GUIDesign = MainSettings.Design
typealias ColorMode = MainSettings.ColorMode
typealias PrefixStyle = MainSettings.PrefixStyle

/**
 * Settings for the CLick Gui
 * @author Aton
 */
@AlwaysActive
object MainSettings: Module(
    "Main Settings",
    category = Category.RENDER,
    description = "Appearance settings for the click gui. \n" +
            "You can set a custom chat prefix with formatting here. For formatting use & or the paragrph symbol followed by a modifier. " +
            "A benefit of using the paragraph symbol is, that you can directly see how it will look in the text field, but you wont be able to see the formatting. \n" +
            "§00...§ff§r are colors, l is §lBold§r, n is §nUnderlined§r, o is §oItalic§r, m is §mStrikethrough§r, k is §kObfuscated§r, r is Reset.",
    keyCode = GLFW.GLFW_KEY_RIGHT_SHIFT,
) {

    val design: SelectorSetting<Design> = +SelectorSetting("Design", Design.JELLYLIKE, description = "Design theme of the gui.")
    val blur: BooleanSetting = +BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val color = +ColorSetting("Color", Color(255,200,0), false, description = "Color theme in the gui.")
    val colorSettingMode = +SelectorSetting("Color Mode", ColorMode.HSB, description = "Mode for all color settings in the gui. Changes the way colors are put in.")
    val clientName: StringSetting = +StringSetting("Name", "Project Mithras", description = "Name that will be rendered in the gui.")
    val prefixStyle = +SelectorSetting("Prefix Style", PrefixStyle.LONG, description = "Chat prefix selection for mod messages.")
    val customPrefix = +StringSetting("Custom Prefix", "§0§l[§4§Project Mithras§0§l]§r", 40, description = "You can set a custom chat prefix that will be used when Custom is selected in the Prefix Style dropdown.")
    val chromaSize  by NumberSetting("Chroma Size",   0.5f, 0.0f,   1.0f, 0.01f, description = "Determines how rapidly the chroma pattern changes spatially.")
    val chromaSpeed by NumberSetting("Chroma Speed",  0.5f, 0.0f,   1.0f, 0.01f, description = "Determines how fast the chroma changes with time.")
    val chromaAngle by NumberSetting("Chroma Angle", 45.0f, 0.0f, 360.0f,  1.0f, description = "Determines the direction in which the chroma changes on your screen.")
    val showUsageInfo = +BooleanSetting("Usage Info", true, visibility = Visibility.ADVANCED_ONLY, description = "Show info on how to use the GUI.")
    val apiKey = +StringSetting("API Key", "", length = 100, visibility = Visibility.HIDDEN)

    const val ADVANCED_GUI_RELATIVE_WIDTH = 0.5
    const val ADVANCED_GUI_RELATIVE_HEIGHT = 0.5

    val advancedRelX = +NumberSetting("Advanced_RelX",(1 - ADVANCED_GUI_RELATIVE_WIDTH)/2.0,0.0, (1- ADVANCED_GUI_RELATIVE_WIDTH), 0.0001, visibility = Visibility.HIDDEN)
    val advancedRelY = +NumberSetting("Advanced_RelY",(1 - ADVANCED_GUI_RELATIVE_HEIGHT)/2.0,0.0, (1- ADVANCED_GUI_RELATIVE_HEIGHT), 0.0001, visibility = Visibility.HIDDEN)

    private const val PANEL_WIDTH = 120.0
    private const val PANEL_HEIGHT = 15.0

    val panelWidth  = NumberSetting("Panel width", default = PANEL_WIDTH, visibility = Visibility.HIDDEN)
    val panelHeight = NumberSetting("Panel height", default = PANEL_HEIGHT, visibility = Visibility.HIDDEN)

    val panelX: MutableMap<Category, NumberSetting<Double>> = mutableMapOf()
    val panelY: MutableMap<Category, NumberSetting<Double>> = mutableMapOf()
    val panelExtended: MutableMap<Category, BooleanSetting> = mutableMapOf()




    init {
        // The Panels

        // this will set the default click gui panel settings. These will be overwritten by the config once it is loaded
        resetPositions()

        addSettings(
            panelWidth,
            panelHeight
        )

        for(category in Category.entries) {
            addSettings(
                panelX[category]!!,
                panelY[category]!!,
                panelExtended[category]!!
            )
        }
    }

    /**
     * Adds if missing and sets the default click gui positions for the category panels.
     */
    fun resetPositions() {
        panelWidth.value = PANEL_WIDTH
        panelHeight.value = PANEL_HEIGHT

        var px = 10.0
        val py = 10.0
        val pxplus = panelWidth.value + 10
        for(category in Category.entries) {
            panelX.getOrPut(category) { NumberSetting(category.name + ",x", default = px, visibility = Visibility.HIDDEN) }.value = px
            panelY.getOrPut(category) { NumberSetting(category.name + ",y", default = py, visibility = Visibility.HIDDEN) }.value = py
            panelExtended.getOrPut(category) { BooleanSetting(category.name + ",extended", default = true, visibility = Visibility.HIDDEN) }.enabled = true
            px += pxplus
        }

        advancedRelX.reset()
        advancedRelY.reset()
    }

    /**
     * Overridden to prevent the chat message from being sent.
     */
    override fun onKeyBind() {
        this.toggle()
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        mc.send { mc.setScreen(Mithras.clickGUINano) }
        super.onEnable()
        toggle()
    }

    enum class Design(override val displayName: String): SelectorOptions {
        JELLYLIKE("Jellylike"), NEW("New")
    }

    enum class ColorMode(override val displayName: String): SelectorOptions {
        HSB("HSB"), RGB("RGB")
    }

    enum class PrefixStyle(override val displayName: String): SelectorOptions {
        LONG("Long"), SHORT("Short"), CUSTOM("Custom")
    }
}
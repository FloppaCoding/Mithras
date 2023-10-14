package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.MapRender
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.*
import java.awt.Color

typealias NameMode = DungeonMap.NameMode

/**
 * This Module functions as a setting storage for the dungeon map.
 */
object DungeonMap : Module(
    "Dungeon Map",
    category = Category.DUNGEON,
    description = "Renders the map from the Magical Map item in Dungeons on your screen." +
            "Check the ${MapRooms.name} module for more settings."
){
    // General
    val trackSecrets = BooleanSetting("Track Secrets", false, visibility = Visibility.HIDDEN, description = "Uses the Hypixel API to track how many secrets are collected in which room.")
    val hideInBoss = BooleanSetting("Hide in Boss", true, visibility = Visibility.VISIBLE, description = "Hides the map in boss.")
    val showRunInformation = BooleanSetting("Show Run Info", true, description = "Shows run information under map.")
    val playerNameMode = SelectorSetting("Player Names", NameMode.HOLDING_LEAP, visibility = Visibility.ADVANCED_ONLY, description = "Show player name under player head.")
    // Scaling
    val mapScale = NumberSetting("Map Scale",1.0f,0.1f,4.0f,0.01f, visibility = Visibility.ADVANCED_ONLY, description = "Scale of entire map.")
    val roomScale = NumberSetting("Dungeon Scale", 1.0f,0.5f,1.5f, 0.01f, description = "Scales the size of the displayed dungeon inside of the map HUD element.")
    val textScale = NumberSetting("Text Scale",0.75f,0.0f,2.0f,0.01f, description = "Scale of room names and secret counts relative to map size.")
    val playerHeadScale = NumberSetting("Head Scale",0.75f,0.0f,2.0f,0.01f, description = "Scale of player heads relative to map size.")
    // Spinny Map
    val spinnyMap = BooleanSetting("Spinny Map", false, description = "Centers the map on you and rotates it.")
    val centerOnPlayer = BooleanSetting("Center on Player", false, description = "Centers the map on your own Player Head.")
    // Border
    val mapBackground = ColorSetting("Background", Color(0, 0, 0, 100),true, visibility = Visibility.ADVANCED_ONLY, description = "Background Color for the map.")
    val mapBorder = ColorSetting("Border", Color(0, 0, 0, 255),true, visibility = Visibility.ADVANCED_ONLY, description = "Border Color for the map.")
    val chromaBorder = BooleanSetting("Chroma Border", true, visibility = Visibility.ADVANCED_ONLY, description = "Will add a chroma effect to your map border. The chroma can be configured in the ClickGui Module.")
    val mapBorderWidth = NumberSetting("Border Width",3.0f,0.0f,10.0f,0.1f, visibility = Visibility.ADVANCED_ONLY, description = "Map border width.")




    val xHud = NumberSetting("x", default = 0.0f, visibility = Visibility.HIDDEN)
    val yHud = NumberSetting("y", default = 0.0f, visibility = Visibility.HIDDEN)

    init {
        this.addSettings(
            trackSecrets,
            hideInBoss,
            showRunInformation,
            playerNameMode,
            mapScale,
            roomScale,
            textScale,
            playerHeadScale,
            spinnyMap,
            centerOnPlayer,
            mapBackground,
            mapBorder,
            chromaBorder,
            mapBorderWidth,
            xHud,
            yHud
        )
    }

    override fun onEnable() {
        Mithras.EVENT_BUS.subscribe(MapRender)
        super.onEnable()
    }

    override fun onDisable() {
        Mithras.EVENT_BUS.unsubscribe(MapRender)
        super.onDisable()
    }

    enum class NameMode(override val displayName: String): SelectorOptions {
        OFF("Off"), HOLDING_LEAP("Holding Leap"), ALWAYS("Always")
    }
}
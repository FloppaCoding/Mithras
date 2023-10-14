package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.*
import java.awt.Color

/**
 * This module serves as a setting storage for the dungeon map. the implementation is elsewhere.
 */
object MapRooms : Module(
    "Map Room Settings",
    category = Category.DUNGEON,
    description = "Appearance settings for the dungeon map."
){
    val darkenUndiscovered  = BooleanSetting("Darken Undiscovered",true, visibility = Visibility.HIDDEN, description = "Darkens unentered rooms.")
    val mapRoomNames        = SelectorSetting("Room Names", RoomNameMode.SPECIAL, description = "Shows names of rooms on map.")
    val mapRoomSecrets      = SelectorSetting("Room Secrets", SecretsMode.OFF, visibility = Visibility.HIDDEN, description = "Shows total secrets of rooms on map. REPLACE CHECKMARKS NOT ADDED!")
    val mapColorText        = BooleanSetting("Color Text",false, description = "Colors name and secret count based on room state.")
    val mapCheckmark        = SelectorSetting("Room Checkmarks", CheckmarkMode.NEU, description = "Adds room checkmarks based on room state.")
    val mapRoomTransparency = NumberSetting("Room Opacity",1.0,0.0, 1.0, 0.01, visibility = Visibility.HIDDEN)
    val mapDarkenPercent    = NumberSetting("DarkenMultiplier",0.4,0.0, 1.0, 0.01, visibility = Visibility.HIDDEN, description = "How much to darken undiscovered rooms")
    val colorBloodDoor      = ColorSetting("Blood Door", Color(231, 0, 0), true)
    val colorEntranceDoor   = ColorSetting("Entrance Door", Color(20, 133, 0), true)
    val colorRoomDoor       = ColorSetting("Normal Door", Color(92, 52, 14), true)
    val colorWitherDoor     = ColorSetting("Wither Door", Color(0, 0, 0), true)
    val colorOpenWitherDoor = ColorSetting("Opened Wither Door", Color(92, 52, 14), true)
    val colorBlood          = ColorSetting("Blood Room", Color(255, 0, 0), true)
    val colorEntrance       = ColorSetting("Entrance Room", Color(20, 133, 0), true)
    val colorFairy          = ColorSetting("Fairy Room", Color(224, 0, 255), true)
    val colorMiniboss       = ColorSetting("Miniboss Room", Color(254, 223, 0), true)
    val colorRoom           = ColorSetting("Normal Room", Color(107, 58, 17), true)
    val colorRoomMimic      = ColorSetting("Mimic Room", Color(186, 66, 52), true)
    val colorPuzzle         = ColorSetting("Puzzle Room", Color(117, 0, 133), true)
    val colorRare           = ColorSetting("Rare Room", Color(255, 203, 89), true)
    val colorTrap           = ColorSetting("Trap Room", Color(216, 127, 51), true)
    val colorUnexplored     = ColorSetting("Unexplored", Color(64, 64, 64), true)

    init {
        this.addSettings(
            darkenUndiscovered,
            mapRoomNames,
            mapRoomSecrets,
            mapColorText,
            mapCheckmark,
            mapRoomTransparency,
            mapDarkenPercent,
            colorBloodDoor,
            colorEntranceDoor,
            colorRoomDoor,
            colorWitherDoor,
            colorOpenWitherDoor,
            colorBlood,
            colorEntrance,
            colorFairy,
            colorMiniboss,
            colorRoom,
            colorRoomMimic,
            colorPuzzle,
            colorRare,
            colorTrap,
            colorUnexplored,
        )
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        super.onEnable()
        toggle()
    }

    /**
     * Prevent keybind Action.
     */
    override fun onKeyBind() {

    }

    enum class RoomNameMode(override val displayName: String): SelectorOptions {
        NONE("None"), SPECIAL("Puzzles / Trap"), ALL("All")
    }

    enum class SecretsMode(override val displayName: String): SelectorOptions {
        OFF("Off"), ON("On"), REPLACE_CHECKMARK("Replace Checkmark")
    }

    enum class CheckmarkMode(override val displayName: String): SelectorOptions {
        NONE("None"), DEFAULT("Default"), NEU("NEU")
    }
}
package floppacoding.mithras.module

import floppacoding.mithras.Mithras
import floppacoding.mithras.events.InputEvent
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.mithras.module.impl.debug.DebugModule
import floppacoding.mithras.module.impl.debug.RenderTest
import floppacoding.mithras.module.impl.debug.RenderTest2
import floppacoding.mithras.module.impl.dungeon.*
import floppacoding.mithras.module.impl.keybinds.AddKeybind
import floppacoding.mithras.module.impl.keybinds.KeyBind
import floppacoding.mithras.module.impl.misc.*
import floppacoding.mithras.module.impl.player.AutoSprint
import floppacoding.mithras.module.impl.player.DisableHotbarScroll
import floppacoding.mithras.module.impl.render.*
import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.ui.clickgui.ClickGUI
import floppacoding.mithras.ui.hud.EditHudGUI
import meteordevelopment.orbit.EventHandler

/**
 * # This object handles all the modules of the mod.
 *
 * After making a [Module] it just has to be added to the [modules] list and
 * everything else will be taken care of automatically. This entails:
 *
 * + It will be added to the click gui in the order it is put in here. But keep in mind that the category is set within
 * the module. The comments here are only for readability.
 *
 * + All settings that are registered within the module will be saved to and loaded from the module config.
 * For this to properly work remember to register the settings to the module.
 *
 * + The module will be registered and unregistered to the eventbus when it is enabled / disabled.
 *
 * + The module will be informed of its keybind presses.
 *
 *
 * @author Aton
 * @see Module
 * @see Setting
 */
object ModuleManager {
    /**
     * All modules have to be added to this list to function!
     */
    val modules: ArrayList<Module> = arrayListOf(
        //DUNGEON
        DungeonMap,
        MapRooms,
        TerminalSolvers,
        ThreeWeirdosSolver,
        BlazeSolver,
        TicTacToeSolver,
        CreeperBeamsSolver,
        TeleportSolver,
        StarMobHighlight,
        DungeonTimers,
        QuizSolver,
        IceFillSolver,
        WaterBoardSolver,


        //RENDER
        MainSettings,
        EditHud,
        CoordinateDisplay,
        ItemAnimations,
        Camera,
        ItemPhysics,
        Fullbright,
        Zoom,
        Particles,


        //PLAYER
        AutoSprint,
        DisableHotbarScroll,


        //MISC
        SmoothTransfer,
        KeepMousePosition,
        ScrollableTooltips,
        EtherwarpHighlight,
        ChatCleaner,
        ItemProtection,
        TreeGiftHud,
        BeaconSolver,


        //KEYBIND
        AddKeybind,

    )

    init {
        if (Mithras.DEBUG) {
            modules.addAll(listOf(
                DebugModule,
                RenderTest,
                RenderTest2,
            ))
        }
    }

    /**
     * Loads in all modules and their elements.
     *
     * This method also accesses instances of all modules and their hud elements.
     * That way all module instances are created and loaded into memory.
     *
     * This step is required before the config is loaded.
     */
    fun loadModules() {
        modules.forEach { it.loadModule() }
    }

    /**
     * Initialize the Modules.
     * This is run on game startup during the FMLInitializationEvent.
     */
    fun initializeModules() {
        modules.forEach {
            it.initializeModule()
            EditHudGUI.addHUDElements(it.hudElements)
        }
    }

    /**
     * Creates a new keybind module and adds it to the list.
     * The current gui will not be updated by this.
     */
    fun addNewKeybind(): KeyBind {
        val number = (modules
            .filter{module -> module.name.startsWith("New")}
            .map {module -> module.name.filter { c -> c.isDigit() }.toIntOrNull()}
            .maxByOrNull { it ?: 0} ?: 0) + 1
        val keyBind = KeyBind("New $number")
        modules.add(keyBind)
        return keyBind
    }

    /**
     * Removes the keybind. Also removes it from the click gui.
     */
    fun removeKeyBind(bind: KeyBind) {
        modules.remove(bind)
        ClickGUI.panels.find { it.category === Category.KEY_BIND }?.moduleButtons?.removeIf { it.module === bind }
    }

    /**
     * Handles the key binds for the modules.
     * This code is run before the vanilla minecraft code.
     */
    @EventHandler
    fun activateModuleKeyBinds(event: InputEvent) {
        if (event.action != InputEvent.PRESSED) return
        modules.stream().filter { module -> module.keyBind == event.key }.forEach { module -> module.onKeyBind() }
    }


    fun getModuleByName(name: String): Module? {
        return modules.find{ it.name.equals(name, ignoreCase = true) }
    }
}
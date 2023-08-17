package floppacoding.mithras.module

import floppacoding.mithras.events.InputEvent
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.mithras.module.impl.render.CoordinateDisplay
import floppacoding.mithras.module.impl.render.EditHud
import floppacoding.mithras.module.impl.render.ItemAnimations
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.Setting
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


        //RENDER
        MainSettings,
        EditHud,
        CoordinateDisplay,
        ItemAnimations,


        //PLAYER


        //MISC


        //KEYBIND

    )

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
     * Handles the key binds for the modules.
     * This code is run before the vanilla minecraft code.
     */
    @EventHandler
    fun activateModuleKeyBinds(event: InputEvent) {
        modules.stream().filter { module -> module.keyBind == event.key }.forEach { module -> module.onKeyBind() }
    }


    fun getModuleByName(name: String): Module? {
        return modules.find{ it.name.equals(name, ignoreCase = true) }
    }
}
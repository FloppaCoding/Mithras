package floppacoding.mithras.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import floppacoding.mithras.Mithras.MOD_NAME
import floppacoding.mithras.config.jsonutils.KeyDeserializer
import floppacoding.mithras.config.jsonutils.KeySerializer
import floppacoding.mithras.config.jsonutils.SettingDeserializer
import floppacoding.mithras.config.jsonutils.SettingSerializer
import floppacoding.mithras.module.ConfigModule
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.impl.BooleanSetting
import net.minecraft.client.util.InputUtil.Key
import java.io.File
import java.io.IOException

/**
 * ## A class to handle the module config file for the mod.
 *
 * Provides methods to save and load the settings for all [modules][ModuleManager.modules] in [ModuleManager] to / from the file.
 *
 * @author Aton
 */
class ModuleConfig(path: File) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingDeserializer())
        .registerTypeAdapter(object : TypeToken<Key>(){}.type, KeySerializer())
        .registerTypeAdapter(object : TypeToken<Key>(){}.type, KeyDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting().create()


    private val configFile = File(path, "mithrasConfig.json")

    init {
        try {
            // This gets run before the pre initialization event (it gets run when the Mithras object
            // is created)
            // therefore the directory did not get created by the preInit handler.
            // It is created here
            if (!path.exists()) {
                path.mkdirs()
            }
            // create file if it doesn't exist
            configFile.createNewFile()
        } catch (e: Exception) {
            println("Error initializing $MOD_NAME module config")
        }
    }

    /**
     * Loads the settings from the config file and updates the Modules.
     *
     * Modules must be loaded.
     */
    fun loadConfig() {
        try {
            val configModules: ArrayList<ConfigModule>
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") {
                    return
                }
                configModules= gson.fromJson(
                    this,
                    object : TypeToken<ArrayList<ConfigModule>>() {}.type
                )
            }
            configModules.forEach { configModule ->
                ModuleManager.getModuleByName(configModule.name).run updateModule@{
                    // If the module was not found check whether it can be a keybind
                    val module = this ?: if (configModule.settings.find { (it is BooleanSetting) && it.name == "THIS_IS_A_KEY_BIND" } != null) {
                        ModuleManager.addNewKeybind()
                    }else {
                        return@updateModule
                    }
                    if (module.enabled != configModule.enabled) module.toggle()
                    module.keyBind = configModule.keyBind
                    for (configSetting in configModule.settings) {
                        // It seems like when the config parsing failed it can result in this being null. The compiler does not know this.
                        // This check ensures that the rest of the config will still get processed in that case, avoiding the loss of data.
                        // So just suppress the warning here.
                        @Suppress("SENSELESS_COMPARISON")
                        if (configSetting == null) continue
                        val setting = module.getSettingByName(configSetting.name) ?: continue
                        setting.updateFromConfigSetting(configSetting)
                    }
                }
            }

        } catch (e: JsonSyntaxException) {
            println("Error parsing $MOD_NAME config.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading $MOD_NAME config.")
        } catch (e: Exception) {
            println("$MOD_NAME Config Error.")
            println(e.message)
            e.printStackTrace()
        }
    }

    fun saveConfig() {
        try {
            configFile.bufferedWriter().use {
                it.write(gson.toJson(ModuleManager.modules))
            }
        } catch (e: IOException) {
            println("Error saving $MOD_NAME config.")
        }
    }
}
package floppacoding.mithras.commands.impl

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import floppacoding.mithras.Mithras
import floppacoding.mithras.commands.ArgBuilder
import floppacoding.mithras.commands.Command
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.impl.*
import floppacoding.mithras.ui.other.FractalScreen
import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.Extensions
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.awt.Color

object MainCommand : Command() {
    override val builder: LiteralArgumentBuilder<FabricClientCommandSource> =
        command("mithras") {
            execute {
                Extensions.setScreen(Mithras.clickGUI)
            }
            literal("reload") {
                execute {
                    ChatUtils.modMessage("reloading config")
                    Mithras.moduleConfig.loadConfig()
                }
            }
            literal("dev") {
                execute {
                    devMode = !devMode
                    ChatUtils.modMessage("${if (devMode) "enabled" else "disabled"} developer mode.")
                }
            }
            literal("fractal") {
                execute {
                    Extensions.setScreen(FractalScreen)
                }
            }
            literal("toggle") {
                stringSelection("module", ::listModules) {
                    execute {
                        val module = getModule("module",it) ?: return@execute
                        module.toggle()
                        ChatUtils.modMessage("${module.name} ${if (module.enabled) "§aenabled" else "§cdisabled"}.")
                    }
                }
            }
            literal("configure") {
                stringSelection("moduleName", ::listModules) {
                    moduleSetting()
                }
            }
        }

    var devMode = false
        private set

    private fun ArgBuilder.moduleSetting() {
        literal("toggle") {
            execute {
                val module = getModule("moduleName",it) ?: return@execute
                module.toggle()
                ChatUtils.modMessage("${module.name} ${if (module.enabled) "§aenabled" else "§cdisabled"}.")
            }
        }
        stringSelection("setting", {context: CommandContext<*> ->  listModuleSettings("moduleName", context) }) {
            string("newValue") {
                execute { context ->
                    val module = getModule("moduleName",context) ?: return@execute
                    val setting = getSetting("setting", module, context) ?: return@execute
                    val newValue = context.getString("newValue")
                    when (setting) {
                        is NumberSetting<*> -> {
                            newValue.toDoubleOrNull()?.let { setting.doubleValue =  it }
                        }
                        is BooleanSetting -> {
                            if (newValue == "toggle") {
                                setting.value = !setting.value
                            }else {
                                newValue.toBooleanStrictOrNull()?.let { setting.value = it }
                            }
                        }
                        is ColorSetting -> {
                            when(newValue) {
                                "red" -> setting.value = Color.red
                                "green" -> setting.value = Color.green
                                "blue" -> setting.value = Color.blue
                                "cyan" -> setting.value = Color.cyan
                                "magenta" -> setting.value = Color.magenta
                                "yellow" -> setting.value = Color.yellow
                                "black" -> setting.value = Color.black
                                "white" -> setting.value = Color.white
                                "pink" -> setting.value = Color.pink
                                "gray" -> setting.value = Color.gray
                                else -> newValue.toIntOrNull()?.let { setting.value =  Color(it) }
                            }
                        }
                        is StringSetting -> {
                            setting.value = newValue
                        }
                        is SelectorSetting<*> -> {
                            val index = newValue.toIntOrNull()
                            if (index != null) {
                                setting.index = index
                            }else {
                                setting.selected = newValue
                            }
                        }
                        else -> ChatUtils.modMessage("Setting type not supported.")
                    }

                    ChatUtils.modMessage("Setting ${setting.name} of ${module.name} set to ${setting.value}.")
                }
            }
        }
    }

    private fun getModule(name: String, context: CommandContext<*>, message: Boolean = true): Module? {
        val moduleName = context.getString(name)
        val module = ModuleManager.getModuleByName(moduleName)
        if (module == null && message) {
            ChatUtils.modMessage("Module $moduleName not found.")
        }
        return module
    }

    private fun getSetting(name: String, module: Module, context: CommandContext<*>, message: Boolean = true): Setting<*>? {
        val settingName = context.getString(name)
        val setting = module.getSettingByName(settingName)
        if (setting == null && message) {
            ChatUtils.modMessage("Setting $settingName not found in ${module.name}.")
        }
        return setting
    }

    private fun listModules(context: CommandContext<*>): List<String> {
        return ModuleManager.modules.map {
            return@map if(it.name.contains(" ")) "\"${it.name}\""
            else it.name
        }
    }
    private fun listModuleSettings(name: String, context: CommandContext<*>): List<String>? {
        return getModule(name, context, false)?.settings?.map {
            return@map if(it.name.contains(" ")) "\"${it.name}\""
            else it.name
        }
    }
}
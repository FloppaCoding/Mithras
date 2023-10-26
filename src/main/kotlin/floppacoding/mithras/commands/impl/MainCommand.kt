package floppacoding.mithras.commands.impl

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import floppacoding.mithras.Mithras
import floppacoding.mithras.commands.Command
import floppacoding.mithras.ui.other.FractalScreen
import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.Extensions
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object MainCommand : Command {
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
        }

    var devMode = false
        private set
}
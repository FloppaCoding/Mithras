package floppacoding.mithras.commands.impl

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import floppacoding.mithras.Mithras
import floppacoding.mithras.commands.CommandBase
import floppacoding.mithras.utils.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object MainCommand : CommandBase() {
    override fun buildCommand(): LiteralArgumentBuilder<FabricClientCommandSource?> {
        return "mithras".literal(
            "reload".literal {
                ChatUtils.modMessage("reloading config")
                Mithras.moduleConfig.loadConfig()
            }
        ){
            ChatUtils.modMessage("opening menu")
            Mithras.mc.send { Mithras.mc.setScreen(Mithras.clickGUI) }
        }
    }
}
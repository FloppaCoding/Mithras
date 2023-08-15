package floppacoding.mithras.commands.impl

import floppacoding.mithras.Mithras
import floppacoding.mithras.commands.CommandBase
import floppacoding.mithras.commands.impl.MainCommand.execute
import floppacoding.mithras.commands.impl.MainCommand.literal
import floppacoding.mithras.utils.ChatUtils

object MainCommand : CommandBase.Command(
    name = "mithras",
    tasks = {
        execute {
            ChatUtils.modMessage("opening menu")
            Mithras.mc.send { Mithras.mc.setScreen(Mithras.clickGUI) }
        }
        literal("reload") {
            execute {
                ChatUtils.modMessage("reloading config")
                Mithras.moduleConfig.loadConfig()
            }
        }
    }
)
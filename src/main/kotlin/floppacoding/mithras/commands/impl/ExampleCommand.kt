package floppacoding.mithras.commands.impl

import floppacoding.mithras.commands.FloppaCommand
import floppacoding.mithras.commands.Subcommand
import floppacoding.mithras.utils.ChatUtils.modMessage

// remove this this is just temporary for test
object ExampleCommand : FloppaCommand {
    override val command: Subcommand =
        command("literaltwo") {
            execute("x", "y", "z") { x: Int, y: Int, z: Int ->
                modMessage("x: $x, y: $y, z: $z")
            }
            command("literaltwo") {
                execute {
                    modMessage("literal 1")
                }
                command("literaltwo") {
                    execute {
                        modMessage("literal 2")
                    }
                }
            }
        }
}

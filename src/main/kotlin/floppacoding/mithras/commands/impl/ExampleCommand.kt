package floppacoding.mithras.commands.impl

import floppacoding.mithras.commands.FloppaCommand
import floppacoding.mithras.commands.Subcommand
import floppacoding.mithras.utils.ChatUtils.modMessage

// remove this this is just temporary for test
object ExampleCommand : FloppaCommand {
    override val command: Subcommand =
        "mtest" {
            execute("x, y, z") { x: Float, y: Float, z: Float ->
                modMessage("x: $x, y: $y, z: $z")
            }
            "literal" {
                execute {
                    modMessage("literal 1")
                }
                "literaltwo" {
                    execute {
                        modMessage("literal 2")
                    }
                }
            }
        }
}

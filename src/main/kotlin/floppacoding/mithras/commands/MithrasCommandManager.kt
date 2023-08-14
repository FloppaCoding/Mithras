package floppacoding.mithras.commands

import com.mojang.brigadier.CommandDispatcher
import floppacoding.mithras.commands.MithrasCommandManager.commands
import floppacoding.mithras.commands.impl.MainCommand
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.command.CommandRegistryAccess

/**
 * # This object handles all the commands of the mod.
 *
 * After making a [Command][CommandBase] it just has to be added to the [commands] list, and
 * it will automatically be registered.
 *
 *
 * @author Aton
 * @see CommandBase
 */
object MithrasCommandManager {
    private val commands : ArrayList<CommandBase> = arrayListOf(
        MainCommand
    )

    /**
     * Registers all commands in the mod which are contained in [commands].
     *
     * This has to be run in [Mithras.onInitialize][floppacoding.mithras.Mithras.onInitialize]!
     * Registering commands later can fail.
     */
    fun registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, _: CommandRegistryAccess? ->
            commands.forEach {
                dispatcher.register(it.buildCommand())
            }
        })
    }
}
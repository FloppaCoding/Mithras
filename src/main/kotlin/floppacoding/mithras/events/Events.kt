package floppacoding.mithras.events

import floppacoding.mithras.utils.SkyblockArea
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.util.InputUtil.Key
import net.minecraft.client.world.ClientWorld
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

//
// A collection of all basic events for this mod.
// Author: Aton

/* GAME EVENTS */

/**
 * Posted when the game starts, after most initialization steps are done.
 * @see floppacoding.mithras.mixin.MinecraftClientMixin.onGameStart
 */
class GameStartEvent

/**
 * Fired at the start and end of a client tick.
 * @see floppacoding.mithras.events.FabricEventMapper.registerEvents
 */
class ClientTickEvent(val phase: Phase) {
    enum class Phase {
        START, END
    }
}

class ConnectionEvent {
    /**
     * Fired when disconnection from a server.
     * @see FabricEventMapper.registerEvents
     */
    class Disconnect

    /**
     * Fired when joining a serer.
     */
    class Join
}


/**
 * Fired when a new world is loaded.
 * @see [AreaChangeEvent]
 * @see floppacoding.mithras.mixin.MinecraftClientMixin
 */
class WorldChangeEvent(val newWorld: ClientWorld?)

/**
 * Fired whenever the Skyblock area changes.
 * May be preferable to [WorldChangeEvent] in some cases.
 * @see floppacoding.mithras.utils.LocationManager.currentArea
 */
class AreaChangeEvent(val oldArea: SkyblockArea?, val newArea: SkyblockArea?)


/**
 * Fired when a sound is about to play.
 *
 * @see floppacoding.mithras.mixin.SoundSystemMixin.onPlaySound
 */
class PlaySoundEvent(val sound: SoundInstance) : Cancellable()


/**
 * Fired when a message is received. [type] signals whether it was a player message, game message or action bar message.
 * When the event is cancelled the message will not be displayed.
 *
 * To modify the message there are two options:
 * - Cast [text] to [MutableText][net.minecraft.text.MutableText], which allows you to modify it.
 * With this option other event listeners which come after your listener will only see the modified message, which may break things.
 * However, this does not replace the message, and therefore it does not interfere with other listeners modifying said message.
 * The cast should always work unless another mod replaced that with a different implementation.
 * - Use [replaceWith]. This way the original message will be replaced after all listeners have been invoked.
 *
 *
 * @see floppacoding.mithras.events.FabricEventMapper.registerEvents
 */
class ChatReceivedEvent(
    /**
     * The text element of the message.
     * To get the message as a string use [text.getString()][Text.getString].
     * This may contain formatting characters. To remove those use
     * [Formatting.strip][net.minecraft.util.Formatting.strip].
     *
     * Example:
     *
     *      val message = Formatting.strip(event.text.string) ?: return
     */
    val text: Text,
    val type: Type
) : Cancellable() {
    /**
     * Use this to modify the message.
     *
     * If this value is not null the message will be replaced with this.
     */
    var replaceWith: Text? = null

    enum class Type {
        /**
         * Game message.
         */
        GAME_MESSAGE,

        /**
         * Game message to be displayed in the action bar.
         */
        ACTION_BAR,

        /**
         * Message sent by a player.
         */
        PLAYER_MESSAGE
    }
}


/* PLAYER ACTION EVENTS */

/**
 * Posted when a key or mouse button is pressed, before the inputs are evaluated.
 * Only posted when not in a GUI.
 * @param action Signals whether the key was pressed or released. 1 = pressed, 0 = released.
 * @see floppacoding.mithras.mixin.MouseMixin
 * @see floppacoding.mithras.mixin.KeyboardMixin.onKeyPress
 */
class InputEvent(val key: Key, val action: Int) : Cancellable() {
    companion object {
        const val RELEASED = 0
        const val PRESSED = 1
    }
}

/**
 * Posted when the mouse is scrolled, before the input is evaluated by the vanilla methods.
 * Only posted when not in a GUI.
 * @see GuiMouseScrollEvent
 * @see floppacoding.mithras.mixin.MouseMixin
 */
class MouseScrollEvent(val amount: Double) : Cancellable()

/**
 * Posted when the mouse is scrolled while in a GUI.
 * This even is posted before the vanilla evaluation of the action and can be cancelled.
 *
 * @see MouseScrollEvent
 */
class GuiMouseScrollEvent(val screen: Screen, val mouseX: Double, val mouseY: Double, val horizontalAmount: Double, val verticalAmount: Double) : Cancellable()


/**
 * Posted when a slot is clicked in a [HandledScreen][net.minecraft.client.gui.screen.ingame.HandledScreen].
 * Should work for basically all inventory types.
 * @see floppacoding.mithras.mixin.gui.HandledScreenMixin
 */
class GuiSlotClickEvent<T : ScreenHandler>(val slot: Slot?, val slotId: Int, val button: Int, val actionType: SlotActionType, val handler: T, val handledScreen: HandledScreen<T>, val inventoryName: Text) : Cancellable()

/**
 * Posted whenever the player tries to drop an item from the hotbar by pressing the drop key.
 * @see floppacoding.mithras.mixin.ClientPlayerEntityMixin
 */
class HotbarDropEvent(val stack: ItemStack): Cancellable()

/**
 * Posted whenever a new screen is opened through [mc.setScreen][net.minecraft.client.MinecraftClient.setScreen].
 *
 * The event is only posted when the screen is set to null.
 *
 */
class GuiOpenEvent(val screen: Screen)

/* RENDER EVENTS */

/**
 * Posted when the in game hud is being rendered.
 * Posted right after the hotbar is rendered but before everything else is rendered.
 * Does not get posted when the hud is hidden but does get posted in spectator
 * @see floppacoding.mithras.mixin.gui.InGameHudMixin.onRenderHUD
 */
class HudRenderEvent(val context: DrawContext, val partialTicks: Float)


/**
 * Posted on [WorldRenderEvents.BEFORE_DEBUG_RENDER][net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.BEFORE_DEBUG_RENDER]
 * after blocks and entities are drawn, but before the games debug rendering, player hand and hud are drawn.
 *
 * Use this for rendering things like wire frames or lines in the world.
 * @see net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.BEFORE_DEBUG_RENDER
 */
class RenderWorldOverlayEvent(val context: WorldRenderContext)

/**
 * Posted whenever a slot in a [HandledScreen] is rendered, before it is drawn.
 *
 * The event can be cancelled to prevent the slot from being rendered.
 * @see floppacoding.mithras.mixin.gui.HandledScreenMixin.onDrawSlot
 */
class DrawSlotEvent<T : ScreenHandler>(val context: DrawContext, val slot: Slot, val handledScreen: HandledScreen<T>) : Cancellable()

/**
 * Posted after the gui background is drawn but before the main content of the gui is drawn.
 * @see floppacoding.mithras.mixin.gui.ScreenMixin.onRenderBackground
 */
class GuiBackgroundDrawnEvent(val screen: Screen, val context: DrawContext)

class DrawItemTooltopEvent(val screen: Screen, val stack: ItemStack) : Cancellable()

/* NETWORK */

/**
 * This even gets posted whenever a packet is received by the client, before it is handled.
 *
 * **DO NOT MODIFY THE PACKET!** Packets tend to have final fields and cannot be modified, but if they can take care!
 * Some packets like [PlayerPositionLookS2CPacket] will trigger a confirmation being sent to the server.
 * Modifying the packet and thereby the confirmation violates the Hypixel sever rules and will flag the anticheat.
 * @see floppacoding.mithras.mixin.network.ClientConnectionMixin
 */
class PacketReceivedEvent(val packet: Packet<*>)

/**
 * Posted when the client received a [PlayerPositionLookS2CPacket] which teleports the player.
 * The event is posted before the packet is evaluated.
 *
 * **DO NOT MODIFY THE PACKET!** Luckily you cant. However, if the handling of this packet were to be modified that could easily
 * end up violation Hypixel server rules and flagging the anticheat, because a confirmation will be sent to the server.
 * @see floppacoding.mithras.mixin.network.ClientPlayNetworkHandlerMixin
 */
class TeleportEvent(val packet: PlayerPositionLookS2CPacket)

/**
 * Posted whenever a block is changed.
 * @see floppacoding.mithras.mixin.WorldChunkMixin.onSetBlock
 */
class BlockStateChangeEvent(val pos: BlockPos, val oldState: BlockState, val newState: BlockState)
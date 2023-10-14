package floppacoding.mithras.events

import com.mojang.authlib.GameProfile
import floppacoding.mithras.Mithras
import floppacoding.mithras.utils.ChatUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.message.MessageType
import net.minecraft.network.message.SignedMessage
import net.minecraft.text.Text
import java.time.Instant

object FabricEventMapper {

    fun registerEvents() {
        // Tick Events
        ClientTickEvents.START_CLIENT_TICK.register {
            Mithras.EVENT_BUS.post(ClientTickEvent(ClientTickEvent.Phase.START))
        }
        ClientTickEvents.END_CLIENT_TICK.register {
            Mithras.EVENT_BUS.post(ClientTickEvent(ClientTickEvent.Phase.END))
        }

        // Chat Events
        ClientReceiveMessageEvents.ALLOW_CHAT.register { text: Text, _: SignedMessage?, _: GameProfile?, _: MessageType.Parameters, _: Instant ->
            // Cancelled state has to be negated because Fabric will intercept the message when false is returned.
            // So to work properly this has to return false when cancelled and tru when not cancelled.
            val event = Mithras.EVENT_BUS.post(ChatReceivedEvent(text, ChatReceivedEvent.Type.PLAYER_MESSAGE))
            if (event.replaceWith != null) {
                ChatUtils.chatMessage(event.replaceWith!!)
                return@register false
            }
            return@register !event.isCancelled
        }
        ClientReceiveMessageEvents.ALLOW_GAME.register { text: Text, overlay: Boolean ->
            // Cancelled state has to be negated because Fabric will intercept the message when false is returned.
            // So to work properly this has to return false when cancelled and tru when not cancelled.
            val event = Mithras.EVENT_BUS.post(ChatReceivedEvent(text, if (overlay) ChatReceivedEvent.Type.ACTION_BAR else ChatReceivedEvent.Type.GAME_MESSAGE))
            if (event.replaceWith != null) {
                messageReplacements.add(MessageReplacement(event.text, event.replaceWith!!, System.currentTimeMillis() + 200))
            }
            return@register !event.isCancelled
        }
        ClientReceiveMessageEvents.MODIFY_GAME.register{ text: Text, overlay: Boolean ->
            messageReplacements.removeIf { System.currentTimeMillis() > it.timeout }
            messageReplacements.find { it.originalMessage === text }?.let {
                return@register it.replacement
            }
            return@register text
        }

        // Connection
        ClientPlayConnectionEvents.DISCONNECT.register { _: ClientPlayNetworkHandler, _: MinecraftClient ->
            Mithras.EVENT_BUS.post(ConnectionEvent.Disconnect())
        }
        ClientPlayConnectionEvents.JOIN.register { _: ClientPlayNetworkHandler, _: PacketSender, _: MinecraftClient ->
            Mithras.EVENT_BUS.post(ConnectionEvent.Join())
        }

        // Rendering
        WorldRenderEvents.LAST.register {context: WorldRenderContext ->
            Mithras.EVENT_BUS.post(RenderWorldOverlayEvent(context))
        }

        // Input
        ScreenEvents.BEFORE_INIT.register{ _, screen, _, _ ->
            ScreenMouseEvents.allowMouseScroll(screen).register { screen2: Screen, mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double  ->
                !Mithras.EVENT_BUS.post(GuiMouseScrollEvent(screen2, mouseX, mouseY, horizontalAmount, verticalAmount)).isCancelled
            }
        }
    }

    private val messageReplacements: MutableList<MessageReplacement> = mutableListOf()

    private data class MessageReplacement(val originalMessage: Text, val replacement: Text, val timeout: Long)
}
package floppacoding.mithras.events

import com.mojang.authlib.GameProfile
import floppacoding.mithras.Mithras
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
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
            !Mithras.EVENT_BUS.post(ChatReceivedEvent(text, ChatReceivedEvent.Type.PLAYER_MESSAGE)).isCancelled
        }
        ClientReceiveMessageEvents.ALLOW_GAME.register { text: Text, overlay: Boolean ->
            // Cancelled state has to be negated because Fabric will intercept the message when false is returned.
            // So to work properly this has to return false when cancelled and tru when not cancelled.
            !Mithras.EVENT_BUS.post(ChatReceivedEvent(text, if (overlay) ChatReceivedEvent.Type.ACTION_BAR else ChatReceivedEvent.Type.GAME_MESSAGE)).isCancelled
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
    }
}
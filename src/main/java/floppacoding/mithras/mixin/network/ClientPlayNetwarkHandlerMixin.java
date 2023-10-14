package floppacoding.mithras.mixin.network;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.TeleportEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The targeted class handles all packets received by the client.
 *
 * @author Aton
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetwarkHandlerMixin {
    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void mithras$onTeleport(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        Mithras.EVENT_BUS.post(new TeleportEvent(packet));
    }
}

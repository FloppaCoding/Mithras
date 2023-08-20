package floppacoding.mithras.mixin;

import floppacoding.mithras.module.impl.player.AutoSprint;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    /**
     * Enables auto sprint.
     */
    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean onCheckSprintState(KeyBinding instance) {
        if (AutoSprint.shouldForceSprint()) return true;
        return instance.isPressed();
    }
}

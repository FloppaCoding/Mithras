package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.HotbarDropEvent;
import floppacoding.mithras.module.impl.player.AutoSprint;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin for the {@link ClientPlayerEntity} class.
 *
 * @author Aton
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    /**
     * Enables auto sprint.
     */
    @Redirect(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/PlayerInput;sprint()Z"))
    private boolean mithras$onCheckSprintState(PlayerInput instance) {
        if (AutoSprint.shouldForceSprint()) return true;
        return instance.sprint();
    }

    /**
     * Posts a {@link HotbarDropEvent} when the player tries to drop an item from the hotbar.
     */
    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    private void mithras$preventItemDrops(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
        assert Mithras.mc.player != null;
        // mc.player is used here instead of this. That avoids having to mixin into superclasses to shadow getInventory().
        ItemStack stack = Mithras.mc.player.getInventory().getSelectedStack();
        if (Mithras.EVENT_BUS.post(new HotbarDropEvent(stack)).isCancelled())
            cir.setReturnValue(false);
    }
}

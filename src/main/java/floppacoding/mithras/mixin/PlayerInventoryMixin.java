package floppacoding.mithras.mixin;

import floppacoding.mithras.module.impl.player.DisableHotbarScroll;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void onHotbarScroll(double scrollAmount, CallbackInfo ci) {
        if (DisableHotbarScroll.INSTANCE.shouldDisableHotbarScroll())
            ci.cancel();
    }
}

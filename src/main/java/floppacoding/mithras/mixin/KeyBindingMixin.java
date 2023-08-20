package floppacoding.mithras.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
    /**
     * Notify the {@link KeyStateTracker} that all keys should be unpressed.
     * This happens when a GUI is opened.
     */
    @Inject(method = "unpressAll", at = @At("HEAD"))
    private static void onUnpressAll(CallbackInfo ci) {
        KeyStateTracker.unpressAllKeys();
    }
}

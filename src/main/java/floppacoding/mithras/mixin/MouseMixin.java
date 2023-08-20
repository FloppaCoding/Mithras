package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.InputEvent;
import floppacoding.mithras.events.MouseScrollEvent;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dispatches mouse events.
 * @author Aton
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {
    /**
     * Post a {@link InputEvent} when a mouse button is clicked.
     */
    @Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V", shift = At.Shift.BEFORE), cancellable = true)
    public void onMouseClick(long window, int button, int action, int mods, CallbackInfo ci) {
        // Action seems to determine whether the key was pressed or release and maybe more?! 1 should indicate a key press.
        if(Mithras.EVENT_BUS.post(new InputEvent(InputUtil.Type.MOUSE.createFromCode(button), action)).isCancelled())
            ci.cancel();
    }

    /**
     * Posts a {@link MouseScrollEvent} when the mouse is scrolled.
     */
    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"), cancellable = true)
    private void onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Mithras.EVENT_BUS.post(new MouseScrollEvent(vertical)).isCancelled())
            ci.cancel();
    }
}

package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.InputEvent;
import floppacoding.mithras.utils.KeyStateTracker;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dispatches keyboard events.
 * @author Aton
 */
@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    /**
     * Post a {@link InputEvent} when a mouse button is clicked.
     */
    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;", shift = At.Shift.AFTER), cancellable = true)
    public void onKeyPress(long window, int keyCode, int scancode, int action, int modifiers, CallbackInfo ci) {
        // action seems to determine press / release with 0 being release and 1 press.
        InputUtil.Key key = InputUtil.fromKeyCode(keyCode, scancode);
        Integer previousState = KeyStateTracker.keyStates.get(key);
        if (previousState == null || action != previousState) {
            KeyStateTracker.keyStates.put(key, action);
            if (Mithras.EVENT_BUS.post(new InputEvent(key, action)).isCancelled())
                ci.cancel();
        }
    }
}

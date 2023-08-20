package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.InputEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

/**
 * Dispatches keyboard events.
 * @author Aton
 */
@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    /**
     * Used to keep track of key states. Only when a state is changed an {@link InputEvent} will be posted.
     */
    @Unique
    private static final HashMap<InputUtil.Key, Integer> keyStates = new HashMap<>();

    /**
     * Post a {@link InputEvent} when a mouse button is clicked.
     */
    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;", shift = At.Shift.AFTER), cancellable = true)
    public void onKeyPress(long window, int keyCode, int scancode, int action, int modifiers, CallbackInfo ci) {
        // action seems to determine press / release with 0 being release and 1 press.
        InputUtil.Key key = InputUtil.fromKeyCode(keyCode, scancode);
        Integer previousState = keyStates.get(key);
        if (previousState == null || action != previousState) {
            keyStates.put(key, action);
            if (Mithras.EVENT_BUS.post(new InputEvent(key, action)).isCancelled())
                ci.cancel();
        }
    }
}

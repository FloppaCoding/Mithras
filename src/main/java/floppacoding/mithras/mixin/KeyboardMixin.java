package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.InputEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

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

/**
 * This class has to exist because Mixin classes don't allow public static members.
 * But such a member is required to effectively unpress all buttons on GUI open.
 * @author Aton
 */
abstract class KeyStateTracker {
    /**
     * Used to keep track of key states. Only when a state is changed an {@link InputEvent} will be posted.
     */
    public static final HashMap<InputUtil.Key, Integer> keyStates = new HashMap<>();

    /**
     * Sets the previous state of all keys to unpressed.
     * This can be used when a gui is opened so that the game will not falsely assume that some keys are still pressed.
     */
    public static void unpressAllKeys() {
        for (Map.Entry<InputUtil.Key, Integer> keyIntegerEntry : keyStates.entrySet()) {
            keyIntegerEntry.setValue(0);
        }
    }
}

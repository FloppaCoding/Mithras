package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.PreKeyInputEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    /**
     * Post a {@link PreKeyInputEvent} when a mouse button is clicked.
     */
    @Inject(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;fromKeyCode(II)Lnet/minecraft/client/util/InputUtil$Key;", shift = At.Shift.AFTER))
    public void onKeyPress(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        // action seems to determine press / release with 0 being release and 1 press.
        if (action != 0) {
            Mithras.EVENT_BUS.post(new PreKeyInputEvent(InputUtil.fromKeyCode(key, scancode)));
        }
    }
}

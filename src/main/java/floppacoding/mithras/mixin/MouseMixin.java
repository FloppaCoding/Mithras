package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.InputEvent;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    /**
     * Post a {@link InputEvent} when a mouse button is clicked.
     */
    @Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V", shift = At.Shift.BEFORE))
    public void onMouseClick(long window, int button, int action, int mods, CallbackInfo ci) {
        // Action seems to determine whether the key was pressed or release and maybe more?! 1 should indicate a key press.
        if (action == 1) {
            Mithras.EVENT_BUS.post(new InputEvent(InputUtil.Type.MOUSE.createFromCode(button)));
        }
    }
}

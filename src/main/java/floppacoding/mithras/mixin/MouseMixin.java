package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.InputEvent;
import floppacoding.mithras.events.MouseScrollEvent;
import floppacoding.mithras.module.impl.misc.KeepMousePosition;
import net.minecraft.client.Mouse;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dispatches mouse events.
 * @author Aton
 */
@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow private double x;
    @Shadow private double y;
    @Unique private double lastX;
    @Unique private double lastY;
    @Unique private long setTime = Long.MAX_VALUE;

    /**
     * Post a {@link InputEvent} when a mouse button is clicked.
     */
    @Inject(method = "onMouseButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"), cancellable = true)
    public void mithras$onMouseClick(long window, int button, int action, int mods, CallbackInfo ci) {
        // Action seems to determine whether the key was pressed or release and maybe more?! 1 should indicate a key press.
        if(Mithras.EVENT_BUS.post(new InputEvent(InputUtil.Type.MOUSE.createFromCode(button), action)).isCancelled())
            ci.cancel();
    }

    /**
     * Posts a {@link MouseScrollEvent} when the mouse is scrolled.
     */
    @Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSpectator()Z"), cancellable = true)
    private void mithras$onScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (Mithras.EVENT_BUS.post(new MouseScrollEvent(vertical)).isCancelled())
            ci.cancel();
    }

    @Inject(method = "lockCursor", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Mouse;cursorLocked:Z"),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;IS_SYSTEM_MAC:Z")
        )
    )
    private void mithras$storeMousePosition(CallbackInfo ci) {
        lastX = this.x;
        lastY = this.y;
        setTime = System.currentTimeMillis();
    }

    /**
     * Using a redirect for this is a bit awkward.
     * But injecting the change to x and y right before the invocation of {@link InputUtil#setCursorParameters} is very
     * finicky.
     * The bytecode will look differnt than expected.
     * This mixin is probably the safest way to deal with that.
     */
    @Redirect(method = "unlockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/InputUtil;setCursorParameters(JIDD)V"))
    private void mithras$modifyMousePosition(long handler, int inputModeValue, double x, double y) {
        if (System.currentTimeMillis() < setTime + 500 && KeepMousePosition.INSTANCE.shouldKeepMousePosition()) {
            this.x = lastX;
            this.y = lastY;
            // This vanilla method is not implemented correctly. It will only set the cursor position when the previous
            // input mode was already NORMAL! So in this situation, when changing from grabbed cursor to normal cursor
            // it will not set the cursor position. So it has to be set manually again.
            InputUtil.setCursorParameters(handler, inputModeValue, lastX, lastY);
            GLFW.glfwSetCursorPos(handler, lastX, lastY);
        }else {
            InputUtil.setCursorParameters(handler, inputModeValue, x, y);
        }
    }
}

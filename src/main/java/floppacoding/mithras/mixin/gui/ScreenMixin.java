package floppacoding.mithras.mixin.gui;

import floppacoding.aurora.mc_modern.Renderer2DMC;
import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.GuiBackgroundDrawnEvent;
import floppacoding.mithras.ui.GuiScreen;
import floppacoding.mithras.ui.core.elements.GuiElement;
import floppacoding.mithras.utils.ScreenMixinDuck;
import kotlin.jvm.functions.Function0;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import static floppacoding.mithras.Mithras.mc;

/**
 * Integrates the handling of custom GUI elements into vanilla GUIs.
 *
 * @author Aton
 */
@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement implements ScreenMixinDuck {
    @Shadow @Final private List<Drawable> drawables;

    @Shadow public abstract void render(DrawContext context, int mouseX, int mouseY, float deltaTicks);

    @Unique private final ArrayList<GuiElement> elements = new ArrayList<>();
    @Unique private float elementScale = (float) mc.getWindow().getScaleFactor();
    @Unique private boolean isVanillaGui = true;
    @Unique private Renderer2DMC renderer() {return  Mithras.getRenderer2D(); }
    @Unique private Boolean shouldBlur()  {
        try {
            return ((GuiScreen) (Object) this).getBlurBackground();
        } catch (Exception e) {
            return true;
        }
    }

    @Inject(method = "applyBlur", at = @At("HEAD"), cancellable = true)
    private void onApplyBlur(CallbackInfo ci) {
        if (!isVanillaGui && !shouldBlur()) {ci.cancel();}
    }

    @Inject(method = "renderDarkening(Lnet/minecraft/client/gui/DrawContext;)V", at = @At("HEAD"), cancellable = true)
    private void onRenderDarkening(DrawContext dc, CallbackInfo ci) {
        if (!isVanillaGui) {ci.cancel();}
    }

    @Override
    public float mithras_getElementScale() {
        return elementScale;
    }

    @Override
    public void mithras_setElementScale(float scale) {
        elementScale = scale;
    }

    @Override
    public void mithras_addElement(GuiElement element) {
        elements.add(element);
    }

    @Override
    public List<GuiElement> mithras_elements() {
        return elements;
    }

    @Override
    public void mithras_setIsVanillaGui(boolean state) {
        isVanillaGui = state;
    }

    @Override
    public List<Drawable> mithras_getDrawables() {
        return this.drawables;
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        // TODO any initialization / resizing goes here
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderElements(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {

        Renderer2DMC renderer = renderer();
        if (isVanillaGui) {
            renderer.beginFrame();
            renderer.scale(elementScale, elementScale);
        }
        renderer.push();
        for (GuiElement element : elements) {
            element.render(scaledMouseX(), scaledMouseY(), delta);
        }
        renderer.pop();
        if (isVanillaGui) {
            renderer.endFrame();
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float x = scaledMouseX();
        float y = scaledMouseY();

        boolean interactedWithElement = interactWithElements( (element) -> element.mouseClicked(x, y, button) );
        if (interactedWithElement) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY,button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        float x = scaledMouseX();
        float y = scaledMouseY();

        boolean interactedWithElement = interactWithElements( (element) -> element.mouseReleased(x, y, button) );
        if (interactedWithElement) {
            return true;
        }
        return super.mouseReleased(mouseX,mouseY,button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        float x = scaledMouseX();
        float y = scaledMouseY();

        boolean interactedWithElement = interactWithElements( (element) -> element.mouseScrolled(x, y, (float) verticalAmount) );
        if (interactedWithElement) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        boolean interactedWithElement = interactWithElements( (element) -> element.keyPressed(keyCode, scanCode, modifiers) );
        if (interactedWithElement) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        boolean interactedWithElement = interactWithElements( (element) -> element.keyReleased(keyCode, scanCode, modifiers) );
        if (interactedWithElement) {
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        boolean interactedWithElement = interactWithElements( (element) -> element.charTyped(chr, modifiers) );
        if (interactedWithElement) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    /**
     * Iterates the gui elements in reverse and supplies them to the function.
     * Meant to be used for click interactions.
     * @param action The action to perform for each element. When it returns true for an element all further actions
     *               will NOT be performed and true will be returned.
     * @return Whether an interaction was performed as determined by the action.
     */
    @Unique
    private boolean interactWithElements(Function<GuiElement, Boolean> action) {
        ListIterator<GuiElement> iterator = elements.listIterator(elements.size());

        while (iterator.hasPrevious()) {
            GuiElement element = iterator.previous();
            if (action.apply(element)) {
                return true;
            }
        }
        return false;
    }

    @Unique private float scaledMouseX() { return (float) (mc.mouse.getX() / elementScale); }
    @Unique private float scaledMouseY() { return (float) (mc.mouse.getY() / elementScale); }

    /**
     * Dispatches the BackgroundDrawEvent
     */
    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void onRenderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        Mithras.EVENT_BUS.post(new GuiBackgroundDrawnEvent((Screen) (Object) this, context));
    }
}

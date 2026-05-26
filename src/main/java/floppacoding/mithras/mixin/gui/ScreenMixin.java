package floppacoding.mithras.mixin.gui;

import floppacoding.aurora.mc_modern.Renderer2DMC;
import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.GuiBackgroundDrawnEvent;
import floppacoding.mithras.ui.GuiScreen;
import floppacoding.mithras.ui.core.elements.GuiElement;
import floppacoding.mithras.utils.ScreenMixinDuck;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

/**
 * Integrates the handling of custom GUI elements into vanilla GUIs.
 *
 * @author Aton
 */
@Mixin(Screen.class)
public abstract class ScreenMixin extends AbstractParentElement implements ScreenMixinDuck {
    @Shadow @Final private List<Drawable> drawables;

    @Shadow public abstract void render(DrawContext context, int mouseX, int mouseY, float deltaTicks);

    @Shadow @Final protected MinecraftClient client;
    @Unique private final ArrayList<GuiElement> elements = new ArrayList<>();
    @Unique private float elementScale = 1f;
    @Unique private boolean isVanillaGui = true;
    @Unique private boolean reinitDrawables = false;
    @Unique private Renderer2DMC renderer() {return  Mithras.getRenderer2D(); }
    @Unique private Boolean shouldBlur()  {
        try {
            return ((GuiScreen) (Object) this).getBlurBackground();
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void mithras_setReinitDrawables(boolean state) {
        this.reinitDrawables = state;
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

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screen/Screen;title:Lnet/minecraft/text/Text;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void onClassInit(MinecraftClient minecraftClient, TextRenderer textRenderer, Text text, CallbackInfo ci) {
        if (client.getWindow() != null) {
            this.mithras_setElementScale(client.getWindow().getScaleFactor());
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

    @Inject(method = "clearAndInit", at = @At("HEAD"))
    private void onClearAndInit(CallbackInfo ci) {
        if (reinitDrawables) {
            drawables.clear();
        }
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
            // TODO implement gui scale affecting these elements?
//            renderer.scale(elementScale, elementScale); // This should not come before the positioning of the elements
        }
        renderer.push();
        for (GuiElement element : elements) {
            element.render(scaledMouseX(), scaledMouseY(), delta);
        }
        renderer.pop();
    }

    @Override
    public void mithras_finishFrame() {
        if (isVanillaGui) {
            renderer().endFrame();
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        float x = scaledMouseX();
        float y = scaledMouseY();

        boolean interactedWithElement = interactWithElements( (element) -> element.mouseClicked(x, y, click.button()) );
        if (interactedWithElement) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(Click click) {
        float x = scaledMouseX();
        float y = scaledMouseY();

        boolean interactedWithElement = interactWithElements( (element) -> element.mouseReleased(x, y, click.button()) );
        if (interactedWithElement) {
            return true;
        }
        return super.mouseReleased(click);
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
    public void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        boolean interactedWithElement = interactWithElements( (element) -> element.keyPressed(input.getKeycode(), input.scancode(), input.modifiers()) );
        if (interactedWithElement) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean keyReleased(KeyInput input) {
        boolean interactedWithElement = interactWithElements( (element) -> element.keyReleased(input.getKeycode(), input.scancode(), input.modifiers()) );
        if (interactedWithElement) {
            return true;
        }
        return super.keyReleased(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        boolean interactedWithElement = interactWithElements( (element) -> element.charTyped(input.asString().charAt(0), input.modifiers()) );
        if (interactedWithElement) {
            return true;
        }
        return super.charTyped(input);
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

    @Unique private float scaledMouseX() { return (float) (client.mouse.getX() /* TODO fix scale / elementScale*/); }
    @Unique private float scaledMouseY() { return (float) (client.mouse.getY() /*TODO fix scale / elementScale*/); }

    /**
     * Dispatches the BackgroundDrawEvent
     */
    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void onRenderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        Mithras.EVENT_BUS.post(new GuiBackgroundDrawnEvent((Screen) (Object) this, context));
    }
}

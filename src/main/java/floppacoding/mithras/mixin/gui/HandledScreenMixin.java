package floppacoding.mithras.mixin.gui;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.commands.impl.MainCommand;
import floppacoding.mithras.events.DrawItemTooltopEvent;
import floppacoding.mithras.events.DrawSlotEvent;
import floppacoding.mithras.events.GuiSlotClickEvent;
import floppacoding.mithras.utils.ChatUtils;
import floppacoding.mithras.utils.inventory.NBTStringWriter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static floppacoding.mithras.Mithras.mc;

/**
 * Mixin to the {@link HandledScreen} class.
 * <br>
 * HandledScreen is the super class used for essentially all inventory guis in the game.
 *
 * @author Aton
 */
@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {
    @Shadow @Final protected T handler;

    @Shadow @Nullable protected Slot focusedSlot;
    /**
     * The corresponding this object of the HandledScreen class that is being mixed in.
     */
    @SuppressWarnings("unchecked")
    @Unique private final HandledScreen<T> handledScreen = (HandledScreen<T>) (Object) this;

    /**
     * Posts a {@link GuiSlotClickEvent} when a slot is click in an inventory screen.
     */
    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickSlot(IIILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), cancellable = true)
    private void mithras$onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        if (Mithras.EVENT_BUS.post(new GuiSlotClickEvent<>(slot, slotId, button, actionType, this.handler, handledScreen, handledScreen.getTitle())).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    private void onDrawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        if (Mithras.EVENT_BUS.post(new DrawSlotEvent<>(context, slot, handledScreen)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "drawMouseoverTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;Ljava/util/Optional;IILnet/minecraft/util/Identifier;)V"), cancellable = true)
    private void onItemTooltip(DrawContext context, int x, int y, CallbackInfo ci) {
        assert this.focusedSlot != null;
        ItemStack itemStack = this.focusedSlot.getStack();
        if (Mithras.EVENT_BUS.post(new DrawItemTooltopEvent(handledScreen, itemStack)).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (!MainCommand.INSTANCE.getDevMode()) return;
        if (keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL && this.focusedSlot != null) {
            ItemStack stack = this.focusedSlot.getStack();
            if (stack == null) return;
            String nbtString = NBTStringWriter.creatNbtString(stack);
            mc.keyboard.setClipboard(nbtString);
            Text stackName = stack.getName();
            ChatUtils.modMessage(ChatUtils.literalText("Copied ").append(stackName).append(" item nbt data to clipboard."));
            cir.setReturnValue(true);
        }
    }
}

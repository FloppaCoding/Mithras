package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.GuiSlotClickEvent;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
}

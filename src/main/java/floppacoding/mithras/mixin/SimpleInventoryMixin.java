package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.SimpleInventoryUpdateEvent;
import net.minecraft.inventory.SimpleInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleInventory.class)
public abstract class SimpleInventoryMixin {
    @Inject(method = "markDirty", at =@At("HEAD"))
    private void onInventoryChange(CallbackInfo ci) {
        Mithras.EVENT_BUS.post(new SimpleInventoryUpdateEvent((SimpleInventory) (Object) this));
    }
}

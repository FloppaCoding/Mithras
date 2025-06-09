package floppacoding.mithras.mixin;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientCommonNetworkHandler.class)
public interface ClientCommonNetworkHandlerAccessor {
    @Nullable
    @Accessor("brand")
    String getBrand();
}

package floppacoding.mithras.mixin.render;

import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;

@Mixin(VertexFormat.DrawMode.class)
@Unique
public abstract class DrawModeMixin {
    /**
     * This synthetic field stores the enum values.
     */
    @Shadow
    @Final
    @Mutable
    private static VertexFormat.DrawMode[] field_27386;

    @Unique
    private static final VertexFormat.DrawMode POINTS = addEnum("POINTS", 0, 1, 1, false);

    /**
     * Invoker for the constructor.
     * <br>
     * There are two extra parameters name and id which get added to the constructor of enums by the compiler.
     */
    @Invoker("<init>")
    private static VertexFormat.DrawMode invokeInit(String name, int id, int glMode, int firstVertexCount, int additionalVertexCount, boolean shareVertices) {
        throw new AssertionError();
    }

    @SuppressWarnings("SameParameterValue")
    @Unique
    private static VertexFormat.DrawMode addEnum(String name, int glMode, int firstVertexCount, int additionalVertexCount, boolean shareVertices) {
        assert DrawModeMixin.field_27386 != null;
        ArrayList<VertexFormat.DrawMode> values = new ArrayList<>(Arrays.asList(DrawModeMixin.field_27386));
        VertexFormat.DrawMode mode = invokeInit(name, values.get(values.size() - 1).ordinal() + 1, glMode, firstVertexCount, additionalVertexCount, shareVertices);
        // Take care to use the right id in case of adding more!!
        values.add(mode);
        DrawModeMixin.field_27386 = values.toArray(new VertexFormat.DrawMode[0]);
        return mode;
    }

    @SuppressWarnings({"ConstantValue", "RedundantCast"})
    @Inject(method = "getIndexCount", at = @At("HEAD"), cancellable = true)
    private void adjustIndexCount(int vertexCount, CallbackInfoReturnable<Integer> cir){
        if ( ((VertexFormat.DrawMode) (Object) this) == POINTS ) {
            cir.setReturnValue(vertexCount);
        }
    }
}

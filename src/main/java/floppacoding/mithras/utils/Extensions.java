package floppacoding.mithras.utils;

import com.mojang.serialization.Codec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;

/**
 * A collection for methods which had to be coded in java / are more convenient to code in java.
 *
 * @author Aton
 */
public abstract class Extensions {

    /**
     * The compiler will add a type cast for SimpleOption.ValidatingIntSliderCallbacks to SimpleOption.Callback,
     * which is not public. With this methods being coded in kotlin that resulted in a crash. This java version works,
     * even tho it looks like the compiler still adds that type cast.
     */
    public static SimpleOption<Integer> provideFakeFov(int min, int max) {
        return new SimpleOption<>(
                "options.fov",
                SimpleOption.emptyTooltip(),
                GameOptions::getGenericValueText,
                new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                Codec.DOUBLE.xmap((value) -> (int) (value * 40.0 + 70.0),
                        (value) -> ((double) value - 70.0) / 40.0),
                70,
                (value) -> MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate()
        );
    }
}

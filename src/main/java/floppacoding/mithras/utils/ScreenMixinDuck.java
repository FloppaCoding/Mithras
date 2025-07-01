package floppacoding.mithras.utils;

import floppacoding.mithras.ui.core.elements.GuiElement;
import net.minecraft.client.gui.Drawable;

import java.util.List;

// TODO probably convert this to kotlin. This should not depend on any mc code.
public interface ScreenMixinDuck {
    void mithras_addElement(GuiElement element);
    List<GuiElement> mithras_elements();

    float mithras_getElementScale();
    void mithras_setElementScale(float scale);

    // TODO replace with IsMidFrame and other stuff??? Probably a good idea!
    void mithras_setIsVanillaGui(boolean state);

    List<Drawable> mithras_getDrawables();

    void mithras_setReinitDrawables(boolean state);
}

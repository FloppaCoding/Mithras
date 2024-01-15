package floppacoding.mithras.ui.core

import floppacoding.mithras.ui.core.elements.GuiElement
import floppacoding.mithras.utils.ScreenMixinDuck
import net.minecraft.client.gui.screen.Screen

object GuiTools {

    fun Screen.addElements(vararg elements: GuiElement) {
        for (element in elements) {
            (this as ScreenMixinDuck).mithras_addElement(element)
        }
    }
}
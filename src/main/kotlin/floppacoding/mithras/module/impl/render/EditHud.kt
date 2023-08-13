package floppacoding.mithras.module.impl.render

import floppacoding.mithras.Mithras
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.ui.hud.EditHudGUI

/**
 * Open the edit hid gui.
 * @author Aton
 */
object EditHud : Module(
    "Edit Hud",
    category = Category.RENDER,
    description = "Opens the eidt hud gui."
){

    /**
     * Overridden to prevent the chat message from being sent.
     */
    override fun onKeyBind() {
        this.toggle()
    }

    /**
     * Automatically disable it again and open the gui
     */
    override fun onEnable() {
        Mithras.display = EditHudGUI
        toggle()
        super.onEnable()
    }
}
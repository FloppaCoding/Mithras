package floppacoding.mithras.module.impl.keybinds

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.ui.clickgui.ClickGUI

object AddKeybind : Module(
    "Add New Key Bind",
    category = Category.KEY_BIND,
    description = "Adds a new key bind you can customize.",
    toggled = true
){
    override fun onEnable() {}

    override fun onDisable() {
        val bind = ModuleManager.addNewKeybind()
        toggle()
        ClickGUI.panels.find { it.category === Category.KEY_BIND }?.let {
            it.moduleButtons.add(floppacoding.mithras.ui.clickgui.elements.ModuleButton(bind, it))
        }
    }

    override fun onKeyBind() {}
}
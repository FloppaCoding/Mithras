package floppacoding.mithras.module.impl.keybinds

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.ActionSetting
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.StringSetting
import floppacoding.mithras.utils.ChatUtils

class KeyBind(name: String) : Module(name, category = Category.KEY_BIND){

    val bindName = StringSetting("Name", this.name, description = "The name of this Key Bind that will be shown on the toggle button in the GUI.")
    private val message = StringSetting("Message","",100, description = "Message to be sent. For commands start the message with \"/\".")
    private val removeButton = ActionSetting("Remove Key Bind", visibility = Visibility.ADVANCED_ONLY, description = "Removes the Key Bind."){
        ModuleManager.removeKeyBind(this@KeyBind)
    }
    // Used by the config loader to determine whether a setting is a keybind
    private val flag = BooleanSetting("THIS_IS_A_KEY_BIND", visibility = Visibility.HIDDEN)

    init {
        this.addSettings(
            bindName,
            message,
            removeButton,
            flag
        )
    }

    override fun onKeyBind() {
        if (!this.enabled) return
        performAction()
    }

    private fun performAction(){
        ChatUtils.sendMessage(message.text)
    }
}
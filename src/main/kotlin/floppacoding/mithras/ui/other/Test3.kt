package floppacoding.mithras.ui.other

import floppacoding.aurora.core.Aurora
import floppacoding.mithras.Mithras
import floppacoding.mithras.ui.GuiScreen
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal

object Test3: GuiScreen(MutableText.of(Literal("Test Screen3"))) {
    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        Aurora.beginFrame()
        Aurora.rect(0f, 0f, 200f, 200f, -1)
        Aurora.endFrame()
        Mithras.logger.debug("yes i am doing it!!!")
        Aurora.beginFrame()
        Aurora.push()
    }
}
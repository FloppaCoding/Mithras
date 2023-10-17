package floppacoding.mithras.ui.hud

import floppacoding.mithras.utils.render.GLR
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText

object Test2 : Screen(MutableText.of(LiteralTextContent("Test Screen")))  {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {

        GLR.beginDraw(context.matrices)
        GLR.push()
        GLR.rotate(15f)
        GLR.roundedRect(50f, 50f, 100f, 200f, 20f, -1)

        GLR.pop()


        super.render(context, mouseX, mouseY, delta)
    }

}
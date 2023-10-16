package floppacoding.mithras.ui.hud

import floppacoding.mithras.utils.render.Renderer2D
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText

object Test2 : Screen(MutableText.of(LiteralTextContent("Test Screen")))  {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {

        Renderer2D.beginDraw(context.matrices)
        Renderer2D.push()
        Renderer2D.rotate(15f)
        Renderer2D.roundedRect(50f, 50f, 100f, 200f, 20f, -1)

        Renderer2D.pop()


        super.render(context, mouseX, mouseY, delta)
    }

}
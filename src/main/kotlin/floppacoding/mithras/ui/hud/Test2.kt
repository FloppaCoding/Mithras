package floppacoding.mithras.ui.hud

import floppacoding.mithras.utils.render.GLR
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import java.awt.Color

object Test2 : Screen(MutableText.of(LiteralTextContent("Test Screen")))  {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {

        val renderer = GLR


        renderer.beginFrame()
        val color = Color(0, 10 ,255 ,10).rgb
        for (ii in 0..9999) {
            renderer.rect(100f, 100f, 200f, 100f, color)
        }
        renderer.endFrame()


        super.render(context, mouseX, mouseY, delta)
    }

}
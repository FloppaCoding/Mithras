package floppacoding.mithras.ui.hud

import floppacoding.mithras.utils.render.GLR
import floppacoding.mithras.utils.render.nanovg.NVGR
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import org.joml.Vector4f
import org.joml.Vector4i
import java.awt.Color

object Test2 : Screen(MutableText.of(LiteralTextContent("Test Screen")))  {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {

        context.matrices.push()
        context.matrices.scale(0.5f, 0.5f, 1.0f)


        NVGR.beginFrame(context)
        GLR.beginFrame(context)

        GLR.roundedRect(300f, -50f, 800f, 500f, Vector4f(50f, 40f,0f, 10f), Vector4i(Color(255,0,0,100).rgb, Color(0,255,0,100).rgb, Color(0,0,255,100).rgb, Color(0,255,255,100).rgb))

        GLR.roundedRect(100f, 100f, 200f, 200f, Vector4f(50f, 40f, 10f ,1f), -1)
        GLR.border(100f, 100f, 200f, 200f, 50f, Vector4f(50f, 40f, 10f ,1f) ,Color(255,0,0,100).rgb)
//        GLR.rect(80f, 95f,40f, 10f, Color(0,0,0).rgb)

        GLR.endFrame()
        NVGR.endFrame()


        context.matrices.pop()


        super.render(context, mouseX, mouseY, delta)
    }

}
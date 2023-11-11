package floppacoding.mithras.ui.hud

import floppacoding.mithras.shaders.impl.Lines
import floppacoding.mithras.shaders.impl.RoundedRectangleSingleColor
import floppacoding.mithras.shaders.impl.TextShader
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

        RoundedRectangleSingleColor.setChroma(true)
        GLR.roundedRect(100f, 100f, 200f, 200f, Vector4f(50f, 40f, 10f ,1f), -1)
        GLR.chromaBorder(100f, 100f, 200f, 200f, 50f, 10f ,Color(255,0,0,100).rgb)
//        GLR.rect(80f, 95f,40f, 10f, Color(0,0,0).rgb)

        TextShader.setChroma(true)
        GLR.text("Chroma Floppa", 100f, 500f, -1, 150f)

        GLR.push()
        GLR.translate(1500f, 400f)
        val angle = (System.currentTimeMillis() - 1699745019401)/100f
        GLR.rotate(angle)
//        GLR.scale(4f, 4f)
        Lines.setChroma(true)
        GLR.line(-50f,0f,50f,0f, 20f,  Color(0,255,0,100).rgb)
        GLR.pop()

        GLR.endFrame()
        NVGR.endFrame()


        context.matrices.pop()


        super.render(context, mouseX, mouseY, delta)
    }

}
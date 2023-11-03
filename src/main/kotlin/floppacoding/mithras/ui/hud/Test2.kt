package floppacoding.mithras.ui.hud

import floppacoding.mithras.utils.render.GLImageManager
import floppacoding.mithras.utils.render.GLR
import floppacoding.mithras.utils.render.nanovg.NVGImageManager
import floppacoding.mithras.utils.render.nanovg.NVGR
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.util.math.RotationAxis
import java.awt.Color

object Test2 : Screen(MutableText.of(LiteralTextContent("Test Screen")))  {

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {

        context.matrices.push()

        context.matrices.translate(100f, 50f, 0f)
        context.matrices.scale(0.5f, 0.5f, 1.0f)


        context.matrices.push()
        val angle = (System.currentTimeMillis() - 1698539810826)/100f

        context.matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle))
//        context.fill(0,0,100,50,-1)
        context.matrices.pop()

        NVGR.beginFrame(context)
        GLR.beginFrame(context)

        NVGR.push()
        NVGR.translate(100f, 0f)
        NVGR.rotate(angle)
        NVGR.line(-50f,0f,50f,0f, 20f, Color(255,0,0,100).rgb)
        NVGR.pop()


//
//     GLR.translate(200f, 100f)



        GLR.push()

        GLR.rotate(angle)
//        GLR.scale(4f, 4f)
        GLR.line(-50f,0f,50f,0f, 20f,  Color(0,255,0,100).rgb)
        GLR.pop()

        GLR.image(GLImageManager.ICON, 0f, 200f, 200f, 200f,0f, 0f, 0f, 1f, 1f)
        NVGR.image(NVGImageManager.ICON, 300f, 200f, 200f, 200f)
//
//        GLR.circle(0f, 200f, 7f, Color(10, 120, 240, 140).rgb)
//        GLR.circle(0f, 500f, 40f, Color(120, 10, 240, 140).rgb)
//        GLR.ellipse(500f, 500f, Vector2f(600f, 20f), 300f, Color(200, 200, 20, 140).rgb)

        GLR.endFrame()
        NVGR.endFrame()


        context.matrices.pop()


        super.render(context, mouseX, mouseY, delta)
    }

}
package floppacoding.mithras.ui.other

import floppacoding.aurora.core.Aurora
import floppacoding.aurora.core.CapStyle
import floppacoding.aurora.core.Renderer2D
import floppacoding.aurora.mc_modern.AuroraMC
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent.Literal
import org.joml.Vector2f
import java.awt.Color

object Test2 : Screen(MutableText.of(Literal("Test Screen")))  {

    val t0 = System.currentTimeMillis()

    var shapes = 400

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {

        val  col1 = Color(0, 140, 100, 100).rgb
        val renderer: Renderer2D = AuroraMC




        AuroraMC.beginFrame()
        AuroraMC.rect(0f, 0f, 100f, 100f, -1)
        AuroraMC.endFrame()


        renderer.beginFrame()
        renderer.rect(0f, 0f, 100f, 110f, -1)


        for (ii in 0 until shapes) {

            renderer.push()
            renderer.translate(200f, 200f)
            renderer.rotate(2f * 3.14f * (System.currentTimeMillis() - t0) / 400f)
            renderer.line(-50f, -50f, 50f, 50f, 20f, col1, CapStyle.FLAT)
            renderer.pop()
            renderer.push()
            renderer.translate(300f, 200f)
            renderer.rotate(2f * 3.14f * (System.currentTimeMillis() - t0) / 400f)
            renderer.line(-50f, -50f, 50f, 50f, 20f, -1, CapStyle.FLAT)
            renderer.line(-50f, -50f, 50f, 50f, 20f, col1, CapStyle.ROUND)
            renderer.pop()

            renderer.ellipse(200f, 500f, Vector2f(200f, 70f), 50f, -1)

            renderer.text("Floppa", 100f, 600f, 0x7f_80_00_90u.toInt(), 40f)
            Aurora.getLastDrawCall()?.enableChroma()?.enableAlpha()


            renderer.border(600f, 200f, 200f, 200f, 30f, -col1)
            renderer.chromaBorder(600f, 200f, 200f, 200f, 30f, 10f, col1)
        }



        renderer.endFrame()

        super.render(context, mouseX, mouseY, delta)
    }

}
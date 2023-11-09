package floppacoding.mithras.ui.hud

import floppacoding.mithras.utils.render.GLFontManager
import floppacoding.mithras.utils.render.GLR
import floppacoding.mithras.utils.render.TextAlign
import floppacoding.mithras.utils.render.nanovg.NVGFontManager
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


//        GLR.textTest("", 0f, 0f)
//        GLR.push()
//        GLR.scale(0.5f,0.5f)
//        GLR.fontAtlas(" ", 0f, 0f)
//        GLR.pop()

        val text = """ÀÆÇÈẔ|||aasdfgASDFQq⌠⌡""" +
                """░▒▓│┤╡╢╖╕╣║╗╝╜╛┐""" +
                """╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀"""


        GLR.translate(500f, 100f)
        GLR.rect(0f, -32f, 3000f, 32f, Color(0,0,0).rgb)
        GLR.text(text, 0f, 0f, Color(0,255,0).rgb, 32f, GLFontManager.KURINTO, TextAlign.CENTER_BASELINE)

        NVGR.translate(500f, 150f)
//        NVGR.rotate(40f)
        NVGR.rect(0f, -32f, 3000f, 32f, Color(0,0,0).rgb)
        NVGR.text(text, 0f, 0f, Color(255,255,0).rgb, 32f, NVGFontManager.KURINTO,  textAlign = TextAlign.CENTER_BASELINE)


        GLR.endFrame()
        NVGR.endFrame()


        context.matrices.pop()


        super.render(context, mouseX, mouseY, delta)
    }

}
package floppacoding.mithras.ui.hud

import floppacoding.aurora.core.font.Font
import floppacoding.mithras.ui.GuiScreen
import floppacoding.mithras.utils.render.nanovg.NVGFontManager
import floppacoding.mithras.utils.render.nanovg.NVGImageManager
import floppacoding.mithras.utils.render.nanovg.NVGR
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG.*
import java.awt.Color
import java.nio.FloatBuffer

object Test : GuiScreen("Hello") {

    override fun render(mouseX: Float, mouseY: Float, delta: Float) {
        NVGR.roundedRect(100f, 500f, 200f, 200f, 8f, -1)
        NVGR.image(NVGImageManager.ICON, 100f, 500f, 200f, 200f)
        NVGR.chromaBorder(100f,500f,200f,200f, 5f, 0f)

        NVGR.push()
        NVGR.translate( 50f, 50f)
        NVGR.roundedRect(0f, 0f, 440f, 400f,8f, Color(30, 30, 0, 240).rgb)
        textField("Enter  credit card number here!",20f, 50f, 400f, Color(70, 20, 100, 200).rgb, 20f, 3f, NVGR.defaultFont)
        NVGR.pop()

        NVGR.push()
        NVGR.translate( 50f, 50f)

        drawSearchBox(NVGR.nanoContext, "The Voices", 10f, 100f, 300f, 25f, Color(255,255,255,170).rgb)
        drawButton(NVGR.nanoContext, "Do it!", 10f, 200f, 100f, 25f, Color(70,55,255).rgb, Color(255,255,255).rgb)

        NVGR.pop()



    }

    private val nanoColor: NVGColor = NVGColor.calloc()
    private val nanoColor2: NVGColor = NVGColor.calloc()
    private val nanoColor3: NVGColor = NVGColor.calloc()
    private val nanoPaint: NVGPaint = NVGPaint.calloc()

    private fun drawSearchBox(vg: Long, text: String, x: Float, y: Float, w: Float, h: Float, textColor: Int)
    {
//        char icon[8]
        val cornerRadius = h/2-1f

        // Edit
        val bg: NVGPaint = nvgBoxGradient(vg, x,y+1.5f, w,h, h/2,5f, nvgRGBA(0,0,0,48, nanoColor), nvgRGBA(0,0,0,112, nanoColor2), nanoPaint)
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x,y, w,h, cornerRadius)
        nvgFillPaint(vg, bg)
        nvgFill(vg)

//        nvgBeginPath(vg);
//        nvgRoundedRect(vg, x+0.5f,y+0.5f, w-1,h-1, cornerRadius-0.5f);
//        nvgStrokeColor(vg, nvgRGBA(0,0,0,48, nanoColor));
//        nvgStroke(vg);

//        nvgFontSize(vg, h*1.3f)
//        nvgFontFace(vg, "icons")
//        nvgFillColor(vg, nvgRGBA(255.toByte(), 255.toByte(), 255.toByte(),64, nanoColor))
//        nvgTextAlign(vg,NVG_ALIGN_CENTER or NVG_ALIGN_MIDDLE)
//        nvgText(vg, x+h*0.55f, y+h*0.55f, cpToUTF8(ICON_SEARCH,icon), NULL)

        nvgFontSize(vg, 17.0f)
        nvgFontFaceId(vg, NVGFontManager.ROBOTO.id)
        nvgFillColor(vg, NVGR.updateColor(textColor))

        nvgTextAlign(vg,NVG_ALIGN_LEFT or NVG_ALIGN_MIDDLE)
        nvgText(vg, x+h*1.05f,y+h*0.5f+1,text)

//        nvgFontSize(vg, h*1.3f)
//        nvgFontFace(vg, "icons");
//        nvgFillColor(vg, nvgRGBA(255,255,255,32));
//        nvgTextAlign(vg,NVG_ALIGN_CENTER|NVG_ALIGN_MIDDLE);
//        nvgText(vg, x+w-h*0.55f, y+h*0.55f, cpToUTF8(ICON_CIRCLED_CROSS,icon), NULL);
    }

    private fun textField(text: String, x: Float, y: Float, width: Float, color: Int, fontSize: Float, radius: Float, font: Font) {
        if (font !is NVGFontManager.NVGFont) throw Error("Invalid Font")
        val height = fontSize* 1.5f
        nvgBeginPath(NVGR.nanoContext)
        nvgRGBA(255.toByte(), 255.toByte(),255.toByte(), 32.toByte(), nanoColor)
        nvgRGBA(23.toByte(), 32.toByte(),32.toByte(), 64.toByte(), nanoColor2)
        nvgBoxGradient(
            NVGR.nanoContext, x +1.5f, y+3f, width - 3f, height-4f, radius, 4f,
            nanoColor,
            nanoColor2,
            nanoPaint
        )
        nvgRoundedRect(NVGR.nanoContext, x+ 1f, y+1f, width-2f, height-2f, radius)
        nvgFillPaint(NVGR.nanoContext, nanoPaint)
        nvgFill(NVGR.nanoContext)

        nvgBeginPath(NVGR.nanoContext)
        nvgRoundedRect(NVGR.nanoContext, x+0.5f, y+0.5f, width-1f, height -1f, (radius-1f).coerceAtLeast(0f))
        nvgStrokeWidth(NVGR.nanoContext, 1f)
        nvgRGBA( 0.toByte(), 0.toByte(),0.toByte(), 64.toByte(), nanoColor)
        nvgStrokeColor(NVGR.nanoContext, nanoColor)
        nvgStroke(NVGR.nanoContext)

        nvgFontSize(NVGR.nanoContext, fontSize)
        nvgFontFaceId(NVGR.nanoContext, font.id)
        nvgTextAlign(NVGR.nanoContext, NVG_ALIGN_LEFT or NVG_ALIGN_MIDDLE)
        NVGR.setFillColor(color)
        nvgText(NVGR.nanoContext, x + height * 0.3f, y + height*0.5f + 2, text)
    }

    private fun drawButton(vg: Long, text: String, x: Float, y: Float, w: Float, h: Float, buttonColor: Int, textColor: Int)
    {
        val cornerRadius = 4.0f
        var tw = 0f
        val iw = 0f

        val bg: NVGPaint = nvgLinearGradient(vg, x,y,x,y+h, nvgRGBA(255.toByte(), 255.toByte(), 255.toByte(), 64, nanoColor), nvgRGBA(0,0,0,64, nanoColor2), nanoPaint)
        nvgBeginPath(vg)
        nvgRoundedRect(vg, x+1,y+1, w-2,h-2, cornerRadius-1)
        nvgFillColor(vg, NVGR.updateColor(buttonColor));
        nvgFill(vg);
        nvgFillPaint(vg, bg);
        nvgFill(vg);

        nvgBeginPath(vg);
        nvgRoundedRect(vg, x+0.5f,y+0.5f, w-1,h-1, cornerRadius-0.5f);
        nvgStrokeWidth(NVGR.nanoContext, 1.5f)
        nvgStrokeColor(vg, nvgRGBA(0,0,0,64, nanoColor));
        nvgStroke(vg);

        nvgFontSize(vg, 17.0f);
        nvgFontFaceId(vg, NVGFontManager.ROBOTO.id)
        tw = nvgTextBounds(vg, 0f,0f, text, null as FloatBuffer?)

        nvgFontSize(vg, 17.0f);
        nvgFontFaceId(vg, NVGFontManager.ROBOTO.id);
        nvgTextAlign(vg,NVG_ALIGN_LEFT or NVG_ALIGN_MIDDLE);
        nvgFillColor(vg, nvgRGBA(0,0,0, 200.toByte(), nanoColor));
        nvgText(vg, x+w*0.5f-tw*0.5f+iw*0.25f,y+h*0.5f,text);
        nvgFillColor(vg, NVGR.updateColor(textColor));
        nvgText(vg, x+w*0.5f-tw*0.5f+iw*0.25f,y+h*0.5f+1,text);
    }

    override val displayPerformance: Boolean = true
}
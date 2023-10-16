package floppacoding.mithras.ui.clickgui.util

import floppacoding.mithras.module.impl.render.GUIDesign
import floppacoding.mithras.module.impl.render.MainSettings
import java.awt.Color

/**
 * Provides color for the click gui.
 *
 * @author Aton
 */
object ColorUtil {
    val clickGUIColor: Color
        get() = MainSettings.color.value

    val elementColor: Int
     get() = if (MainSettings.design.isSelected(GUIDesign.NEW))
             NEW_COLOR
         else if (MainSettings.design.isSelected(GUIDesign.JELLYLIKE))
             JELLY_COLOR
         else
             0

    val bgColor: Int
        get() = if (MainSettings.design.isSelected(GUIDesign.NEW))
            NEW_COLOR
        else if (MainSettings.design.isSelected(GUIDesign.JELLYLIKE))
            Color(255,255,255,50).rgb
        else
            0

    val outlineColor : Int
        get() = clickGUIColor.darker().rgb

    val hoverColor: Int
        get() {
            val temp = clickGUIColor.darker()
            val scale = 0.5
            return Color(((temp.red*scale).toInt()), (temp.green*scale).toInt(), (temp.blue*scale).toInt()).rgb
        }

    val tabColor: Int
        get() = clickGUIColor.withAlpha(150).rgb

    fun sliderColor(dragging: Boolean): Int = clickGUIColor.withAlpha(if (dragging) 250 else 200).rgb

    fun sliderKnobColor(dragging: Boolean): Int = clickGUIColor.withAlpha(if (dragging) 255 else 230).rgb



    const val JELLY_COLOR = -0x44eaeaeb
    const val NEW_COLOR = -0xdcdcdd
    const val MODULE_BUTTON_COLOR = -0xe5e5e6
    const val TEXT_COLOR = -0x101011

    const val MODULE_HOVER_ENABLED = 0x55111111

    const val JELLY_PANEL_COLOR = -0x555556

    const val TAB_BACKGROUND_COLOR = 0x77000000
    const val DROPDOWN_COLOR = -0x55ededee
    const val BOX_HOVER_COLOR = 0x55111111
    const val SLIDER_BACKGROUND_COLOR = -0xefeff0

    const val BUTTON_COLOR = -0x1000000


    private fun Color.withAlpha(alpha: Int) : Color {
        return Color(this.red, this.green, this.blue, alpha)
    }

}
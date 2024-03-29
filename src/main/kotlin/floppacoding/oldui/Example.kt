package floppacoding.oldui

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.ModuleManager.modules
import floppacoding.mithras.module.impl.render.MainSettings
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.utils.ChatUtils.modMessage
import floppacoding.oldui.color.Animating
import floppacoding.oldui.color.Color
import floppacoding.oldui.color.IColor
import floppacoding.oldui.constraint.Align
import floppacoding.oldui.constraint.Aligning
import floppacoding.oldui.constraint.Constraints
import floppacoding.oldui.constraint.px
import floppacoding.oldui.elements.ElementOLD
import floppacoding.oldui.elements.impl.Rect
import floppacoding.oldui.events.onClick
import floppacoding.oldui.events.onMouseEnter
import floppacoding.oldui.events.onMouseExit

/*
*
* Ideal syntax goal
*
* Hypothetical ClickGUI implementation (starting from panel):
*
*   column(constraint = pos(panel.x, panel.y)) {
*       button(constraint = size(120, 20)) {
*           text(panel.name).center()
*       }
*       group {
*           for (module in modules) {
*               if (module.category != panel) continue
*               column {
*                   button(constraints = size(120, 15)) {
*                       text(module.name).center()
*                   }
*                   for (setting in module.settings) {
*                       setting.createElements() // make some abstract function in setting so they handle it themselves
*                   }
*               }
*           }
*       }
*       rect(constraint = size(120, 5))
*   }
*
*   // example function in the settings for BooleanSetting (example)
*   override fun createElements(): Element {
*       rect(constraints = size(120, 15)) {
*           text(at = pos(1, 3))
*           button(constraints = constraint(100, 2, 11, 11))
*       }
*   }
*
*
*/
// Positioning (X or Y) should be: defined with a number or aligned by left/top, center, right/bottom (with some padding)
// or undefined where it gets set by the parent's element (by default best option is to center)
//
// Sizing (Width or Height) should be: defined with a number, copy the parents size, or wrap around elements children,
// if left undefined, it is up to the element to set what it's default value should be

fun create(renderer2D: Renderer2D): UI {
    return UI(renderer2D).also { ui ->
        ui.main.apply { // tidy up dsl

            for (panel in Category.entries) {
                val x = panel.ordinal * 130 + 10

                column(at(x.px, 10.px)) {
                    rect(size(120.px, 20.px), color = Color(-0x55ededee)) {
                        text(panel.name).center()
                    }
                    column {
                        for (module in modules.filter { it.category == panel }) {
                            column {
                                button(constraints = size(120.px, 15.px), Color(-0xe5e5e6), ClickGUIColor, module.enabled) {
                                    text(module.name).center()

                                    onClick(1) {
                                        parent?.elements?.get(1)?.toggle()
                                        true
                                    }
                                } indentWidth 2

                                column {
                                    for (setting in module.settings) {
                                        when (setting) {
                                            is BooleanSetting -> BooleanSetting(setting)
                                            else -> continue
                                        }// indentWidth 4
                                    }
                                }.toggle()
                            }
                        }
                    }
                    rect(size(120.px, 5.px), color = Color(-0x55ededee))
                }
            }


        }
    }
}

fun ElementOLD.BooleanSetting(setting: BooleanSetting) =
    rect(size(120.px, 15.px), Color(-0x55ededee)) {
        text(setting.name, at(1.px, 3.px))
        button(constraint(Aligning(Align.END, 2f), 2.px, 11.px, 11.px), Color(-0xe5e5e6), ClickGUIColor, setting.value) {
            onClick(0) {
                modMessage("Pretends this toggles")
                true
            }
        }
    }

fun ElementOLD.button(
    constraints:Constraints,
    offColor: IColor,
    onColor: IColor,
    on: Boolean = false,
    block: Rect.() -> Unit
): Rect {
    val mainColor = Animating(offColor, onColor).apply { if (on) animate() }
    val hoverColor = Animating(Color(0, 0, 0, 0f), Color(0, 0, 0, 0.25f))
    return rect(constraints, mainColor) {
        rect(color = hoverColor) {
            onMouseEnter {
                hoverColor.animate()
                true
            }
            onMouseExit {
                hoverColor.animate()
                true
            }
        }.copySize()

        onClick(0) {
            mainColor.animate()
            false
        }
        block()
    }
}

//fun Element.toggle()


object ClickGUIColor : IColor {
    override val rgba: Int get() = MainSettings.color.value.rgb
}
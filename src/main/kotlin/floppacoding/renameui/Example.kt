package floppacoding.renameui

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.utils.ChatUtils.modMessage
import floppacoding.renameui.events.onClick
import java.awt.Color

/*
*
* Ideal syntax goal
*
* Hypothetical ClickGUI implemntation (starting from panel):
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


fun create(renderer2D: Renderer2D): UI {
    return UI(renderer2D).also { ui ->
        ui.main.apply { // tidy up dsl

            for (panel in Category.entries) {
                column(at(panel.ordinal * 130 + 10, 10)) {
                    rect(size(120, 20), color = Color(-0x55ededee)) {
                        text(panel.name).center()
                    }.onClick(0) {
                        modMessage("hello")
                        true
                    }
                    column {
                        for (module in ModuleManager.modules.filter { it.category == panel }) {
                            column {
                                rect(constraints = size(120, 15), color = Color(-0xe5e5e6)) {
                                    text(module.name).center()
                                }.indent(2f)
                            }
                        }
                    }
                    rect(size(120, 5), color = Color(-0x55ededee))
                }
            }
        }
    }
}
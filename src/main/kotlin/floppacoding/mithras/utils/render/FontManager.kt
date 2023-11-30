package floppacoding.mithras.utils.render

import floppacoding.aurora.core.font.AuroraFont
import floppacoding.aurora.mc_modern.AuroraMC
import floppacoding.mithras.Mithras

object FontManager {
    val ROBOTO = AuroraFont("roboto", "/assets/${Mithras.RESOURCE_DOMAIN}/gui/fonts/roboto-regular.ttf")
    val KURINTO = AuroraFont("kurinto", "/assets/${Mithras.RESOURCE_DOMAIN}/gui/fonts/KurintoSans-Rg.ttf")

    init {
        AuroraMC.defaultFont = ROBOTO
    }
}
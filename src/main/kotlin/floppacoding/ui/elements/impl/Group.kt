package floppacoding.ui.elements.impl

import floppacoding.ui.constraints.Constraints
import floppacoding.ui.constraints.sizes.Bounding
import floppacoding.ui.elements.Element
import floppacoding.ui.utils.replaceUndefined

class Group(constraints: Constraints?) : Element(constraints.replaceUndefined(w = Bounding, h = Bounding)) {
    override fun draw() {
//        renderer.border(x, y, width, height, 1f, java.awt.Color.WHITE.rgb)
    }
}
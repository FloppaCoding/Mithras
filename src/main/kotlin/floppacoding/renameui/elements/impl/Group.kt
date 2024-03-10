package floppacoding.renameui.elements.impl

import floppacoding.renameui.constraint.Bounding
import floppacoding.renameui.constraint.Constraints
import floppacoding.renameui.constraint.Placeholder
import floppacoding.renameui.elements.Element

class Group(constraints: Constraints?) : Element(constraints ?: Constraints(Placeholder, Placeholder, Bounding(), Bounding())) {
    override fun draw() {
       // renderer.border(x, y, width, height, 1f, Color.RED.rgb)
    }
}
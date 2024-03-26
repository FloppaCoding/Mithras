package floppacoding.oldui.elements.impl

import floppacoding.oldui.constraint.Bounding
import floppacoding.oldui.constraint.Constraints
import floppacoding.oldui.constraint.Placeholder
import floppacoding.oldui.elements.Element

class Group(constraints: Constraints?) : Element(constraints ?: Constraints(Placeholder, Placeholder, Bounding(), Bounding())) {
    override fun draw() {
       // renderer.border(x, y, width, height, 1f, Color.RED.rgb)
    }
}
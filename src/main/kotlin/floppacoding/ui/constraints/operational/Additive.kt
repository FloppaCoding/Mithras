package floppacoding.ui.constraints.operational

import floppacoding.ui.constraints.Constraint
import floppacoding.ui.constraints.Measurement
import floppacoding.ui.constraints.Type
import floppacoding.ui.elements.Element

class Additive(val first: Constraint, val second: Constraint) : Measurement {
    override fun get(element: Element, type: Type): Float = first.get(element, type) + second.get(element, type)

}

class Subtractive(val first: Constraint, val second: Constraint) : Measurement {
    override fun get(element: Element, type: Type): Float = first.get(element, type) - second.get(element, type)
}
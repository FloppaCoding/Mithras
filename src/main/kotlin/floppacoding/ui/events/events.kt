package floppacoding.ui.events

interface Event

interface Mouse : Event {

    class Clicked(val button: Int) : Mouse {
        override fun equals(other: Any?): Boolean { // needs to be overridden, so it is recognized in the eventsMap
            if (other !is Clicked) return false
            return button == other.button
        }

        override fun hashCode(): Int = 31 * (button + 500)

        override fun toString(): String = "MouseClicked(button=$button)"
    }

    class Released(val button: Int) : Mouse {
        override fun equals(other: Any?): Boolean {
            if (other !is Released) return false
            return button == other.button
        }

        override fun hashCode(): Int = 31 * (button + 250)

        override fun toString(): String = "MouseReleased(button=$button)"
    }

    object Entered : Mouse

    object Exited : Mouse

}
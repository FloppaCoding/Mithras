package floppacoding.ui.events

interface Event

interface Mouse : Event {

    class Clicked(val button: Int) : Mouse {
        override fun equals(other: Any?): Boolean { // needs to be overridden, so it is recognized in the eventsMap
            if (this === other) return true
            if (other !is Clicked) return false
            return button == other.button
        }

        override fun hashCode(): Int = 31 * (button + 500)

        override fun toString(): String = "MouseClicked(button=$button)"
    }

    class Released(val button: Int) : Mouse {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Released) return false
            return button == other.button
        }

        override fun hashCode(): Int = 31 * (button + 250)

        override fun toString(): String = "MouseReleased(button=$button)"
    }

    object Entered : Mouse

    object Exited : Mouse

    object Moved: Mouse
}

// todo: implement cleaner way?
// todo: implement key mods (i.e indicator for if ctrl and or shift is down)
interface Key : Event {
    class Typed(val code: Int?) : Key {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Typed) return false
            return other.code == null || code == other.code
        }

        override fun hashCode(): Int {
            return 31
        }
    }

    class Released(val code: Int?) : Key {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Released) return false
            return other.code == null || code == other.code
        }

        override fun hashCode(): Int {
            return 31 * 31
        }
    }
}

interface Focused : Event {

    object Gained : Focused

    object Lost : Focused

}
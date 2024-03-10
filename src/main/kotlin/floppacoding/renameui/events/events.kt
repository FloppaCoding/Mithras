package floppacoding.renameui.events

interface Event

interface Mouse : Event {

    class Clicked(val button: Int) : Mouse {
        override fun equals(other: Any?): Boolean {
            if (other !is Clicked) return false
            return button == other.button
        }

        override fun hashCode(): Int = 31 * (button + 500)

        override fun toString(): String = "Clicked(button=$button)"
    }

    class Released(val button: Int) : Mouse

}
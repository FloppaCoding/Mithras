package floppacoding.mithras.utils.clock

@Suppress("NOTHING_TO_INLINE")
/**
 * Class to simplify handling delays with [System.currentTimeMillis]
 *
 * @see [hasTimePassed]
 * @see [Executor]
 * @author Stivais
 */
class Clock(val delay: Long = 0L) {

    var lastTime = System.nanoTime()

    inline fun getTime(): Long {
        return System.nanoTime() - lastTime
    }

    inline fun setTime(time: Long) {
        lastTime = time
    }

    /**
     * Sets lastTime to now
     */
    inline fun update() {
        lastTime = System.nanoTime()
    }

    /**
     * @param setTime sets lastTime if time has passed
     */
    inline fun hasTimePassed(setTime: Boolean = false): Boolean {
        if (getTime() >= delay) {
            if (setTime) update()
            return true
        }
        return false
    }

    /**
     * @param delay the delay to check if it has passed since lastTime
     * @param setTime sets lastTime if time has passed
     */
    inline fun hasTimePassed(delay: Long, setTime: Boolean = false): Boolean {
        if (getTime() >= delay) {
            if (setTime) update()
            return true
        }
        return false
    }
}
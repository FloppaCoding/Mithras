package floppacoding.mithras.events

import meteordevelopment.orbit.ICancellable

/**
 * Implements the [ICancellable] interface with default behaviour, so that cancellable events don't always have to
 * implement the behaviour which will be the same most events anyway.
 *
 * You can still override the methods to have custom functionality when an event is cancelled.
 * @author Aton
 */
abstract class Cancellable: ICancellable {
    private var cancelled: Boolean = false
    override fun setCancelled(cancelled: Boolean) {
        this.cancelled = cancelled
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }
}
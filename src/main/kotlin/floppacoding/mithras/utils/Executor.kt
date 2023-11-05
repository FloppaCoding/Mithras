package floppacoding.mithras.utils

import floppacoding.mithras.utils.Extensions.hasTimePassed

/**
 * Class that allows repeating execution of code while being dynamic.
 * @author Stivais
 */
open class Executor(val delay: () -> Long, val func: Executable) {

    constructor(delay: Long, func: Executable) : this({ delay }, func)

    internal var time = System.nanoTime()
    internal var shouldFinish = false

    open fun run(): Boolean {
        if (shouldFinish) return true
        if (time.hasTimePassed(delay())) {
            runCatching {
                func()
                time = System.nanoTime()
            }
        }
        return false
    }

    /**
     * Starts an executor that ends after a certain amount of times.
     * @author Stivais
     */
    class LimitedExecutor(delay: Long, repeats: Int, func: Executable) : Executor(delay, func) {
        private val repeats = repeats - 1
        private var totalRepeats = 0

        override fun run(): Boolean {
            if (shouldFinish) return true
            if (time.hasTimePassed(delay())) {
                runCatching {
                    if (totalRepeats >= repeats) return true
                    totalRepeats++
                    func()
                    time = System.nanoTime()
                }
            }
            return false
        }
    }

    /**
     * Allows to stop executing an executor
     *
     * Returning [Nothing] allows for us to stop running the function without specifyinge
     * @author Stivais
     */
    fun Executor.destroyExecutor(): Nothing {
        shouldFinish = true
        throw Throwable()
    }

    companion object {
        // Make a global pool of executors
    }
}

/**
 * Here for more readability
 */
typealias Executable = Executor.() -> Unit
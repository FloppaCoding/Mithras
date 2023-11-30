package floppacoding.aurora.core.data

import org.lwjgl.opengl.GL45.glDeleteTextures
import org.lwjgl.system.MemoryUtil
import java.lang.ref.Cleaner
import java.nio.Buffer
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * # Memory management for classes requiring off heap memory
 *
 * This class provides functionality for automatic cleanup of off heap memory by the garbage collector (GC).
 *
 * Any additional memory management actions which are required to be performed by the garbage collector have to be
 * registered with [addCleanables]. This is meant to be used in combination with [registerTextureCleaner] and
 * [registerDirectBufferCleaner].
 *
 * This class also implements the [AutoCloseable] interface. So all the cleanup actions registered with [addCleanables]
 * will also be executed by its [close] method.
 *
 * While this class allows for automatic freeing of memory, it should when possible still be freed up manually.
 *
 * @author Aton
 */
abstract class OffHeapMemoryConsumer: AutoCloseable {

    private val cleanables: MutableList<Cleaner.Cleanable> = mutableListOf()

    /**
     * Tracks the given [cleanables] to be executed by the [close] method.
     */
    protected fun addCleanables(vararg cleanables: Cleaner.Cleanable) {
        this.cleanables.addAll(cleanables)
    }

    /**
     * Registers the open gl texture with the given [glID] to be freed by the GC.
     */
    protected fun registerTextureCleaner(glID: Int): Cleaner.Cleanable {
        return cleaner.register(this) { cleanupQueue.add { deleteTexture(glID) } }
    }

    /**
     * Registers the given buffer to be freed through [MemoryUtil.memFree] by the GC.
     */
    protected fun registerDirectBufferCleaner(buffer: Buffer): Cleaner.Cleanable {
        return cleaner.register(this) { MemoryUtil.memFree(buffer) }
    }

    /**
     * Frees the off heap memory of this object.
     */
    override fun close() {
        cleanables.forEach { it.clean() }
    }

    companion object {
        private val cleaner: Cleaner = Cleaner.create()
        private val cleanupQueue: ConcurrentLinkedQueue<() -> Unit> = ConcurrentLinkedQueue()

        /**
         * Replays all buffered cleanup actions which must be run on the main thread.
         */
        fun replayCleanupQueue() {
            while (cleanupQueue.isNotEmpty()) {
                cleanupQueue.poll()?.invoke()
            }
        }

        private fun deleteTexture(glID: Int) {
            glDeleteTextures(glID)
        }
    }
}
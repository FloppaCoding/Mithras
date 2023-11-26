package floppacoding.aurora.core.data

import org.lwjgl.system.MemoryUtil
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.ByteBuffer

object ResourceLoader {
    /**
     * Loads the resource to an off heap byte buffer for use with native libraries.
     * Example [path]:
     *
     *      "/assets/aurora/gui/icon.png"
     * The returned byte buffer has to be freed manually with [MemoryUtil.memFree].
     *
     * @throws FileNotFoundException When the file does not exist.
     */
    @Throws(IOException::class)
    fun resourceToByteBuffer(path: String): ByteBuffer {
        // This works for mod assets, otherwise "Files.newInputStream(file.toPath())" should be used.
        val stream = this::class.java.getResourceAsStream(path) ?: throw FileNotFoundException(path)
        val bytes = stream.readAllBytes()
        val data = MemoryUtil.memAlloc(bytes.size).put(0, bytes)
        stream.close()
        return data
    }
}
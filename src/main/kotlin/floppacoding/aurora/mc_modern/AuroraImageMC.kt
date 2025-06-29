package floppacoding.aurora.mc_modern

import floppacoding.aurora.core.images.AuroraImage
import floppacoding.aurora.core.images.Image
import net.minecraft.client.MinecraftClient
import net.minecraft.util.Identifier
import org.apache.commons.io.IOUtils
import java.io.FileNotFoundException
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Extension of [AuroraImage] with extra functionality for Minecraft.
 *
 * @param imageFlags Creation flags for the image. Any of:
 *
 * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
 * [REPEAT_X][Image.Flags.REPEAT_X],
 * [REPEAT_Y][Image.Flags.REPEAT_Y]
 * [FLIP_Y][Image.Flags.FLIP_Y],
 * [NEAREST][Image.Flags.NEAREST]
 * @author Aton
 */
class AuroraImageMC
@Throws(IOException::class) constructor(identifier: Identifier, vararg imageFlags: Image.Flags) :
    AuroraImage(byteBufferFromIdentifier(identifier), *imageFlags)
{
    companion object {
        private val mc = MinecraftClient.getInstance()

        /**
         * Gets the resource from the identifier as a byte buffer.
         *
         * @throws FileNotFoundException when the resource could not be loaded.
         */
        @Throws(IOException::class)
        private fun byteBufferFromIdentifier(identifier: Identifier): ByteBuffer {
            val resource = mc.resourceManager.getResource(identifier)
            val inputStream = if (resource.isPresent) {
                resource.get().inputStream
            } else { // try to get from skin cache TODO fix this for new version
//                val texture = mc.textureManager.getTexture(identifier)
//                val cacheFile = ((texture as? PlayerSkinTextureDownloader) as? PlayerSkinAccessor)?.cacheFile
//                if (cacheFile != null) {
//                    Files.newInputStream(cacheFile.toPath())
//                } else
                    throw FileNotFoundException(identifier.namespace + ":" + identifier.path)
            }
            val bytes = IOUtils.toByteArray(inputStream)
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder())
                .put(bytes)
            (data as Buffer).flip()
            inputStream.close()
            return data
        }
    }
}
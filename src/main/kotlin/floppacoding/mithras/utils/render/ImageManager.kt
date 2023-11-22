package floppacoding.mithras.utils.render

import floppacoding.aurora.core.images.AuroraImage
import floppacoding.aurora.core.images.Image
import floppacoding.aurora.mc_modern.AuroraImageMC
import floppacoding.mithras.Mithras
import net.minecraft.util.Identifier
import java.io.IOException

object ImageManager {

    // TODO it might be better to combine these two classes into one and make the constructor private.
    // That way using the buffer is enforced.
    val ICON = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/gui/icon.png")
    val HUE_SCALE = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/gui/huescale.png")
    val CHROMA = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/gui/huescale20_lowres.png")

    //Dungeon Map
    val NEU_GREEN = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/green_check.png", Image.Flags.NEAREST)
    val NEU_WHITE = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/white_check.png", Image.Flags.NEAREST)
    val NEU_CROSS = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/cross.png", Image.Flags.NEAREST)
    val NEU_QUESTION  = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/neu/question.png", Image.Flags.NEAREST)
    val DEFAULT_GREEN = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/green_check.png", Image.Flags.NEAREST)
    val DEFAULT_WHITE  = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/white_check.png", Image.Flags.NEAREST)
    val DEFAULT_CROSS = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/cross.png", Image.Flags.NEAREST)
    val DEFAULT_QUESTION = AuroraImage("/assets/${Mithras.RESOURCE_DOMAIN}/dungeonmap/default/question.png", Image.Flags.NEAREST)

    private val bufferedImagesMC = mutableMapOf<Identifier, AuroraImage>()
    private val bufferedImages = mutableMapOf<String, AuroraImage>()

    @Throws(IOException::class)
    fun createImage(path: String, vararg imageFlags: Image.Flags): AuroraImage {
        val bufferedImage = bufferedImages[path]
        if (bufferedImage != null) return bufferedImage
        val newImage = AuroraImage(path, *imageFlags)
        bufferedImages[path] = newImage
        return newImage
    }

    /**
     * @param imageFlags the image flags. Any of:
     *
     * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
     * [REPEATX][Image.Flags.REPEAT_X],
     * [IMAGE_REPEATY][Image.Flags.REPEAT_Y]
     * [FLIPY][Image.Flags.FLIP_Y],
     * [NEAREST][Image.Flags.NEAREST]
     */
    @Throws(IOException::class)
    fun createImage(identifier: Identifier, vararg imageFlags: Image.Flags): AuroraImage {
        val bufferedImage = bufferedImagesMC[identifier]
        if (bufferedImage != null) return bufferedImage
        val newImage = AuroraImageMC(identifier, *imageFlags)
        bufferedImagesMC[identifier] = newImage
        return newImage
    }
}
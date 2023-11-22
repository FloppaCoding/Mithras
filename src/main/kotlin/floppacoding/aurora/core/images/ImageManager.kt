package floppacoding.aurora.core.images

import floppacoding.mithras.Mithras
import floppacoding.mithras.utils.render.nanovg.NVGImageManager
import floppacoding.mithras.utils.render.nanovg.NVGR
import net.minecraft.util.Identifier
import java.io.IOException

object ImageManager {

    val ICON: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.ICON
            else -> GLImageManager.ICON
        }
    val HUE_SCALE: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.HUE_SCALE
            else -> GLImageManager.HUE_SCALE
        }
    val CHROMA: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.CHROMA
            else -> GLImageManager.CHROMA
        }

    //Dungeon Map
    val NEU_GREEN: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.NEU_GREEN
            else -> GLImageManager.NEU_GREEN
        }
    val NEU_WHITE: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.NEU_WHITE
            else -> GLImageManager.NEU_WHITE
        }
    val NEU_CROSS: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.NEU_CROSS
            else -> GLImageManager.NEU_CROSS
        }
    val NEU_QUESTION: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.NEU_QUESTION
            else -> GLImageManager.NEU_QUESTION
        }
    val DEFAULT_GREEN: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.DEFAULT_GREEN
            else -> GLImageManager.DEFAULT_GREEN
        }
    val DEFAULT_WHITE: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.DEFAULT_WHITE
            else -> GLImageManager.DEFAULT_WHITE
        }
    val DEFAULT_CROSS: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.DEFAULT_CROSS
            else -> GLImageManager.DEFAULT_CROSS
        }
    val DEFAULT_QUESTION: Image
        get() = when(Mithras.renderer2D){
            NVGR -> NVGImageManager.DEFAULT_QUESTION
            else -> GLImageManager.DEFAULT_QUESTION
        }

    /**
     * @param imageFlags the image flags. Any of:
     *
     * [GENERATE_MIPMAPS][Image.Flags.GENERATE_MIPMAPS],
     * [REPEATX][Image.Flags.REPEATX],
     * [IMAGE_REPEATY][Image.Flags.REPEATY]
     * [FLIPY][Image.Flags.FLIPY],
     * [NEAREST][Image.Flags.NEAREST]
     */
    @Throws(IOException::class)
    fun createImage(identifier: Identifier, vararg imageFlags: Image.Flags): Image {
        return when(Mithras.renderer2D){
            NVGR -> NVGImageManager.createImage(identifier, *imageFlags)
            else -> GLImageManager.createImage(identifier, *imageFlags)
        }
    }
}
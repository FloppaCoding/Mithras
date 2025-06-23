package floppacoding.aurora.mc_modern

import floppacoding.aurora.core.images.Image
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.GlTexture
import net.minecraft.client.texture.TextureManager
import net.minecraft.util.Identifier

class AuroraSkinImage @Throws(ClassCastException::class) constructor(identifier: Identifier): Image {

    override val glID: Int
    override val width: Int
    override val height: Int
    override val flags: List<Image.Flags>

     init {
         this.flags = listOf(Image.Flags.NEAREST)
         val textureManager: TextureManager = MinecraftClient.getInstance().textureManager
         val abstractTexture: AbstractTexture = textureManager.getTexture(identifier)
         val glTexture = abstractTexture.glTexture as GlTexture
         glID = glTexture.glId
         width = glTexture.getWidth(0)
         height = glTexture.getHeight(0)
     }
}
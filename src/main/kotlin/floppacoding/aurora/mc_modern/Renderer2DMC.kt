package floppacoding.aurora.mc_modern

import floppacoding.aurora.core.Renderer2D
import floppacoding.mithras.utils.render.nanovg.NVGR.beginFrame
import floppacoding.mithras.utils.render.nanovg.NVGR.endFrame
import net.minecraft.client.gui.DrawContext

interface Renderer2DMC : Renderer2D {

    /**
     * This method in not required in this library, as dimensions are handled internally.
     * It therefore should do nothing.
     */
    override fun setDimensions(width: Int, height: Int) {}

    /**
     * Begins drawing a new frame.
     *
     * This sets up the coordinate space such that the top left corner of the screen is the origin.
     * The coordinates of the bottom right corner of the screen are
     * [[mc.window.width][net.minecraft.client.util.Window.width], [mc.window.height][net.minecraft.client.util.Window.height]].
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     */
    override fun beginFrame()

    /**
     * Begins drawing a new frame.
     *
     * Sets the current transformation based on the given [context].
     *
     * All further rendering instructions have to be wrapped in [beginFrame] amd [endFrame].
     * @see setTransform
     */
    fun beginFrame(context: DrawContext) {
        beginFrame()
        setTransform(context)
    }

    /**
     * Sets the current transform so that the coordinate system matches that of the given [context].
     * This also takes the current [GUI Scale][net.minecraft.client.util.Window.scaleFactor] into account and scales
     * the coordinate system accordingly. The GUI scale is not present in the [context]. In the vanilla rendering it gets
     * applied separately through the [ProjectionMatix][com.mojang.blaze3d.systems.RenderSystem.getProjectionMatrix] in
     * the vertex shader.
     *
     * After calling this method
     *
     *      rect(0f,0f,100f,50f,-1)
     * should produce the same result as
     *
     *      context.fill(0,0,100,50,-1)
     */
    fun setTransform(context: DrawContext)
}
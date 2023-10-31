package floppacoding.mithras.module.impl.render

import floppacoding.mithras.Mithras
import floppacoding.mithras.events.InputEvent
import floppacoding.mithras.events.MouseScrollEvent
import floppacoding.mithras.events.WorldChangeEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.utils.JavaExtensions
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.option.SimpleOption

/**
 * A replacement for the beloved Optifine zoom.
 *
 * @author Aton
 */
object Zoom : Module(
    "Zoom",
    category = Category.RENDER,
    description = "Lets you zoom in by pressing the key bind."
){
    private val smoothCamera by BooleanSetting("Smooth Camera", true, description = "Smoothes out the camera movement when zoomed in.")
    private val scrollToZoom by BooleanSetting("Scroll Zoom", true, description = "When zoomed in scrolling will change the zoom level.")


    private var wasSmoothCameraEnabled = false

    private const val BASE_FOV: Int = 50
    private const val MIN_FOV: Int = 2 // THIS VALUE MUST BE GREATER THAN 1 OR THE GAME CAN CRASH!

    private var zoomState by NumberSetting("State", 5, MIN_FOV, BASE_FOV, visibility = Visibility.HIDDEN)

    /**
     * Used as a stand in for the games fov setting when zoomed in.
     */
    private val fakeFov = JavaExtensions.provideFakeFov(MIN_FOV, BASE_FOV)
    private var isZoomed = false


    /**
     * Returns the option with which the FOV should be overridden.
     * Returns null when it should not be overridden.
     * @see floppacoding.mithras.mixin.GameOptionsMixin.onGetFov
     */
    fun fovOverride(): SimpleOption<Int>? {
        return if (this.enabled && isZoomed) {
            fakeFov
        }else
            null
    }

    /**
     * Prevent the keybind from toggling the module.
     */
    override fun onKeyBind() {}

    override fun onDisable() {
        isZoomed = false
        super.onDisable()
    }

    /**
     * Disable the module when the key is released.
     */
    @EventHandler
    fun onInput(event: InputEvent) {
        if(event.key == this.keyBind) {
            when(event.action) {
                InputEvent.RELEASED -> {
                    isZoomed = false
                    Mithras.mc.options.smoothCameraEnabled = wasSmoothCameraEnabled
                    Mithras.moduleConfig.saveConfig()
                }
                InputEvent.PRESSED -> {
                    // Prevents this from being activated in a gui
                    if (Mithras.mc.currentScreen != null) return
                    isZoomed = true
                    fakeFov.value = zoomState
                    if (smoothCamera) {
                        wasSmoothCameraEnabled = Mithras.mc.options.smoothCameraEnabled
                        Mithras.mc.options.smoothCameraEnabled = true
                    }
                }
            }
        }
    }

    @EventHandler
    fun onScroll(event: MouseScrollEvent) {
        if (!isZoomed || !scrollToZoom) return
        // Linearized discrete approximation of a 1/x zooming behaviour.
        val delta = -(((zoomState * zoomState).toDouble() / BASE_FOV  * 0.3).coerceAtLeast(1.0) * event.amount).toInt()
        zoomState += delta // The Number setting takes care of coercing the value in the allowed range.
        fakeFov.value = zoomState
        event.cancel()
    }

    @EventHandler
    fun onWarp(event: WorldChangeEvent) {
        isZoomed = false
    }
}
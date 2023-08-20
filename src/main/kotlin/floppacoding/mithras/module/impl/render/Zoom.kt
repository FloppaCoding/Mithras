package floppacoding.mithras.module.impl.render

import com.mojang.serialization.Codec
import floppacoding.mithras.Mithras
import floppacoding.mithras.events.InputEvent
import floppacoding.mithras.events.MouseScrollEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.GameOptions.getGenericValueText
import net.minecraft.client.option.SimpleOption
import net.minecraft.text.Text

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
    private val fakeFov = SimpleOption(
        "options.fov",
        SimpleOption.emptyTooltip(),
        // This returns the displayed value in the gui (I think)
        SimpleOption.ValueTextGetter { optionText: Text?, value: Int ->
            return@ValueTextGetter getGenericValueText(optionText, value)
        },
        // This sets the range
        SimpleOption.ValidatingIntSliderCallbacks(MIN_FOV, BASE_FOV),
        // It seems like this might map between slider position with a Double value ranging from -1 to 1
        // and the value of this setting.
        Codec.DOUBLE.xmap(
            { value: Double -> (value * 40.0+70.0).toInt() }, { value:Int -> (value.toDouble()-70.0) / 40.0 }
        ),
        BASE_FOV,  // Default value
        ( // This gets run when the value is changed (I think)
            { _: Int -> MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate() }
        )
    )


    /**
     * Returns the option with which the FOV should be overridden.
     * Returns null when it should not be overridden.
     * @see floppacoding.mithras.mixin.GameOptionsMixin.onGetFov
     */
    fun fovOverride(): SimpleOption<Int>? {
        return if (this.enabled) {
            fakeFov
        }else
            null
    }

    /**
     * Prevent the message from showing.
     */
    override fun onKeyBind() {
        toggle()
    }

    override fun onEnable() {
        if (Mithras.mc.currentScreen != null){
            // Prevents this from being activated in a gui
            toggle()
            return
        }
        fakeFov.value = zoomState
        if (smoothCamera) {
            wasSmoothCameraEnabled = Mithras.mc.options.smoothCameraEnabled
            Mithras.mc.options.smoothCameraEnabled = true
        }
        super.onEnable()
    }

    override fun onDisable() {
        Mithras.mc.options.smoothCameraEnabled = wasSmoothCameraEnabled
        Mithras.moduleConfig.saveConfig()
        super.onDisable()
    }

    /**
     * Disable the module when the key is released.
     */
    @EventHandler
    fun onInput(event: InputEvent) {
        if(event.key == this.keyBind && event.action == InputEvent.RELEASED) {
            toggle()
        }
    }

    @EventHandler
    fun onScroll(event: MouseScrollEvent) {
        if (!scrollToZoom) return
        // Linearized discrete approximation of a 1/x zooming behaviour.
        val delta = -(((zoomState * zoomState).toDouble() / BASE_FOV  * 0.3).coerceAtLeast(1.0) * event.amount).toInt()
        zoomState += delta // The Number setting takes care of coercing the value in the allowed range.
        fakeFov.value = zoomState
        event.cancel()
    }
}
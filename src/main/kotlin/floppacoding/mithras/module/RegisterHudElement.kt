package floppacoding.mithras.module

/**
 * Annotate your [HUDElements][floppacoding.mithras.ui.hud.HudElement] with this to register them.
 *
 * This annotation tells the [ModuleManager] to take care of registering the hud element for you.
 * It only works for objects inheriting from [HudElement][floppacoding.mithras.ui.hud.HudElement] declared within your module.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RegisterHudElement

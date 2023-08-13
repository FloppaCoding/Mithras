package floppacoding.mithras.module.settings

/**
 * This annotation can be used for properties in [Module][floppacoding.mithras.module.Module] classes which delegate to a [Setting] class.
 * It prevents the delegation from registering the Setting to the Module.
 * @author Aton
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class DoNotRegister

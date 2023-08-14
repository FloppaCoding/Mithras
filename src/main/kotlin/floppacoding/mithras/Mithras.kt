package floppacoding.mithras

import floppacoding.mithras.commands.MithrasCommandManager
import floppacoding.mithras.config.ModuleConfig
import floppacoding.mithras.events.GameStartEvent
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.ui.clickgui.ClickGUI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import meteordevelopment.orbit.EventBus
import meteordevelopment.orbit.EventHandler
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.invoke.MethodHandles
import kotlin.coroutines.EmptyCoroutineContext


object Mithras : ModInitializer {
    val logger = LoggerFactory.getLogger("mithras")

	const val MOD_ID = "mithras"
	const val MOD_NAME = "Project Mithras"
	const val MOD_VERSION = "0.0.1"
	const val CHAT_PREFIX = "§6§lProject §r§eMithras §6§l»§r"
	const val SHORT_PREFIX = "§6§lF§r§eC §6§l»§r"
	const val RESOURCE_DOMAIN = "mithras"
	const val CONFIG_DOMAIN = "mithras"


	@JvmField
	val mc: MinecraftClient = MinecraftClient.getInstance()

	@JvmField
	val EVENT_BUS = EventBus()

	var display: Screen? = null

	val scope = CoroutineScope(EmptyCoroutineContext)

	val moduleConfig = ModuleConfig(File(mc.runDirectory, "config/$CONFIG_DOMAIN"))


	lateinit var clickGUI: ClickGUI


	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Initializing Project Mithras")

		// Loads in all modules and sets up automatically generated funtionality
		ModuleManager.loadModules()

		// Important for orbit, I don't yet know why
		EVENT_BUS.registerLambdaFactory("floppacoding.mithras") { lookupInMethod, klass ->
			lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
		}

		listOf(
			this,
			ModuleManager
		).forEach{ EVENT_BUS.subscribe(it) }

		// Load in the config
		// This has to be run before ModuleManager.initializeModules()
		runBlocking {
			launch(Dispatchers.IO) {
				moduleConfig.loadConfig()
			}
		}

		// Initialize all modules and register them to the eventbus
		ModuleManager.initializeModules()

		// Register the commands
		MithrasCommandManager.registerCommands()
	}

	@EventHandler
	fun onGameStart(event: GameStartEvent) {
		clickGUI = ClickGUI()
	}
}
package floppacoding.mithras

import floppacoding.mithras.commands.MithrasCommandManager
import floppacoding.mithras.config.ModuleConfig
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.FabricEventMapper
import floppacoding.mithras.events.GameStartEvent
import floppacoding.mithras.module.ModuleManager
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import floppacoding.mithras.ui.clickgui.ClickGUI
import floppacoding.mithras.ui.clickguinano.ClickGUINano
import floppacoding.mithras.utils.LocationManager
import kotlinx.coroutines.*
import meteordevelopment.orbit.EventBus
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.invoke.MethodHandles

object Mithras : ModInitializer {

    val logger: Logger = LoggerFactory.getLogger("mithras")

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

	private val handler = CoroutineExceptionHandler { _, exception ->
		logger.error("Mithras coroutine caught exception: $exception")
		exception.printStackTrace()
		mc.send {
			throw Error("Fatal Exception caught in Mithras coroutine.")
		}
	}
	val scope = CoroutineScope(Dispatchers.Default + handler + CoroutineName("mithras"))

	val moduleConfig = ModuleConfig(File(mc.runDirectory, "config/$CONFIG_DOMAIN"))

	var tickRamp = 0
		private set
	var totalTicks: Long = 0
		private set


	lateinit var clickGUI: ClickGUI
	lateinit var clickGUINano: ClickGUINano


	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Initializing Project Mithras")

		// Important for orbit, I don't yet know why
		EVENT_BUS.registerLambdaFactory("floppacoding.mithras") { lookupInMethod, klass ->
			lookupInMethod.invoke(null, klass, MethodHandles.lookup()) as MethodHandles.Lookup
		}

		FabricEventMapper.registerEvents()

		listOf(
			this,
			ModuleManager,
			LocationManager,
			Dungeon
		).forEach { EVENT_BUS.subscribe(it) }



		// Register the commands
		MithrasCommandManager.registerCommands()
	}

	@EventHandler
	fun onGameStart(event: GameStartEvent) {

		// Moved here from onInitialize because at that time some minecraft classes are not yet loaded in.
		// Loads in all modules and sets up automatically generated functionality
		ModuleManager.loadModules()

		// Load in the config
		// This has to be run before ModuleManager.initializeModules()
		runBlocking {
			launch(Dispatchers.IO) {
				moduleConfig.loadConfig()
			}
		}

		// Initialize all modules and register them to the eventbus
		ModuleManager.initializeModules()

		clickGUI = ClickGUI()
		clickGUINano = ClickGUINano()
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	fun onTick(event: ClientTickEvent) {
		if (event.phase != ClientTickEvent.Phase.START) return
		tickRamp = (tickRamp+1) % 20
		totalTicks++
	}
}
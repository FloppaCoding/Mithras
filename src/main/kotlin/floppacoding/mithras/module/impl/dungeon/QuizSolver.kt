package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.ChatReceivedEvent
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.events.WorldChangeEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.ConfigRoom
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.RoomUtils
import floppacoding.mithras.utils.ChatUtils.stripControlCodes
import floppacoding.mithras.utils.Extensions.containsOneOf
import floppacoding.mithras.utils.LocationManager.inDungeons
import floppacoding.mithras.utils.render.Renderer3D
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import java.awt.Color
import kotlin.math.floor


/**
 * Solver for Oruos quiz.
 *
 * Based on the [Skytils](https://github.com/Skytils/SkytilsMod) Trivia Solver.
 * @author Aton
 */
object QuizSolver  : Module(
    "Quiz Solver",
    Category.DUNGEON,
    "Shows you the correct answers for Oruos Puzzle."
) {

    private var triviaAnswers: List<String>? = null
    private var triviaAnswer: String? = null

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: ChatReceivedEvent) {
        if (!inDungeons || event.type != ChatReceivedEvent.Type.GAME_MESSAGE) return
        val unformatted = event.text.string.stripControlCodes()

        if (unformatted.startsWith("[STATUE] Oruo the Omniscient: ") && unformatted.contains("answered Question #") && unformatted.endsWith(
                "correctly!"
            )
        ) triviaAnswer = null
        if (unformatted.trim() == "What SkyBlock year is it?") {
            val currentTime = System.currentTimeMillis() / 1000.0
            val diff = floor(currentTime - 1560276000)
            val year = (diff / 446400 + 1).toInt()
            triviaAnswers = listOf("Year $year")
        } else {
            solutions.entries.find {
                unformatted.contains(it.key)
            }.also {
                if (it != null) triviaAnswers = it.value
            }
        }

        if (triviaAnswers != null && unformatted.trim().containsOneOf("ⓐ", "ⓑ", "ⓒ")) {
            triviaAnswers!!.find { unformatted.endsWith(it) }.also {
                if (it == null) {
                    event.replaceWith = Text.literal(event.text.string.replace("§a", "§c"))
                } else {
                    triviaAnswer = it
                }
            }
        }

    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (triviaAnswer == null || !RoomUtils.isInRoom(ConfigRoom.QUIZ)) return
        val room = Dungeon.currentRoom ?: return
        val box = Box(room.x - 15.0, 69.0, room.z-15.0, room.x + 15.0, 79.0, room.z+15.0)
        mc.world!!.getEntitiesByClass(ArmorStandEntity::class.java, box) {
            val name = it.customName?.string ?: return@getEntitiesByClass false
            name.containsOneOf("ⓐ","ⓑ","ⓒ") && name.contains(triviaAnswer!!)
        }.firstOrNull()?.let {
            Renderer3D.drawBlockBoundingBox(event.context, it.blockPos.up(), null, Color(20,255,40,150))

        }
    }

    @EventHandler
    fun onWorldChange(event: WorldChangeEvent) {
        triviaAnswer = null
    }

    /**
     * From Skytils.
     */
    private val solutions: Map<String,List<String>> = mapOf(
        "What is the status of The Watcher?" to listOf("Stalker"),
        "What is the status of Bonzo?" to listOf("New Necromancer"),
        "What is the status of Scarf?" to listOf("Apprentice Necromancer"),
        "What is the status of The Professor?" to listOf("Professor"),
        "What is the status of Thorn?" to listOf("Shaman Necromancer"),
        "What is the status of Livid?" to listOf("Master Necromancer"),
        "What is the status of Sadan?" to listOf("Necromancer Lord"),
        "What is the status of Maxor, Storm, Goldor, and Necron?" to listOf("The Wither Lords"),
        "How many total Fairy Souls are there?" to listOf("266 Fairy Souls"),
        "How many Fairy Souls are there in Spider's Den?" to listOf("19 Fairy Souls"),
        "How many Fairy Souls are there in Spiders Den?" to listOf("19 Fairy Souls"),
        "How many Fairy Souls are there in The End?" to listOf("12 Fairy Souls"),
        "How many Fairy Souls are there in The Farming Islands?" to listOf("20 Fairy Souls"),
        "How many Fairy Souls are there in Crimson Isle?" to listOf("29 Fairy Souls"),
        "How many Fairy Souls are there in The Park?" to listOf("12 Fairy Souls"),
        "How many Fairy Souls are there in Jerry's Workshop?" to listOf("5 Fairy Souls"),
        "How many Fairy Souls are there in Hub?" to listOf("80 Fairy Souls"),
        "How many Fairy Souls are there in The Hub?" to listOf("80 Fairy Souls"),
        "How many Fairy Souls are there in Deep Caverns?" to listOf("21 Fairy Souls"),
        "How many Fairy Souls are there in Gold Mine?" to listOf("12 Fairy Souls"),
        "How many Fairy Souls are there in Dungeon Hub?" to listOf("7 Fairy Souls"),
        "Which brother is on the Spider's Den?" to listOf("Rick"),
        "Which brother is on the Spiders Den?" to listOf("Rick"),
        "What is the name of Rick's brother?" to listOf("Pat"),
        "What is the name of the Painter in the Hub?" to listOf("Marco"),
        "What is the name of the person that upgrades pets?" to listOf("Kat"),
        "What is the name of the lady of the Nether?" to listOf("Elle"),
        "Which villager in the Village gives you a Rogue Sword?" to listOf("Jamie"),
        "How many unique minions are there?" to listOf("59 Minions"),
        "Which of these enemies does not spawn in the Spider's Den?" to listOf(
            "Zombie Spider",
            "Cave Spider",
            "Wither Skeleton",
            "Dashing Spooder",
            "Broodfather",
            "Night Spider"
        ),
        "Which of these enemies does not spawn in the Spiders Den?" to listOf(
            "Zombie Spider",
            "Cave Spider",
            "Wither Skeleton",
            "Dashing Spooder",
            "Broodfather",
            "Night Spider"
        ),
        "Which of these monsters only spawns at night?" to listOf("Zombie Villager", "Ghast"),
        "Which of these is not a dragon in The End?" to listOf(
            "Zoomer Dragon",
            "Weak Dragon",
            "Stonk Dragon",
            "Holy Dragon",
            "Boomer Dragon",
            "Booger Dragon",
            "Older Dragon",
            "Elder Dragon",
            "Stable Dragon",
            "Professor Dragon"
        )
    )
}
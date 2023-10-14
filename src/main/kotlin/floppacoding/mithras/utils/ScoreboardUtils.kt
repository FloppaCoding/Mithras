package floppacoding.mithras.utils

import floppacoding.mithras.Mithras.mc
import net.minecraft.scoreboard.ScoreboardPlayerScore
import net.minecraft.scoreboard.Team
import net.minecraft.text.LiteralTextContent
import net.minecraft.text.MutableText
import net.minecraft.util.Formatting

object ScoreboardUtils {
    /**
     * Removes formatting codes as well as special characters from the given string.
     */
    fun cleanSB(scoreboard: String?): String {
        return (Formatting.strip(scoreboard) ?: "").toCharArray().filter { it.code in 21..126 }.joinToString("")
    }

    /**
     * Returns unformatted scorebaord lines from the bottom up.
     *
     * This list does not contain formatting codes, but it can contain special characters.
     *
     * This works on hypixel but not on other servers.
     * Some other servers use [ScoreboardPlayerScore.playerName][net.minecraft.scoreboard.ScoreboardPlayerScore.playerName] to store formatted strings.
     * Hypixel uses [Team.prefix] and [Team.suffix] to store the line, but sets
     * [ScoreboardPlayerScore.playerName][net.minecraft.scoreboard.ScoreboardPlayerScore.playerName] to something of the
     * form "§h" with a letter that does not create a control code.
     */
    val sidebarLines: List<String>
        get() {
            val scoreboard = mc.world?.scoreboard ?: return emptyList()
            val objective = scoreboard.getObjectiveForSlot(1) ?: return emptyList()
            var scores = scoreboard.getAllPlayerScores(objective)
            scores = scores.filter {
                it?.playerName?.startsWith("#") == false
            }.let {
                if (it.size > 15) it.drop(15) else it
            }
            return scores.map {
                Team.decorateName(scoreboard.getPlayerTeam(it.playerName), MutableText.of(LiteralTextContent( ""))).string
                // it.playerName // This would work on some other servers instead.
            }
        }

    /**
     * Returns a list of the scores on the scoreboard.
     *
     * @see sidebarLines
     */
    val scores: List<ScoreboardPlayerScore>
        get() {
            val scoreboard = mc.world?.scoreboard ?: return emptyList()
            val objective = scoreboard.getObjectiveForSlot(1) ?: return emptyList()
            return scoreboard.getAllPlayerScores(objective).toList()
        }
}

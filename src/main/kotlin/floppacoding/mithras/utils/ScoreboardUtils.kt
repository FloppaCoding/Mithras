package floppacoding.mithras.utils

import floppacoding.mithras.Mithras.mc
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.scoreboard.ScoreboardEntry
import net.minecraft.scoreboard.Team
import net.minecraft.text.MutableText
import net.minecraft.text.PlainTextContent
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
            val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()
            var scores = scoreboard.getScoreboardEntries(objective)
            scores = scores.filter {
                it?.hidden() == false
            }.let {
                if (it.size > 15) it.drop(15) else it
            }
            return scores.map {
                Team.decorateName(scoreboard.getScoreHolderTeam(it.owner), MutableText.of(PlainTextContent.Literal( ""))).string
                // it.playerName // This would work on some other servers instead.
            }
        }

    /**
     * Returns a list of the scores on the scoreboard.
     *
     * @see sidebarLines
     */
    val scores: List<ScoreboardEntry>
        get() {
            val scoreboard = mc.world?.scoreboard ?: return emptyList()
            val objective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return emptyList()
            return scoreboard.getScoreboardEntries(objective).toList()
        }
}

package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.utils.ChatUtils.stripControlCodes
import floppacoding.mithras.utils.LocationManager.inDungeons
import floppacoding.mithras.utils.render.Renderer3D
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.network.OtherClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.boss.WitherEntity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.EndermanEntity
import net.minecraft.entity.passive.BatEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.Box
import java.awt.Color

/**
 * Highlights relevant dungeon mobs.
 * @author Aton
 */
object StarMobHighlight : Module(
    "Star Mob Highlights",
    category = Category.RENDER,
    description = "Draws a box around relevant dungeon mobs."
){
    private val defaultLineWidth by NumberSetting("Default LW",1.0f,0.1,10.0,0.1, description = "Default line width of the box.")
    private val specialLineWidth by NumberSetting("Special Mob LW",2.0f,0.1f,10.0f,0.1f, description = "Line width of the box for special mobs like Fel and Withermancer.")
    private val miniLineWidth by NumberSetting("Mini Boss LW",3.0f,0.1,10.0,0.1, description = "Line width of the box for Mini Bosses.")
    private val showStarMobs by BooleanSetting("Star Mobs", true, description = "Highlight star mobs.")
    private val showFelHead by BooleanSetting("Fel Head", true, description = "Render a box around Fel heads.")
    private val showBat by BooleanSetting("Bat ESP", true, description = "Highlight bats.")
    private val colorShadowAssassin by ColorSetting("SA Color", Color(255, 0, 255), false, description = "Color for Shadow Assassins.")
    private val colorMini by ColorSetting("Mini Boss Color", Color(255, 255, 0), false, description = "Color for all Mini Bosses except Shadow Assassins.")
    private val colorStar by ColorSetting("Star Mob Color", Color(255, 0, 0), false, description = "Color for star mobs.")
    private val colorFel  by ColorSetting("Fel Color", Color(0, 255, 255), false, description = "Color for star Fel.")
    private val colorFelHead by ColorSetting("Fel Head Color", Color(0, 0, 255), false, description = "Color for Fel heads on the floor.")
    private val colorWithermancer by ColorSetting("Withermancer Color", Color(255, 255, 0), false, description = "Color for star Withermancer.")
    private val colorBat by ColorSetting("Bat Color", Color(0, 255, 0), false, description = "Color for bats.")

    private val entityList: MutableList<HighlightEntity> = mutableListOf()

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (!inDungeons) return
        entityList.clear()
        mc.world!!.entities.forEach { entity ->
            val entityName = entity.name?.string?.stripControlCodes() ?: return@forEach
            when(entity) {
                is ArmorStandEntity -> {
                    if(showStarMobs && entityName.contains("✯")
                        && !entityName.contains("Angry Archeologist")
                        && !entityName.contains("Frozen Adventurer")
                        && !entityName.contains("Lost Adventurer")
                    ){ // starred mobs
                        val mob = getCorrespondingMob(entity) ?: return@forEach
                        val color: Color
                        val lineWidth: Float
                        if(entityName.contains("Fel")){ // Fel
                            color = colorFel
                            lineWidth = specialLineWidth
                        }else if(entityName.contains("Withermancer")){ // Withermancer
                            color = colorWithermancer
                            lineWidth = specialLineWidth
                        }else {
                            color = colorStar
                            lineWidth = defaultLineWidth
                        }
                        if (!mob.isInvisible)
                            entityList.add(HighlightEntity(mob,mob.boundingBox, color, lineWidth) )
                    }
                }
                is EndermanEntity -> {
                    if(showFelHead && entityName == "Dinnerbone"){
                        val box: Box = entity.boundingBox.shrink(0.0,2.4,0.0)
                        entityList.add(HighlightEntity(entity, box, colorFelHead, specialLineWidth))
                    }
                }
                is OtherClientPlayerEntity -> {
                    val color: Color? = if(entityName == "Shadow Assassin" && !entity.isInvisible){ // shadow assassin
                        colorShadowAssassin
                    } else if(entityName == "Diamond Guy" || entityName == "Lost Adventurer"){ // miniBoss
                        colorMini
                    }else null
                    if (color != null)
                        entityList.add(HighlightEntity(entity, entity.boundingBox, color, miniLineWidth))
                }
                is BatEntity -> {
                    if (showBat && !entity.isInvisible) {
                        entityList.add(HighlightEntity(entity, entity.boundingBox, colorBat, defaultLineWidth))
                    }
                }
            }
        }
    }

    @EventHandler
    fun onRenderWorld(event: RenderWorldOverlayEvent) {
        if (!inDungeons) return
        entityList.forEach {
            val dx: Double = event.context.tickDelta() * (it.entity.x - it.entity.lastRenderX)
            val dy: Double = event.context.tickDelta() * (it.entity.y - it.entity.lastRenderY)
            val dz: Double = event.context.tickDelta() * (it.entity.z - it.entity.lastRenderZ)
            Renderer3D.drawBox(event.context, it.box.offset(dx,dy,dz), it.color, null, it.lineWidth)
//            Renderer3D.drawBox(event.context, it.box, it.color, null, it.lineWidth)
        }
    }

    private fun getCorrespondingMob(entity: Entity): Entity? {
        val possibleEntities = entity.entityWorld.getOtherEntities(
            entity, entity.boundingBox.expand(0.2,0.0,0.2).offset(0.0, -1.0, 0.0)
        ) { it !is ArmorStandEntity }

        return possibleEntities.find {
            when (it) {
                is PlayerEntity -> !it.isInvisible() && it.uuid
                    .version() == 2 && it != mc.player
                is WitherEntity -> false
                else -> true
            }
        }
    }

    class HighlightEntity(
        val entity: Entity,
        val box: Box,
        val color: Color,
        val lineWidth: Float
    )
}
package floppacoding.mithras.utils

import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.abs
import kotlin.math.atan2

object GeometryHelper {

    /**
     * Returns a normalized vector in the direction specified by the view angles [pitch], [yaw],
     * according to Minecrafts coordinate system.
     * [net.minecraft.util.math.Vec3d.fromPolar] seems to do the same.
     */
    fun getDirection(pitch: Float, yaw: Float): Vec3d {
        val theta = -pitch * DEG_TO_RAD
        val phi = -yaw * DEG_TO_RAD
        val cPhi = MathHelper.cos(phi)
        val sPhi = MathHelper.sin(phi)
        val cTheta = MathHelper.cos(theta)
        val sTheta = MathHelper.sin(theta)
        return Vec3d((sPhi * cTheta).toDouble(), (sTheta).toDouble(), (cPhi * cTheta).toDouble())
    }

    val Vec3d.yaw: Float
        get() {
            return atan2(-this.x, this.z).toFloat() * RAD_TO_DEG
        }

    val Vec3d.yawRad: Float
        get() {
            return atan2(-this.x, this.z).toFloat()
        }

    /**
     * Returns the distance between two lines in 3D space.
     * The lines are passes as two points for each line.
     */
    fun distanceBetweenLines(pointA0: Vec3d, pointB0: Vec3d, pointA1: Vec3d, pointB1: Vec3d): Double {
        val vec1 =  pointB0.subtract(pointA0).normalize()
        val vec2 =  pointB1.subtract(pointA1).normalize()
        val normal = vec1.crossProduct(vec2).normalize()
        return if (normal.length() < 1.0E-4) {
            // the lines are parallell
            pointA0.subtract(pointA1).crossProduct(vec1).length()
        }else{
            // the lines are skew
            abs(pointA0.subtract(pointA1).dotProduct(normal))
        }
    }

    private const val DEG_TO_RAD: Float = Math.PI.toFloat() / 180f
    private const val RAD_TO_DEG: Float = 180f / Math.PI.toFloat()
}
package floppacoding.mithras.utils

import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.abs

object GeometryHelper {

    /**
     * Returns a normalized vector in the direction specified by the view angles [pitch], [yaw],
     * according to Minecrafts coordinate system.
     */
    fun getDirection(pitch: Float, yaw: Float): Vec3d {
        val theta = -pitch * 0.017453292f
        val phi = -yaw * 0.017453292f
        val cPhi = MathHelper.cos(phi)
        val sPhi = MathHelper.sin(phi)
        val cTheta = MathHelper.cos(theta)
        val sTheta = MathHelper.sin(theta)
        return Vec3d((sPhi * cTheta).toDouble(), (sTheta).toDouble(), (cPhi * cTheta).toDouble())
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
}
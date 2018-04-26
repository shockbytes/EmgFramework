package at.fhooe.mc.emg.core.util

object MathUtility {

    fun angleBetween2Lines(a1: PointD, a2: PointD, b1: PointD, b2: PointD): Double {
        val angle1 = Math.atan2(a2.y - a1.y, a1.x - a2.x).toFloat()
        val angle2 = Math.atan2(b2.y - b1.y, b1.x - b2.x).toFloat()

        val angle1Deg = Math.toDegrees(angle1.toDouble()) - 180
        val angle2Deg = Math.toDegrees(angle2.toDouble()) - 180

        var calculatedAngle: Double = angle1Deg - angle2Deg
        if (calculatedAngle < 0) calculatedAngle += 360.0
        return calculatedAngle
    }

}
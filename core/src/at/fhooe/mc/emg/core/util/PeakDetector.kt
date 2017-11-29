package at.fhooe.mc.emg.core.util

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
// TODO Implement peak detector
object PeakDetector {

    fun detectSimpleThresholdPeaks(yVals: DoubleArray, threshold: Int): Int {

        var peaks = 0
        var isRising = true
        for (yVal in yVals) {

            if (yVal > threshold && isRising) {
                isRising = false
            }
            if (yVal < threshold && !isRising) {
                peaks++
                isRising = true
            }
        }
        return peaks
    }

    fun detectPeakLocations(yVals: DoubleArray): List<Int>? {
        return null
    }

}

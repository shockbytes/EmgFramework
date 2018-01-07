package at.fhooe.mc.emg.core.util

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Author:  Martin Macheiner
 * Date:    08.07.2017
 */
// TODO Implement peaks detector
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

    fun detectPeakLocations(data: DoubleArray): Observable<IntArray> {
        return Observable.defer {
            Observable.just(IntArray(data.size))
        }.subscribeOn(Schedulers.computation())
    }

}

/*
 Copyright (C) 2001, 2006 by Simon Dixon 
 
 This program is free software; you can redistribute it and/or modify 
 it under the terms of the GNU General Public License as published by 
 the Free Software Foundation; either version 2 of the License, or 
 (at your option) any later version. 
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 GNU General Public License for more details. 
 
 You should have received a copy of the GNU General Public License along 
 with this program (the file gpl.txt); if not, download it from 
 http://www.gnu.org/licenses/gpl.txt or write to the 
 Free Software Foundation, Inc., 
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA. 
*/

package at.fhooe.mc.emg.core.tools.peaks

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

object PeakDetector {

    var debug = false
    var pre = 3
    var post = 1

    const val defaultWidth: Int = 40
    const val defaultThreshold: Double = 120.0
    const val defaultDecayRate: Double = 0.0
    const val defaultIsRelative: Boolean = false

    fun detectPeakLocations(data: DoubleArray, width: Int = defaultWidth,
                            threshold: Double = defaultThreshold,
                            decayRate: Double = defaultDecayRate,
                            isRelative: Boolean = defaultIsRelative): Observable<IntArray> {
        return Observable.fromCallable {
            findPeaks(data, width, threshold, decayRate, isRelative).toIntArray()
        }.subscribeOn(Schedulers.computation())
    }

    fun detectSimpleThresholdPeaks(data: DoubleArray, width: Int = defaultWidth,
                                   threshold: Double = defaultThreshold,
                                   decayRate: Double = defaultDecayRate,
                                   isRelative: Boolean = defaultIsRelative): Int {
        return detectPeakLocations(data, width, threshold, decayRate, isRelative)
                .timeout(200, TimeUnit.MILLISECONDS, Observable.just(intArrayOf())) // allow 200ms of delay, otherwise cancel
                .blockingFirst().size
    }


    /**
     * General peak picking method for finding n local maxima in an array
     *
     * @param data  input data
     * @param peaks list of peak indexes
     * @param width minimum distance between peaks
     */
    fun findPeaks(data: DoubleArray, peaks: IntArray, width: Int): Int {
        var peakCount = 0
        var maxp: Int
        var mid = 0
        val end = data.size
        while (mid < end) {
            var i = mid - width
            if (i < 0)
                i = 0
            var stop = mid + width + 1
            if (stop > data.size)
                stop = data.size
            maxp = i
            i++
            while (i < stop) {
                if (data[i] > data[maxp])
                    maxp = i
                i++
            }
            if (maxp == mid) {
                var j: Int = peakCount
                while (j > 0) {
                    if (data[maxp] <= data[peaks[j - 1]])
                        break
                    else if (j < peaks.size)
                        peaks[j] = peaks[j - 1]
                    j--
                }
                if (j != peaks.size)
                    peaks[j] = maxp
                if (peakCount != peaks.size)
                    peakCount++
            }
            mid++
        }
        return peakCount
    } // findPeaks()

    /**
     * General peak picking method for finding local maxima in an array
     *
     * @param data       input data
     * @param width      minimum distance between peaks
     * @param threshold  minimum value of peaks
     * @param decayRate  how quickly previous peaks are forgotten
     * @param isRelative minimum value of peaks is relative to local average
     * @return list of peak indexes
     */
    private fun findPeaks(data: DoubleArray, width: Int,
                          threshold: Double,
                          decayRate: Double = 0.0,
                          isRelative: Boolean = false): LinkedList<Int> {
        val peaks = LinkedList<Int>()
        var maxp: Int
        var mid = 0
        val end = data.size
        var av = data[0]
        while (mid < end) {
            av = decayRate * av + (1 - decayRate) * data[mid]
            if (av < data[mid])
                av = data[mid]
            var i = mid - width
            if (i < 0)
                i = 0
            var stop = mid + width + 1
            if (stop > data.size)
                stop = data.size
            maxp = i
            i++
            while (i < stop) {
                if (data[i] > data[maxp])
                    maxp = i
                i++
            }
            if (maxp == mid) {
                if (overThreshold(data, maxp, width, threshold, isRelative, av)) {
                    if (debug)
                        println(" peak")
                    peaks.add(maxp)
                } else if (debug)
                    println()
            }
            mid++
        }
        return peaks
    } // findPeaks()

    fun expDecayWithHold(_av: Double, decayRate: Double,
                         data: DoubleArray, _start: Int, stop: Int): Double {
        var av = _av
        var start = _start
        while (start < stop) {
            av = decayRate * av + (1 - decayRate) * data[start]
            if (av < data[start])
                av = data[start]
            start++
        }
        return av
    } // expDecayWithHold()

    fun overThreshold(data: DoubleArray, index: Int, width: Int,
                      threshold: Double, isRelative: Boolean,
                      av: Double): Boolean {
        if (debug)
            System.out.printf("%4d : %6.3f     Av1: %6.3f    ",
                    index, data[index], av)
        if (data[index] < av)
            return false
        if (isRelative) {
            var iStart = index - pre * width
            if (iStart < 0)
                iStart = 0
            var iStop = index + post * width
            if (iStop > data.size)
                iStop = data.size
            var sum = 0.0
            val count = iStop - iStart
            while (iStart < iStop)
                sum += data[iStart++]
            if (debug)
                System.out.printf("    %6.3f    %6.3f   ", sum / count,
                        data[index] - sum / count - threshold)
            return data[index] > sum / count + threshold
        } else
            return data[index] > threshold
    } // overThreshold()

    fun normalise(data: DoubleArray) {
        var sx = 0.0
        var sxx = 0.0
        for (i in data.indices) {
            sx += data[i]
            sxx += data[i] * data[i]
        }
        val mean = sx / data.size
        var sd = Math.sqrt((sxx - sx * mean) / data.size)
        if (sd == 0.0)
            sd = 1.0  // all data[i] == mean  -> 0; avoids div by 0
        for (i in data.indices) {
            data[i] = (data[i] - mean) / sd
        }
    } // normalise()

    /**
     * Uses an n-point linear regression to estimate the slope of data.
     *
     * @param data  input data
     * @param hop   spacing of data points
     * @param n     length of linear regression
     * @param slope output data
     */
    fun getSlope(data: DoubleArray, hop: Double, n: Int,
                 slope: DoubleArray) {
        var i = 0
        var j = 0
        var t: Double
        var sx = 0.0
        var sxx = 0.0
        var sy = 0.0
        var sxy = 0.0
        while (i < n) {
            t = i * hop
            sx += t
            sxx += t * t
            sy += data[i]
            sxy += t * data[i]
            i++
        }
        val delta = n * sxx - sx * sx
        while (j < n / 2) {
            slope[j] = (n * sxy - sx * sy) / delta
            j++
        }
        while (j < data.size - (n + 1) / 2) {
            slope[j] = (n * sxy - sx * sy) / delta
            sy += data[i] - data[i - n]
            sxy += hop * (n * data[i] - sy)
            j++
            i++
        }
        while (j < data.size) {
            slope[j] = (n * sxy - sx * sy) / delta
            j++
        }
    } // getSlope()

    fun min(arr: DoubleArray): Double {
        return arr[imin(arr)]
    }

    fun max(arr: DoubleArray): Double {
        return arr[imax(arr)]
    }

    fun imin(arr: DoubleArray): Int {
        var i = 0
        for (j in 1 until arr.size)
            if (arr[j] < arr[i])
                i = j
        return i
    } // imin()

    fun imax(arr: DoubleArray): Int {
        var i = 0
        for (j in 1 until arr.size)
            if (arr[j] > arr[i])
                i = j
        return i
    } // imax()

}
/**
 * General peak picking method for finding local maxima in an array
 *
 * @param data      input data
 * @param width     minimum distance between peaks
 * @param threshold minimum value of peaks
 * @return list of peak indexes
 */// findPeaks()
// class PeakDetector
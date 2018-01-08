/*
*   CLASS:      CurveSmooth
*
*   USAGE:      Class for smoothing a curve and obtaining the maxima and minima of a curve
*               Smoothing methods: moving average window or Savitzky-Golay filter
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:       February 2012
*   AMENDED:    26-27 February 2012, 3-17 March 2012 
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web pages:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*   http://www.ee.ucl.ac.uk/~mflanaga/java/Smooth.html
*
*   Copyright (c) 2012  Michael Thomas Flanagan
*
*   PERMISSION TO COPY:
*
* Permission to use, copy and modify this software and its documentation for NON-COMMERCIAL purposes is granted, without fee,
* provided that an acknowledgement to the author, Dr Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies
* and associated documentation or publications.
*
* Redistributions of the source code of this source code, or parts of the source codes, must retain the above copyright notice, this list of conditions
* and the following disclaimer and requires written permission from the Michael Thomas Flanagan:
*
* Redistribution in binary form of all or parts of this class must reproduce the above copyright notice, this list of conditions and
* the following disclaimer in the documentation and/or other materials provided with the distribution and requires written permission from the Michael Thomas Flanagan:
*
* Dr Michael Thomas Flanagan makes no representations about the suitability or fitness of the software for any or for a particular purpose.
* Dr Michael Thomas Flanagan shall not be liable for any damages suffered as a result of using, modifying or distributing this software
* or its derivatives.
*
***************************************************************************************/

package at.fhooe.mc.emg.core.util.filter.sg

class CurveSmooth(y: DoubleArray) {

    private var xData: DoubleArray = DoubleArray(y.size)                                 // original x data, y = f(x)
    private var yData: DoubleArray = y                                 // original y data, y = f(x)
    private var nPoints = 0                                        // number of data points
    private var yDataSavGol: DoubleArray? = null                            // Savitzky-Golay smoothed y data, y = f(x)

    init {

        (0 until y.size).mapIndexed { idx, _ -> xData[idx] = idx.toDouble() }
        check()
    }

    private fun check() {
        nPoints = xData.size
        val m = yData.size
        if (m != nPoints)
            throw IllegalArgumentException("The length of the x data array, $nPoints, must be the same as the length of the y data array, $m")
        if (m < 5) throw IllegalArgumentException("There must be at least five data points")
    }

    private fun windowLength(width: Int): Int {
        return if (isEven(width)) width + 1 else width
    }

    fun savitzkyGolay(sgWindowWidth: Int): DoubleArray? {
        this.yDataSavGol = DoubleArray(this.nPoints)
        val sgWindowWidth1 = this.windowLength(sgWindowWidth)
        this.savitzkyGolayCommon(sgWindowWidth1)
        return copy(this.yDataSavGol)
    }

    private fun savitzkyGolayCommon(width: Int): DoubleArray {
        val ww = (width - 1) / 2
        val coeff = savitzkyGolayFilter(ww, ww)[0]
        val psData = padData(yData, ww)
        for (i in ww until this.nPoints + ww) {
            var counter = 0
            val sum = (i - ww..i + ww).sumByDouble { psData[it] * coeff[counter++] }
            yDataSavGol?.set(i - ww, sum)
        }
        return yDataSavGol ?: DoubleArray(0)
    }

    private fun padData(data: DoubleArray, ww: Int): DoubleArray {
        val nn = data.size
        val psData = DoubleArray(nn + 2 * ww)
        System.arraycopy(data, 0, psData, ww, nn)
        for (i in 0 until ww) {
            psData[i] = psData[ww]
        }
        for (i in nn + ww until nn + 2 * ww) {
            psData[i] = psData[nn + ww - 1]
        }
        return psData
    }

    private fun savitzkyGolayFilter(bp: Int, fp: Int): Array<DoubleArray> {

        val ww = bp + fp + 1
        val values = IntArray(ww)
        for (i in 0 until ww) {
            values[i] = i - bp
        }

        val sgPolyDeg = 4
        val x = Array(ww) { DoubleArray(sgPolyDeg + 1) }
        for (i in 0 until ww) {
            for (j in 0 until sgPolyDeg + 1) {
                x[i][j] = Math.pow(values[i].toDouble(), j.toDouble())
            }
        }
        val matX = Matrix(x)
        val matT = matX.transpose()
        val matTX = matT.times(matX)
        val matI = matTX.inverse()
        val matC = matI.times(matT)
        return matC.arrayCopy
    }

    private fun isEven(x: Int): Boolean {
        var test = false
        if ((x % 2).toDouble() == 0.0) test = true
        return test
    }

    private fun copy(array: DoubleArray?): DoubleArray? {
        if (array == null) return null
        val n = array.size
        val copy = DoubleArray(n)
        System.arraycopy(array, 0, copy, 0, n)
        return copy
    }
}

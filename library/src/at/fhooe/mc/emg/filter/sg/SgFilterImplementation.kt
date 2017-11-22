/*
 * Copyright [2009] [Marcin Rzeźnicki]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package at.fhooe.mc.emg.filter.sg

import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.linear.SingularValueDecomposition
import java.lang.Math.pow
import java.util.*


/**
 * Savitzky-Golay filter implementation. For more information see
 * http://www.nrbook.com/a/bookcpdf/c14-8.pdf. This implementation,
 * however, does not use FFT
 *
 * @author Marcin Rzeźnicki
 */
class SgFilterImplementation
/**
 * Constructs Savitzky-Golay filter which uses specified numebr of
 * surrounding data points
 *
 * @param nl
 * numer of past data points filter will use
 * @param nr
 * numer of future data points filter will use
 * @throws IllegalArgumentException
 * of `nl < 0` or `nr < 0`
 */
(var nl: Int, var nr: Int) {

    init {
        if (nl < 0 || nr < 0)
            throw IllegalArgumentException("Bad arguments")
    }

    /**
     * Smooths data by using Savitzky-Golay filter. This method will use 0 for
     * any element beyond `data` which will be needed for computation (you
     * may want to use some [Preprocessor])
     *
     * @param data
     * data for filter
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws NullPointerException
     * when any array passed as parameter is null
     */
    fun smooth(data: DoubleArray, coeffs: DoubleArray): DoubleArray {
        return smooth(data, 0, data.size, coeffs)
    }

    /**
     * Smooths data by using Savitzky-Golay filter. Smoothing uses `leftPad` and/or `rightPad` if you want to augment data on
     * boundaries to achieve smoother results for your purpose. If you do not
     * need this feature you may pass empty arrays (filter will use 0s in this
     * place, so you may want to use appropriate preprocessor)
     *
     * @param data
     * data for filter
     * @param leftPad
     * left padding
     * @param rightPad
     * right padding
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws NullPointerException
     * when any array passed as parameter is null
     */
    fun smooth(data: DoubleArray, leftPad: DoubleArray, rightPad: DoubleArray,
               coeffs: DoubleArray): DoubleArray {
        return smooth(data, leftPad, rightPad, 0, arrayOf(coeffs))
    }

    /**
     * Smooths data by using Savitzky-Golay filter. Smoothing uses `leftPad` and/or `rightPad` if you want to augment data on
     * boundaries to achieve smoother results for your purpose. If you do not
     * need this feature you may pass empty arrays (filter will use 0s in this
     * place, so you may want to use appropriate preprocessor). If you want to
     * use different (probably non-symmetrical) filter near both ends of
     * (padded) data, you will be using `bias` and `coeffs`. `bias` essentially means
     * "how many points of pad should be left out when smoothing". Filters
     * taking this condition into consideration are passed in `coeffs`.
     * <tt>coeffs[0]</tt> is used for unbiased data (that is, for
     * <tt>data[bias]..data[data.length-bias-1]</tt>). Its length has to be
     * <tt>nr + nl + 1</tt>. Filters from range
     * <tt>coeffs[coeffs.length - 1]</tt> to
     * <tt>coeffs[coeffs.length - bias]</tt> are used for smoothing first
     * `bias` points (that is, from <tt>data[0]</tt> to
     * <tt>data[bias]</tt>) correspondingly. Filters from range
     * <tt>coeffs[1]</tt> to <tt>coeffs[bias]</tt> are used for smoothing last
     * `bias` points (that is, for
     * <tt>data[data.length-bias]..data[data.length-1]</tt>). For example, if
     * you use 5 past points and 5 future points for smoothing, but have only 3
     * meaningful padding points - you would use `bias` equal to 2 and
     * would pass in `coeffs` param filters taking 5-5 points (for regular
     * smoothing), 5-4, 5-3 (for rightmost range of data) and 3-5, 4-5 (for
     * leftmost range). If you do not wish to use pads completely for
     * symmetrical filter then you should pass <tt>bias = nl = nr</tt>
     *
     * @param data
     * data for filter
     * @param leftPad
     * left padding
     * @param rightPad
     * right padding
     * @param bias
     * how many points of pad should be left out when smoothing
     * @param coeffs
     * array of filter coefficients
     * @return filtered data
     * @throws IllegalArgumentException
     * when <tt>bias < 0</tt> or <tt>bias > min(nr, nl)</tt>
     * @throws IndexOutOfBoundsException
     * when `coeffs` has less than <tt>2*bias + 1</tt>
     * elements
     * @throws NullPointerException
     * when any array passed as parameter is null
     */
    fun smooth(data: DoubleArray, leftPad: DoubleArray, rightPad: DoubleArray,
               bias: Int, coeffs: Array<DoubleArray>): DoubleArray {

        var mutableData = data
        if (bias < 0 || bias > nr || bias > nl)
            throw IllegalArgumentException(
                    "bias < 0 or bias > nr or bias > nl")
        val dataLength = mutableData.size
        if (dataLength == 0)
            return mutableData
        val n = dataLength + nl + nr
        val dataCopy = DoubleArray(n)
        // copy left pad reversed
        val leftPadOffset = nl - leftPad.size
        if (leftPadOffset >= 0)
            for (i in leftPad.indices) {
                dataCopy[leftPadOffset + i] = leftPad[i]
            }
        else
            for (i in 0 until nl) {
                dataCopy[i] = leftPad[i - leftPadOffset]
            }
        // copy actual data
        for (i in 0 until dataLength) {
            dataCopy[i + nl] = mutableData[i]
        }
        // copy right pad
        val rightPadOffset = nr - rightPad.size
        if (rightPadOffset >= 0)
            for (i in rightPad.indices) {
                dataCopy[i + dataLength + nl] = rightPad[i]
            }
        else
            for (i in 0 until nr) {
                dataCopy[i + dataLength + nl] = rightPad[i]
            }

        // convolution (with savitzky-golay coefficients)
        val sdata = DoubleArray(dataLength)
        var sg: DoubleArray
        for (b in bias downTo 1) {
            sg = coeffs[coeffs.size - b]
            val x = nl + bias - b
            var sum = 0.0
            for (i in -nl + b..nr) {
                sum += dataCopy[x + i] * sg[nl - b + i]
            }
            sdata[x - nl] = sum
        }
        sg = coeffs[0]
        for (x in nl + bias until n - nr - bias) {
            var sum = 0.0
            for (i in -nl..nr) {
                sum += dataCopy[x + i] * sg[nl + i]
            }
            sdata[x - nl] = sum
        }
        for (b in 1..bias) {
            sg = coeffs[b]
            val x = n - nr - bias + (b - 1)
            var sum = 0.0
            for (i in -nl..nr - b) {
                sum += dataCopy[x + i] * sg[nl + i]
            }
            sdata[x - nl] = sum
        }
        return sdata
    }

    /**
     * Runs filter on data from `from` (including) to `to`
     * (excluding). Data beyond range spanned by `from` and `to`
     * will be used for padding
     *
     * @param data
     * data for filter
     * @param from
     * inedx of the first element of data
     * @param to
     * index of the first element omitted
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws ArrayIndexOutOfBoundsException
     * if <tt>to > data.length</tt>
     * @throws IllegalArgumentException
     * if <tt>from < 0</tt> or <tt>to > data.length</tt>
     * @throws NullPointerException
     * if `data` is null or `coeffs` is null
     */
    fun smooth(data: DoubleArray, from: Int, to: Int, coeffs: DoubleArray): DoubleArray {
        return smooth(data, from, to, 0, arrayOf(coeffs))
    }

    /**
     * Runs filter on data from `from` (including) to `to`
     * (excluding). Data beyond range spanned by `from` and `to`
     * will be used for padding. See
     * [.smooth] for usage
     * of `bias`
     *
     * @param data
     * data for filter
     * @param from
     * inedx of the first element of data
     * @param to
     * index of the first element omitted
     * @param bias
     * how many points of pad should be left out when smoothing
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws ArrayIndexOutOfBoundsException
     * if <tt>to > data.length</tt> or when `coeffs` has less
     * than <tt>2*bias + 1</tt> elements
     * @throws IllegalArgumentException
     * if <tt>from < 0</tt> or <tt>to > data.length</tt> or
     * <tt>from > to</tt> or when <tt>bias < 0</tt> or
     * <tt>bias > min(nr, nl)</tt>
     * @throws NullPointerException
     * if `data` is null or `coeffs` is null
     */
    fun smooth(data: DoubleArray, from: Int, to: Int, bias: Int,
               coeffs: Array<DoubleArray>): DoubleArray {
        val leftPad = Arrays.copyOfRange(data, 0, from)
        val rightPad = Arrays.copyOfRange(data, to, data.size)
        val dataCopy = Arrays.copyOfRange(data, from, to)
        return smooth(dataCopy, leftPad, rightPad, bias, coeffs)
    }

    /**
     * See [.smooth]. This method converts `data` to double for computation and then converts it back to float
     *
     * @param data
     * data for filter
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws NullPointerException
     * when any array passed as parameter is null
     */
    fun smooth(data: FloatArray, coeffs: DoubleArray): FloatArray {
        return smooth(data, 0, data.size, coeffs)
    }

    /**
     * See [.smooth]. This method
     * converts `data` `leftPad` and `rightPad` to double for
     * computation and then converts back to float
     *
     * @param data
     * data for filter
     * @param leftPad
     * left padding
     * @param rightPad
     * right padding
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws NullPointerException
     * when any array passed as parameter is null
     */
    fun smooth(data: FloatArray, leftPad: FloatArray, rightPad: FloatArray,
               coeffs: DoubleArray): FloatArray {
        return smooth(data, leftPad, rightPad, 0, arrayOf(coeffs))
    }

    /**
     * See [.smooth]. This
     * method converts `data` `leftPad` and `rightPad` to
     * double for computation and then converts back to float
     *
     * @param data
     * data for filter
     * @param leftPad
     * left padding
     * @param rightPad
     * right padding
     * @param bias
     * how many points of pad should be left out when smoothing
     * @param coeffs
     * array of filter coefficients
     * @return filtered data
     * @throws IllegalArgumentException
     * when <tt>bias < 0</tt> or <tt>bias > min(nr, nl)</tt>
     * @throws IndexOutOfBoundsException
     * when `coeffs` has less than <tt>2*bias + 1</tt>
     * elements
     * @throws NullPointerException
     * when any array passed as parameter is null
     */
    fun smooth(data: FloatArray, leftPad: FloatArray, rightPad: FloatArray,
               bias: Int, coeffs: Array<DoubleArray>): FloatArray {
        val dataAsDouble = DoubleArray(data.size)
        val leftPadAsDouble = DoubleArray(leftPad.size)
        val rightPadAsDouble = DoubleArray(rightPad.size)
        convertFloatArrayToDouble(data, dataAsDouble)
        convertFloatArrayToDouble(leftPad, leftPadAsDouble)
        convertFloatArrayToDouble(rightPad, rightPadAsDouble)
        val results = smooth(dataAsDouble, leftPadAsDouble,
                rightPadAsDouble, bias, coeffs)
        val resultsAsFloat = FloatArray(results.size)
        convertDoubleArrayToFloat(results, resultsAsFloat)
        return resultsAsFloat
    }

    /**
     * See [.smooth]. This method converts
     * `data` to double for computation and then converts it back to float
     *
     * @param data
     * data for filter
     * @param from
     * inedx of the first element of data
     * @param to
     * index of the first element omitted
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws ArrayIndexOutOfBoundsException
     * if <tt>to > data.length</tt>
     * @throws IllegalArgumentException
     * if <tt>from < 0</tt> or <tt>to > data.length</tt>
     * @throws NullPointerException
     * if `data` is null or `coeffs` is null
     */
    fun smooth(data: FloatArray, from: Int, to: Int, coeffs: DoubleArray): FloatArray {
        return smooth(data, from, to, 0, arrayOf(coeffs))
    }

    /**
     * See [.smooth]. This method
     * converts `data` to double for computation and then converts it back
     * to float
     *
     * @param data
     * data for filter
     * @param from
     * inedx of the first element of data
     * @param to
     * index of the first element omitted
     * @param bias
     * how many points of pad should be left out when smoothing
     * @param coeffs
     * filter coefficients
     * @return filtered data
     * @throws ArrayIndexOutOfBoundsException
     * if <tt>to > data.length</tt> or when `coeffs` has less
     * than <tt>2*bias + 1</tt> elements
     * @throws IllegalArgumentException
     * if <tt>from < 0</tt> or <tt>to > data.length</tt> or
     * <tt>from > to</tt> or when <tt>bias < 0</tt> or
     * <tt>bias > min(nr, nl)</tt>
     * @throws NullPointerException
     * if `data` is null or `coeffs` is null
     */
    fun smooth(data: FloatArray, from: Int, to: Int, bias: Int,
               coeffs: Array<DoubleArray>): FloatArray {
        val leftPad = Arrays.copyOfRange(data, 0, from)
        val rightPad = Arrays.copyOfRange(data, to, data.size)
        val dataCopy = Arrays.copyOfRange(data, from, to)
        return smooth(dataCopy, leftPad, rightPad, bias, coeffs)
    }

    companion object {

        /**
         * Computes Savitzky-Golay coefficients for given parameters
         *
         * @param nl
         * numer of past data points filter will use
         * @param nr
         * number of future data points filter will use
         * @param degree
         * order of smoothin polynomial
         * @return Savitzky-Golay coefficients
         * @throws IllegalArgumentException
         * if `nl < 0` or `nr < 0` or `nl + nr <
         * degree`
         */
        fun computeSGCoefficients(nl: Int, nr: Int, degree: Int): DoubleArray {
            if (nl < 0 || nr < 0 || nl + nr < degree)
                throw IllegalArgumentException("Bad arguments")
            val matrix = MatrixUtils.createRealMatrix(degree + 1, degree + 1)
            val a = matrix.data
            var sum: Double
            for (i in 0..degree) {
                for (j in 0..degree) {
                    sum = (if (i == 0 && j == 0) 1 else 0).toDouble()
                    for (k in 1..nr)
                        sum += pow(k.toDouble(), (i + j).toDouble())
                    for (k in 1..nl)
                        sum += pow((-k).toDouble(), (i + j).toDouble())
                    a[i][j] = sum
                }
            }
            var b = DoubleArray(degree + 1)
            b[0] = 1.0

            val bVec = MatrixUtils.createRealVector(b)
            val solver = SingularValueDecomposition(matrix).solver
            b = solver.solve(bVec).toArray()

            val coeffs = DoubleArray(nl + nr + 1)
            for (n in -nl..nr) {
                sum = b[0]
                for (m in 1..degree)
                    sum += b[m] * pow(n.toDouble(), m.toDouble())
                coeffs[n + nl] = sum
            }
            return coeffs
        }

        private fun convertDoubleArrayToFloat(`in`: DoubleArray, out: FloatArray) {
            for (i in `in`.indices)
                out[i] = `in`[i].toFloat()
        }

        private fun convertFloatArrayToDouble(`in`: FloatArray, out: DoubleArray) {
            for (i in `in`.indices)
                out[i] = `in`[i].toDouble()
        }
    }
}
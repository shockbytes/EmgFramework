package at.fhooe.mc.emg.util

import org.jtransforms.fft.DoubleFFT_1D
import java.util.*

object Analysis {

    fun fft(input: DoubleArray): DoubleArray {

        val fftDo = DoubleFFT_1D(input.size.toLong())
        val fft = DoubleArray(input.size * 2)
        System.arraycopy(input, 0, fft, 0, input.size)
        fftDo.realForwardFull(fft)
        return fft
    }

    fun powerSpectrum(fft: DoubleArray, fs: Double): Pair<DoubleArray, DoubleArray> {

        val N = fft.size
        val resolution = fs / N
        val spectrum = DoubleArray(N / 2 - 1)
        val xData = DoubleArray(N / 2 - 1)
        Arrays.setAll(xData) { i -> AppUtils.roundDouble(i * resolution, 2) }
        for (k in 2 until N / 2 - 1) {
            spectrum[k] = AppUtils.roundDouble(Math.sqrt(Math.pow(fft[2 * k], 2.0) + Math.pow(fft[2 * k + 1], 2.0)), 2)
        }

        return Pair(xData, spectrum)
    }


}

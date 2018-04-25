package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.core.analysis.model.MeanMedianFrequency
import at.fhooe.mc.emg.core.util.CoreUtils
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.jtransforms.fft.DoubleFFT_1D
import java.util.*

object AnalysisUtils {

    fun fft(input: DoubleArray): Single<DoubleArray> {
        return if (input.isNotEmpty()) {
            Single.fromCallable {
                val fftDo = DoubleFFT_1D(input.size.toLong())
                val fft = DoubleArray(input.size * 2)
                System.arraycopy(input, 0, fft, 0, input.size)
                fftDo.realForwardFull(fft)
                fft
            }.subscribeOn(Schedulers.computation())
        } else {
            Single.error(IllegalArgumentException("Input for FFT must not be empty!"))
        }
    }

    fun powerSpectrum(fft: DoubleArray, fs: Double): Single<Pair<DoubleArray, DoubleArray>> {
        return if (fft.isNotEmpty()) {
            Single.fromCallable {
                val n = fft.size
                val resolution = fs / n
                val spectrum = DoubleArray(n / 2 - 1)
                val xData = DoubleArray(n / 2 - 1)
                Arrays.setAll(xData) { i -> CoreUtils.roundDouble(i * resolution, 2) }
                for (k in 2 until n / 2 - 1) {
                    spectrum[k] = CoreUtils.roundDouble(Math.sqrt(Math.pow(fft[2 * k], 2.0) + Math.pow(fft[2 * k + 1], 2.0)), 2)
                }
                Pair(xData, spectrum)
            }.subscribeOn(Schedulers.computation())
        } else {
            Single.error(IllegalArgumentException("Input for power spectrum must not be empty!"))
        }
    }

    fun meanMedianFrequency(input: DoubleArray, fs: Double): Single<MeanMedianFrequency> {
        return if (input.isNotEmpty()) {
            Single.fromCallable {
                // TODO
                MeanMedianFrequency(0.0, 0.0, fs)
            }.subscribeOn(Schedulers.computation())
        } else {
            Single.error(IllegalArgumentException("Input for mean and median must not be empty!"))
        }
    }

}

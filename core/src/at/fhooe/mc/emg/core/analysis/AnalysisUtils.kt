package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.core.util.CoreUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jtransforms.fft.DoubleFFT_1D
import java.util.*

object AnalysisUtils {

    fun fft(input: DoubleArray): Observable<DoubleArray> {
        return if (!input.isEmpty()) {
            Observable.defer {
                val fftDo = DoubleFFT_1D(input.size.toLong())
                val fft = DoubleArray(input.size * 2)
                System.arraycopy(input, 0, fft, 0, input.size)
                fftDo.realForwardFull(fft)
                Observable.just(fft)
            }.subscribeOn(Schedulers.computation())
        } else {
            Observable.error(IllegalArgumentException("Input for FFT must not be empty!"))
        }
    }

    fun powerSpectrum(fft: DoubleArray, fs: Double): Observable<Pair<DoubleArray, DoubleArray>> {
        return if (!fft.isEmpty()) {
            Observable.defer {
                val n = fft.size
                val resolution = fs / n
                val spectrum = DoubleArray(n / 2 - 1)
                val xData = DoubleArray(n / 2 - 1)
                Arrays.setAll(xData) { i -> CoreUtils.roundDouble(i * resolution, 2) }
                for (k in 2 until n / 2 - 1) {
                    spectrum[k] = CoreUtils.roundDouble(Math.sqrt(Math.pow(fft[2 * k], 2.0) + Math.pow(fft[2 * k + 1], 2.0)), 2)
                }
                Observable.just(Pair(xData, spectrum))
            }.subscribeOn(Schedulers.computation())
        } else {
            Observable.error(IllegalArgumentException("Input for power spectrum must not be empty!"))
        }
    }

}

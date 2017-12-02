package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.core.util.CoreUtils
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.jtransforms.fft.DoubleFFT_1D
import java.util.*

object AnalysisUtils {

    fun fft(input: DoubleArray): Observable<DoubleArray> {
        return Observable.defer {
            val fftDo = DoubleFFT_1D(input.size.toLong())
            val fft = DoubleArray(input.size * 2)
            System.arraycopy(input, 0, fft, 0, input.size)
            fftDo.realForwardFull(fft)
            Observable.just(fft)
        }.subscribeOn(Schedulers.computation())
    }

    fun powerSpectrum(fft: DoubleArray, fs: Double): Observable<Pair<DoubleArray, DoubleArray>> {
        return Observable.defer {
            val N = fft.size
            val resolution = fs / N
            val spectrum = DoubleArray(N / 2 - 1)
            val xData = DoubleArray(N / 2 - 1)
            Arrays.setAll(xData) { i -> CoreUtils.roundDouble(i * resolution, 2) }
            for (k in 2 until N / 2 - 1) {
                spectrum[k] = CoreUtils.roundDouble(Math.sqrt(Math.pow(fft[2 * k], 2.0) + Math.pow(fft[2 * k + 1], 2.0)), 2)
            }
            Observable.just(Pair(xData, spectrum))
        }.subscribeOn(Schedulers.computation())
    }


}

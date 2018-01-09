package at.fhooe.mc.emg.core.analysis

/**
 * Author:  Mescht
 * Date:    29.11.2017
 */
class FrequencyAnalysisMethod(private val method: Method, private val input: DoubleArray,
                              private val sampleFrequency: Double) {

    enum class Method {
        FFT, SPECTRUM
    }

    fun evaluate(view: FrequencyAnalysisView?) {
        doCalculation(method, input, sampleFrequency, view)
    }

    private fun doCalculation(method: FrequencyAnalysisMethod.Method, input: DoubleArray,
                              fs: Double, view: FrequencyAnalysisView?) {

        AnalysisUtils.fft(input).subscribe({ fft ->
            when (method) {
                FrequencyAnalysisMethod.Method.FFT -> showFFTPlot(fft, view)
                FrequencyAnalysisMethod.Method.SPECTRUM -> showPowerSpectrumPlot(fft, fs, view)
            }
        }, { throwable -> view?.showError(throwable) })
    }

    private fun showFFTPlot(fft: DoubleArray, view: FrequencyAnalysisView?) {
        val xData = DoubleArray(fft.size) { i -> i.toDouble() }
        view?.showEvaluation(Method.FFT, xData, fft)
    }

    private fun showPowerSpectrumPlot(fft: DoubleArray, fs: Double, view: FrequencyAnalysisView?) {
        AnalysisUtils.powerSpectrum(fft, fs).subscribe({ data ->
            view?.showEvaluation(Method.SPECTRUM, data.first, data.second)
        }, { throwable -> view?.showError(throwable) })
    }


}
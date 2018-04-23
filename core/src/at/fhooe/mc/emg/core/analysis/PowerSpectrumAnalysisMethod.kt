package at.fhooe.mc.emg.core.analysis

import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent

@EmgComponent(type = EmgComponentType.SINK)
class PowerSpectrumAnalysisMethod(override var view: FrequencyAnalysisView? = null,
                                  override var fs: Double = 100.0) : FrequencyAnalysisMethod {

    override val name = "Power Spectrum"

    override val hasDisplay = true

    override fun calculate(input: DoubleArray) {
        AnalysisUtils.fft(input).subscribe({ fft -> showPowerSpectrumPlot(fft, fs, view) })
    }

    private fun showPowerSpectrumPlot(fft: DoubleArray, fs: Double, view: FrequencyAnalysisView?) {
        AnalysisUtils.powerSpectrum(fft, fs).subscribe({ data ->
            view?.showEvaluation("Power Spectrum", data.first, data.second)
        }, { throwable -> view?.showError(throwable) })
    }
}
package at.fhooe.mc.emg.core.analysis

/**
 * Author:  Mescht
 * Date:    29.11.2017
 */
interface FrequencyAnalysisView {

    fun showEvaluation(method: FrequencyAnalysisMethod.Method,
                       xData: DoubleArray, yData: DoubleArray)

    fun showError(error: Throwable)
}
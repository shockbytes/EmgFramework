package at.fhooe.mc.emg.analysis

/**
 * Author:  Mescht
 * Date:    29.11.2017
 */
interface FrequencyAnalysisView {

    fun showEvaluation(method: FrequencyAnalysisMethod.Method,
                       xData: DoubleArray, yData: DoubleArray)
}
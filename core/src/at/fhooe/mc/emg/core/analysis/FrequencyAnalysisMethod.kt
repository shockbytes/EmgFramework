package at.fhooe.mc.emg.core.analysis

/**
 * Author:  Mescht
 * Date:    29.11.2017
 */

interface FrequencyAnalysisMethod {

    val name: String

    val hasDisplay: Boolean

    var view: FrequencyAnalysisView?

    var fs: Double

    /**
     * TODO
     * @param input
     * @param fs
     * @param view
     *
     * @return
     */
    fun calculate(input: DoubleArray)

}
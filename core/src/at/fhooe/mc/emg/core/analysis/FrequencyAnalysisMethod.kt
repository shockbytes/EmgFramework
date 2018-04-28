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
     * @param input series of input values of a single channel
     */
    fun calculate(input: DoubleArray)

}
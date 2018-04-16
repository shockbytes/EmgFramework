package at.fhooe.mc.emg.core.analysis

import io.reactivex.Single

/**
 * Author:  Mescht
 * Date:    29.11.2017
 */

interface FrequencyAnalysisMethod {

    val name: String

    val hasDisplay: Boolean

    /**
     * TODO
     * @param input
     * @param fs
     * @param view
     *
     * @return
     */
    fun calculate(input: DoubleArray, fs: Double, view: FrequencyAnalysisView?): Single<Double>

}
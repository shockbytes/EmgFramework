package at.fhooe.mc.emg.visual

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.filter.Filter

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
interface Visual<out T> {

    val dataForFrequencyAnalysis: DoubleArray

    val view: T

    fun initialize()

    fun update(data: ChannelData, filters: List<Filter>)

    fun setYMaximum(maximum: Double)

    fun reset()

}

package at.fhooe.mc.emg.core.view

import at.fhooe.mc.emg.clientdriver.ChannelData
import at.fhooe.mc.emg.core.filter.Filter
import io.reactivex.Scheduler

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
interface VisualView<out T> {

    val dataForFrequencyAnalysis: DoubleArray

    val view: T

    val scheduler: Scheduler?

    val requestScheduler: Boolean
        get() = scheduler != null

    val bufferSpan: Long

    val requestBufferedUpdates: Boolean
        get() = bufferSpan > 0

    fun initialize()

    fun update(data: ChannelData, filters: List<Filter>)

    fun setYMaximum(maximum: Double)

    fun reset()

}

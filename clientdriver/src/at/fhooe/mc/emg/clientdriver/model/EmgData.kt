package at.fhooe.mc.emg.clientdriver.model

import at.fhooe.mc.emg.clientdriver.model.writable.Csvable
import at.fhooe.mc.emg.clientdriver.model.writable.Plotable
import java.io.Serializable
import java.util.*

class EmgData(private val maxAmount: Int = defaultMaxAmount,
              private var channels: MutableList<MutableList<EmgPoint>> = ArrayList()) : Csvable, Plotable, Serializable {

    val channelCount: Int
        get() = channels.size

    fun section(start: Int, stop: Int, channel: Int): EmgData {

        return if ((channel <= 0 && channel >= channels.size) ||
                (start < 0 || stop > channels[0].size)) {
            throw IllegalArgumentException("Channel $channel or interval [$start, $stop] out of range!")
        } else {
            val chSize = channels[channel].size
            val subSection = channels[channel].drop(start).dropLast(chSize-stop).toMutableList()
            EmgData(channels = mutableListOf(subSection))
        }
    }

    fun updateChannel(channel: Int, point: EmgPoint) {

        // Add channels if not available
        if (channel >= channels.size) {
            val diffChannels = channel - channels.size + 1
            addChannels(diffChannels)
        }
        channels[channel].add(point)
    }

    // --------------------------------------------

    override fun plotData(channel: Int): List<EmgPoint> {

        return if (channel < channelCount) {
            // Drop the first entries and just receive The last in the window of
            // maxAmount, but this will preserve the X value when entering the plot
            val drops = Math.max(0, channels[channel].size - maxAmount)
            channels[channel].drop(drops)
        } else {
            throw IllegalArgumentException("Channel $channel out of range ($channelCount)")
        }
    }

    override fun asCsv(excelCompat: Boolean): String {

        val sb = StringBuilder()

        if (excelCompat) {
            val headerPrefix = "sep=,\n"
            sb.append(headerPrefix)
        }

        // ----------- Header line -----------
        val header = StringBuilder("time,")
        for (i in channels.indices) {
            header.append("ch_").append(i)
            if (i < channels.size - 1) {
                header.append(",")
            }
        }
        sb.append(header).append("\n")

        // To avoid IndexOutOfBounds exceptions just take the smallest channel size
        // This code is legacy from ChannelData and should not occur in EmgData
        val minSize = Collections
                .min(channels, Comparator.comparingInt({ it.size }))
                .size

        for (i in 0 until minSize) {
            // Assume that the first channel contains
            // all X points for the other channels as well
            sb.append(channels[0][i].x)
            sb.append(",")
            for (j in channels.indices) {
                sb.append(channels[j][i].y)
                if (i < channels.size - 1) {
                    sb.append(", ")
                }
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    // --------------------------------------------

    private fun addChannels(numOfChannels: Int) {
        for (i in 0 until numOfChannels) {
            channels.add(ArrayList())
        }
    }

    companion object {

        val defaultMaxAmount = 512
    }

}
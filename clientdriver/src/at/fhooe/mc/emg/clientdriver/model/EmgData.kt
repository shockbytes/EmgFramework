package at.fhooe.mc.emg.clientdriver.model

import at.fhooe.mc.emg.clientdriver.model.writable.Csvable
import at.fhooe.mc.emg.clientdriver.model.writable.Plotable
import java.io.Serializable
import java.util.*

/**
 * @author  Martin Macheiner
 * Date:    11.11.2017
 *
 *
 *
 * @param windowWidth
 * @param channels
 * @param heartRateData
 *
 */
class EmgData(private val windowWidth: Int = DEFAULT_WINDOW_WITH,
              private val channels: MutableList<MutableList<EmgPoint>> = ArrayList(),
              val heartRateData: MutableList<Int> = mutableListOf()) : Csvable, Plotable, Serializable {

    val channelCount: Int
        get() = channels.size

    val emgSize: Int
        get() = channels[0].size

    /**
     * Creates a new EmgData instance for the desired section. This method is mainly used for external tool, which
     * are only interested in some parts of the captured data.
     *
     * @param start Start point of the desired section in the stored data stream
     * @param stop End point of the desired section in the stored data stream
     * @param channel Desired channel, as it is assumed the data provides multiple channels
     *
     * @return A new EmgData instance only containing data of the desired section boundaries.
     */
    fun section(start: Int, stop: Int, channel: Int): EmgData {
        return if ((channel <= 0 && channel >= channels.size) || (start < 0 || stop > channels[0].size)) {
            throw IllegalArgumentException("Channel $channel or interval [$start, $stop] out of range!")
        } else {
            val channelSize = channels[channel].size
            // Maybe use .takeLast()
            val emgSection = channels[channel].drop(start).dropLast(channelSize - stop).toMutableList()
            val hrSection = heartRateData.drop(start).dropLast(channelSize - stop).toMutableList()
            EmgData(DEFAULT_WINDOW_WITH, mutableListOf(emgSection), hrSection)
        }
    }

    /**
     * Returns the last entry point of a single channel of the data
     * @param channel Desired channel, as it is assumed the data provides multiple channels
     *
     * @return The last EmgPoint of the given channel
     */
    fun lastOfChannel(channel: Int): EmgPoint {
        return channels[channel].last()
    }

    /**
     * Returns the last entry point of all channels of the data
     *
     * @return The last EmgPoint of all channels
     */
    fun lastOfChannels(): List<EmgPoint> {
        return channels.map { it.last() }
    }

    /**
     * Returns the last entry of recorded heart rate values
     *
     * @return The last heart rate value
     */
    fun lastOfHeartRate(): Int {
        return heartRateData.last()
    }

    /**
     * Updates a channel with a given EmgPoint.
     *
     * @param channel Indicates the channel to update with the data
     * @param point EmgPoint containing measured Emg data
     */
    fun updateChannel(channel: Int, point: EmgPoint) {

        // Add channels if not available
        if (channel >= channels.size) {
            val diffChannels = channel - channels.size + 1
            addChannels(diffChannels)
        }
        channels[channel].add(point)
    }

    /**
     * Update the list of heart rate values with the current value
     *
     * @param hr The current heart rate
     */
    fun updateHeartRate(hr: Int) {
        heartRateData.add(hr)
    }

    /**
     * Delete all previosly stored data
     */
    fun reset() {
        channels.clear()
        heartRateData.clear()
    }

    // --------------------------------------------

    override fun plotData(channel: Int): List<EmgPoint> {
        return if (channel < channelCount) {
            // Drop the first entries and just receive The last in the window of
            // windowWidth, but this will preserve the X value when entering the plot
            val drops = Math.max(0, channels[channel].size - windowWidth)
            channels[channel].drop(drops)
        } else {
            throw IllegalArgumentException("Channel $channel out of range ($channelCount)")
        }
    }

    /**
     * Output format:
     *      timestamp:channel1,channel2:timestamp
     */
    override fun asCsv(excelCompat: Boolean): String {

        val sb = StringBuilder()

        val sep = ":"

        if (excelCompat) {
            val headerPrefix = "sep=$sep\n"
            sb.append(headerPrefix)
        }

        // ----------- Header line -----------
        val header = StringBuilder("time$sep")
        for (i in channels.indices) {
            header.append("ch_").append(i)
            if (i < channels.size - 1) {
                header.append(sep)
            }
        }
        sb.append(header).append("\n")

        // To avoid IndexOutOfBounds exceptions just take the smallest channel size
        // This code is legacy from ChannelData and should not occur in EmgData
        val minSize = Collections
                .min(channels, { o1, o2 -> Integer.compare(o1.size, o2.size) })
                //.min(channels, Comparator.comparingInt({ it.size }))
                .size

        for (i in 0 until minSize) {
            // Assume that the first channel contains
            // all X points for the other channels as well
            sb.append(channels[0][i].timestamp)
            sb.append(sep)
            for (j in channels.indices) {
                sb.append(channels[j][i].y)
                if (i < channels.size - 1) {
                    sb.append("$, ")
                }
            }
            val hr = heartRateData.getOrNull(i) ?: -1
            sb.append(sep)
            sb.append(hr)

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

        const val DEFAULT_WINDOW_WITH = 512
    }

}
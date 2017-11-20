package at.fhooe.mc.emg.client

import java.io.Serializable
import java.util.*
import java.util.stream.IntStream

class ChannelData : Serializable {

    @Volatile private var yChartValues: MutableList<MutableList<Double>>
    @Volatile private var xChartValues: MutableList<MutableList<Double>>

    private var maxAmount: Int = 0

    val channelCount: Int
        get() = yChartValues.size

    val csvLogOutput: String
        @Synchronized get() {

            val headerPrefix = "sep=,\n"

            val sb = StringBuilder()
            sb.append(headerPrefix)

            val header = StringBuilder("time,")
            for (i in yChartValues.indices) {
                header.append("channel_").append(i)
                if (i < yChartValues.size - 1) {
                    header.append(",")
                }
            }
            sb.append(header).append("\n")

            val minSize = Collections.min(yChartValues, Comparator.comparingInt({ it.size })).size

            for (i in 0 until minSize) {
                sb.append(xChartValues[0][i])
                sb.append(",")
                for (j in yChartValues.indices) {
                    sb.append(yChartValues[j][i])
                    if (i < yChartValues.size - 1) {
                        sb.append(", ")
                    }
                }
                sb.append("\n")
            }

            return sb.toString()
        }

    @JvmOverloads constructor(maxAmount: Int = DEFAULT_MAX_AMOUNT) {
        this.maxAmount = maxAmount

        xChartValues = ArrayList()
        yChartValues = ArrayList()
    }

    constructor(xChartValues: MutableList<MutableList<Double>>, yChartValues: MutableList<MutableList<Double>>) {
        this.xChartValues = xChartValues
        this.yChartValues = yChartValues
        this.maxAmount = DEFAULT_MAX_AMOUNT
    }

    private fun addSeries(num: Int) {
        for (i in 0 until num) {
            addSeries()
        }
    }

    private fun addSeries() {
        yChartValues.add(ArrayList())
        xChartValues.add(ArrayList())
    }

    fun updateXYSeries(channel: Int, x: Double, y: Double) {

        // Add channels if not available
        if (channel >= yChartValues.size) {
            addSeries(channel - yChartValues.size + 1)
        }

        updateXSeries(channel, x)
        updateYSeries(channel, y)
    }

    private fun updateYSeries(channel: Int, value: Double) {
        yChartValues[channel].add(value)
    }

    private fun updateXSeries(channel: Int, value: Double) {
        xChartValues[channel].add(value)
    }

    @Synchronized
    fun getXSeries(channel: Int): DoubleArray {
        val vals = xChartValues[channel].toTypedArray()
        return Arrays.stream(vals)
                .mapToDouble({ it.toDouble() })
                .skip(Math.max(0, vals.size - maxAmount).toLong())
                .toArray()
    }

    @Synchronized
    fun getYSeries(channel: Int): DoubleArray {
        val vals = yChartValues[channel].toTypedArray()
        return Arrays.stream(vals)
                .mapToDouble({ it.toDouble() })
                .skip(Math.max(0, vals.size - maxAmount).toLong())
                .toArray()
    }

    @Synchronized
    fun getSingleChannelSection(start: Int, stop: Int, channel: Int): ChannelData {

        if (channel <= 0 && channel >= yChartValues.size) {
            throw IllegalArgumentException("Channel $channel out of range!")
        }
        if (start < 0 || stop > yChartValues[0].size) {
            throw IllegalArgumentException("Start or stop out of range [$start, $stop]")
        }

        val subX = ArrayList<Double>()
        val subY = ArrayList<Double>()
        IntStream.range(start, stop).forEach { idx ->
            subX.add(xChartValues[channel][idx])
            subY.add(yChartValues[channel][idx])
        }

        return ChannelData(mutableListOf(subX),
                mutableListOf(subY))
    }

    companion object {

        private val DEFAULT_MAX_AMOUNT = 512
    }
}

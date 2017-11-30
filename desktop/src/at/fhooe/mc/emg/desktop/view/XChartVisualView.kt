package at.fhooe.mc.emg.desktop.view

import at.fhooe.mc.emg.clientdriver.ChannelData
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.view.VisualView
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.style.Styler
import java.awt.Color
import java.util.*
import javax.swing.JComponent

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
class XChartVisualView : VisualView<JComponent> {

    private lateinit var realtimeChart: org.knowm.xchart.XYChart
    private lateinit var chartWrapper: XChartPanel<org.knowm.xchart.XYChart>

    override val dataForFrequencyAnalysis: DoubleArray
        get() {
            val first = realtimeChart.seriesMap.values.iterator().next()
            return first.yData.stream().mapToDouble({ it.toDouble() }).toArray()
        }

    override val view: JComponent
        get() = chartWrapper

    init {
        initialize()
    }

    override fun initialize() {

        realtimeChart = org.knowm.xchart.XYChartBuilder().width(800).height(600).theme(Styler.ChartTheme.GGPlot2).build()
        realtimeChart.styler.legendPosition = Styler.LegendPosition.OutsideE
        realtimeChart.styler.isPlotGridLinesVisible = false
        realtimeChart.styler.plotBackgroundColor = Color.WHITE
        chartWrapper = XChartPanel(realtimeChart)
    }

    override fun update(data: ChannelData, filters: List<Filter>) {

        if (!addChannelsIfNecessary(data, filters)) {
            for (i in 0 until data.channelCount) {
                filters
                        .filter { it.isEnabled }
                        .forEach { filter ->
                            realtimeChart.updateXYSeries((i + 1).toString() + "." + filter.shortName,
                                    data.getXSeries(i),
                                    Arrays.stream(data.getYSeries(i)).map({ filter.step(it) }).toArray(), null)
                        }
            }
        }
        chartWrapper.repaint()
    }

    override fun setYMaximum(maximum: Double) {
        realtimeChart.styler.yAxisMax = maximum
    }

    override fun reset() {
        initialize()
        chartWrapper.invalidate()
    }

    private fun addChannelsIfNecessary(channelData: ChannelData, filters: List<Filter>): Boolean {

        val series = realtimeChart.seriesMap.entries.size
        val addSeries = series < channelData.channelCount
        if (addSeries) {
            for (i in series until channelData.channelCount) {
                filters
                        .filter { it.isEnabled }
                        .forEach { filter ->
                            realtimeChart.addSeries((i + 1).toString() + "." + filter.shortName,
                                    channelData.getXSeries(i),
                                    Arrays.stream(channelData.getYSeries(i)).map({ filter.step(it) }).toArray(), null)
                        }
            }
        }
        return addSeries
    }

}

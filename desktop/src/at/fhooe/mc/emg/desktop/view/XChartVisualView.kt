package at.fhooe.mc.emg.desktop.view

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.view.VisualView
import io.reactivex.Scheduler
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.style.Styler
import java.awt.Color
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

            return if (realtimeChart.seriesMap.values.iterator().hasNext()) {
                val first = realtimeChart.seriesMap.values.iterator().next()
                first.yData.map{ it.toDouble() }.toDoubleArray()
            } else {
                DoubleArray(0)
            }
        }

    override val view: JComponent
        get() = chartWrapper

    override val scheduler: Scheduler? = null
    override val bufferSpan: Long = -1

    init {
        initialize()
    }

    override fun initialize() {

        realtimeChart = org.knowm.xchart.XYChartBuilder()
                .width(800)
                .height(600)
                .theme(Styler.ChartTheme.GGPlot2)
                .build()

        realtimeChart.styler.legendPosition = Styler.LegendPosition.OutsideE
        realtimeChart.styler.isPlotGridLinesVisible = false
        realtimeChart.styler.plotBackgroundColor = Color.WHITE
        chartWrapper = XChartPanel(realtimeChart)
    }

    override fun update(data: EmgData, filters: List<Filter>) {

        if (!addChannelsIfNecessary(data, filters)) {
            for (i in 0 until data.channelCount) {
                filters
                        .filter { it.isEnabled }
                        .forEach { filter ->
                            val plotData = data.plotData(i)
                            realtimeChart.updateXYSeries((i + 1).toString() + "." + filter.shortName,
                                    plotData.map { it.x },
                                    plotData.map({ filter.step(it.y) }),
                                    null)
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

    private fun addChannelsIfNecessary(data: EmgData, filters: List<Filter>): Boolean {

        val series = realtimeChart.seriesMap.entries.size
        val addSeries = series < data.channelCount
        if (addSeries) {
            for (i in series until data.channelCount) {
                filters
                        .filter { it.isEnabled }
                        .forEach { filter ->
                            val plotData = data.plotData(i)
                            realtimeChart.addSeries((i + 1).toString() + "." + filter.shortName,
                                    plotData.map { it.x },
                                    plotData.map({ filter.step(it.y) }),
                                    null)
                        }
            }
        }
        return addSeries
    }

}

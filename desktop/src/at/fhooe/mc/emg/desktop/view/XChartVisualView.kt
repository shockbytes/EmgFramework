package at.fhooe.mc.emg.desktop.view

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.view.VisualView
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.XYSeries
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

            return if (!realtimeChart.seriesMap.isEmpty()) {
                if (realtimeChart.seriesMap.values.iterator().hasNext()) {
                    val first = realtimeChart.seriesMap.values.iterator().next()
                    first.yData.map { it }.toDoubleArray()
                } else {
                    DoubleArray(0)
                }

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

        realtimeChart = XYChartBuilder().width(800).height(600).theme(Styler.ChartTheme.GGPlot2).build()

        realtimeChart.styler.legendPosition = Styler.LegendPosition.OutsideE
        realtimeChart.styler.isPlotGridLinesVisible = false
        realtimeChart.styler.plotBackgroundColor = Color.WHITE
        realtimeChart.styler.seriesColors = arrayOf(Color.decode("#1976D2"), Color.decode("#607D8B"),
                Color.decode("#009688"), Color.decode("#4CAF50"), Color.decode("#F44336"),
                Color.decode("#FF9800"), Color.decode("#FFEB3B"), Color.decode("#FF6F00"))
        chartWrapper = XChartPanel(realtimeChart)
    }

    override fun update(data: EmgData, filters: List<Filter>) {
        Flowable.fromCallable {
             (0 until data.channelCount)
                    .flatMap { i ->
                        val plotData = data.plotData(i)
                        val x = plotData.map { it.x }
                        filters.filter { it.isEnabled }
                                .map { filter ->
                                    val name = (i + 1).toString() + "." + filter.shortName
                                    val y = plotData.map { filter.step(it.y) }
                                    VisualViewChannel(x, y, name)
                                }
                    }
        }.subscribeOn(Schedulers.computation()).subscribe({

            it.forEach {
                if (isChannelAvailable(it.name)) {
                    realtimeChart.updateXYSeries(it.name, it.x, it.y, null)
                } else {
                    realtimeChart.addSeries(it.name, it.x, it.y)
                            .xySeriesRenderStyle = XYSeries.XYSeriesRenderStyle.Line
                }
            }
            chartWrapper.repaint()
        }, { throwable -> println(throwable.message) })
    }

    override fun setYMaximum(maximum: Double) {
        realtimeChart.styler.yAxisMax = maximum
    }

    override fun reset() {
        initialize()
        chartWrapper.invalidate()
    }

    private fun isChannelAvailable(channel: String) = realtimeChart.seriesMap.containsKey(channel)

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

    @Deprecated("It's not efficient, but it works. Use the reactive way instead.")
    private fun updateDeprecated(data: EmgData, filters: List<Filter>) {
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

    data class VisualViewChannel(val x: List<Double>, val y: List<Double>, val name: String)

}

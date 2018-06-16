package at.fhooe.mc.emg.core.tool.salient

import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.*
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.apache.commons.math3.stat.regression.SimpleRegression

/**
 * Author:  Martin Macheiner
 * Date:    17.04.2018
 *
 * Detects salient points of a calculated metric during time
 *
 */
@EmgComponent(type = EmgComponentType.RELAY_SINK, displayTitle = "Salient point detector")
class SalientPointTool(override var toolView: SalientPointToolView? = null) : Tool, SalientPointToolViewCallback {

    override val name = "Salient Point Detection"

    private val points: MutableList<Double> = mutableListOf()

    private val confidenceGauge: ConfidenceGauge = WeightedErrorConfidenceGauge(0.4f, 0.6f)

    @JvmField
    @EmgComponentProperty("50.0", "Minimum confidence threshold")
    var confidenceThreshold: Double = 50.0

    @JvmField
    @EmgComponentProperty("0", "Minimum angle threshold")
    var salientAngleThreshold: Int = 0

    @JvmField
    @EmgComponentOutputPort(SalientPoint::class)
    var outputPort: PublishSubject<SalientPoint> = PublishSubject.create()

    val testData = listOf(31.14, 37.2, 38.13, 32.49, 30.94, 32.4, 31.68, 34.68, 31.61, 43.75, 115.22, 179.98)

    override fun updateParameter(confidence: Double, angle: Int) {
        this.confidenceThreshold = confidence
        this.salientAngleThreshold = angle
    }

    override fun start(toolable: Toolable?, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)

        /*
        Observable.interval(1, TimeUnit.SECONDS).subscribe {
            if (it < testData.size) {
                update(testData[it.toInt()])
            }
        } */
    }

    override fun onViewClosed() {
        points.clear()
    }

    @EmgComponentInputPort(Double::class)
    fun update(value: Double) {
        points.add(value)
        toolView?.updateChart(DoubleArray(points.size) { it.toDouble() }.toList(), points)
        calculateSalientPoint()
    }

    @EmgComponentStartablePoint("toolView", SalientPointToolView::class)
    override fun externalStart() {
        start(null, true)
    }

    private fun calculateSalientPoint() {

        // Only calculate if there are at least 5 points in the list
        if (points.size >= 6) {
            Single.fromCallable {
                val (bestFit, angle, confidence) = findBestFitWithAngle()
                val yVal = points[bestFit]
                SalientPoint(bestFit, yVal, confidence, angle)
            }.subscribeOn(Schedulers.computation()).subscribe(Consumer {
                toolView?.updateSalientPointInformation(it) // Update point info anyway
                if ((it.confidence <= confidenceThreshold) && (it.angle >= salientAngleThreshold)) {
                    toolView?.drawSalientPoint(it)
                    outputPort.onNext(it)
                } else {
                    toolView?.clearSalientPoint()
                }
            })
        }
    }

    /**
     * @return Triple destructured as (bestFit, angle, confidence)
     */
    private fun findBestFitWithAngle(): Triple<Int, Int, Double> {

        // Contains a pair for each traversed point with both RMSE error values
        val rmse: MutableList<ApproximatedLines> = mutableListOf()

        // Move the salient point through the array and calculate RMSE values for each
        for (i in 3 until points.size - 2) {

            val leftArray = points.subList(0, i)
            val rightArray = points.subList(i + 1, points.size)

            val leftRegression = SimpleRegression()
            leftRegression.addData(leftArray.mapIndexed { index, d -> doubleArrayOf(index.toDouble(), d) }.toTypedArray())
            val leftRmse = Math.pow(leftRegression.meanSquareError, 0.5)
            val leftSlope = leftRegression.slope

            val rightRegression = SimpleRegression()
            rightRegression.addData(rightArray.mapIndexed { index, d -> doubleArrayOf(index.toDouble(), d) }.toTypedArray())
            val rightRmse = Math.pow(rightRegression.meanSquareError, 0.5)
            val rightSlope = rightRegression.slope

            rmse.add(ApproximatedLines(i, leftRmse, rightRmse, leftSlope, rightSlope))
        }

        // Search best fit
        val bestFit = findBestFitByRmse(rmse)

        // Calculate angle
        val angle = calculateAngle(bestFit)

        return Triple(bestFit.salientPointIdx, angle, bestFit.confidence)
    }

    private fun calculateAngle(l: ApproximatedLines): Int {
        val tanAngle = (l.leftSlope - l.rightSlope) / (1 + (l.leftSlope * l.rightSlope))
        return Math.toDegrees(Math.atan(tanAngle)).toInt()
    }

    private fun findBestFitByRmse(lines: List<ApproximatedLines>): ApproximatedLines {
        return lines.map { l ->
            val confidence = confidenceGauge.calculateConfidence(l.salientPointIdx, Pair(l.leftRmse, l.rightRmse))
            l.confidence = confidence
            l
        }.sortedBy {
            it.confidence
        }.first()
    }

    data class ApproximatedLines(val salientPointIdx: Int,
                                 val leftRmse: Double, val rightRmse: Double,
                                 val leftSlope: Double, val rightSlope: Double,
                                 var confidence: Double = 0.0)

}

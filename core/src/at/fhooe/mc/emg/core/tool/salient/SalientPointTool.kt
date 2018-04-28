package at.fhooe.mc.emg.core.tool.salient

import at.fhooe.mc.emg.core.Toolable
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.util.MathUtility
import at.fhooe.mc.emg.core.util.PointD
import at.fhooe.mc.emg.core.util.rmse
import at.fhooe.mc.emg.core.util.round
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Author:  Martin Macheiner
 * Date:    17.04.2018
 *
 * Detects salient points of a calculated metric during time
 *
 */
@EmgComponent(EmgComponentType.RELAY_SINK)
class SalientPointTool(override var toolView: SalientPointToolView? = null) : Tool, SalientPointToolViewCallback {

    override val name = "Salient Point Detection"

    private val points: MutableList<Double> = mutableListOf()

    private val confidenceGauge: ConfidenceGauge = WeightedErrorConfidenceGauge(0.4f, 0.6f)

    @JvmField
    @EmgComponentProperty("50.0")
    var confidenceThreshold: Double = 50.0

    @JvmField
    @EmgComponentProperty("0")
    var salientAngleThreshold: Int = 0

    @JvmField
    @EmgComponentOutputPort(SalientPoint::class)
    var outputPort: PublishSubject<SalientPoint> = PublishSubject.create()

    override fun updateParameter(confidence: Double, angle: Int) {
        this.confidenceThreshold = confidence
        this.salientAngleThreshold = angle
    }

    override fun start(toolable: Toolable?, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)
    }

    override fun onViewClosed() {
        points.clear()
    }

    @EmgComponentInputPort(Double::class)
    override fun update(value: Double) {
        println("Update SalientPointTool: $value")

        points.add(value)
        toolView?.updateChart(DoubleArray(points.size) { it.toDouble() }.toList(), points)
        calculateSalientPoint()
    }

    private fun calculateSalientPoint() {

        // Only calculate if there are at least 5 points in the list
        if (points.size >= 5) {
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
        val rmse: MutableList<Pair<Int, Pair<Double, Double>>> = mutableListOf()

        // Move the salient point through the array and calculate RMSE values for each
        for (i in 1 until points.size - 1) {
            val (first, second) = divideIntoLinesWithApproximation(i, points.size)
            rmse.add(Pair(i, calculateRmseValues(first.first, first.second, second.first, second.second)))
        }

        // Search best fit
        val (bestFit, confidence) = findBestFitByRmse(rmse)

        // Calculate angle
        val angle = calculateAngle(bestFit, points.size)

        return Triple(bestFit, angle, confidence)
    }

    private fun calculateRmseValues(first: List<Double>, firstApprox: List<Double>,
                                    second: List<Double>, secondApprox: List<Double>): Pair<Double, Double> {
        return Pair(first.rmse(firstApprox), second.rmse(secondApprox))
    }

    private fun calculateAngle(idx: Int, size: Int): Int {

        val div = divideIntoLinesWithApproximation(idx, size)
        val first = div.first.second
        val second = div.second.second

        return 180 - MathUtility.angleBetween2Lines(PointD(0.0, first.first()), PointD(idx.toDouble(), first.last()),
                PointD(idx.toDouble(), second.first()), PointD(size.toDouble(), second.last())).round()
    }

    private fun findBestFitByRmse(rmse: MutableList<Pair<Int, Pair<Double, Double>>>): Pair<Int, Double> {
        return rmse.map { (idx, errors) ->
            val confidence = confidenceGauge.calculateConfidence(idx, errors)
            Pair(idx, confidence)
        }.sortedBy { (_, confidence) ->
            confidence
        }.first()
    }

    private fun getApproximatedLine(first: Double, last: Double, size: Int): List<Double> {
        val step = (last - first) / size
        return DoubleArray(size) { it * step + first }.toList()
    }

    private fun divideIntoLinesWithApproximation(i: Int, size: Int)
            : Pair<Pair<List<Double>, List<Double>>, Pair<List<Double>, List<Double>>> {

        val first = points.subList(0, i + 1)
        val firstApprox = getApproximatedLine(first.first(), first.last(), first.size)
        val second = points.subList(i, size)
        val secondApprox = getApproximatedLine(second.first(), second.last(), second.size)
        return Pair(Pair(first, firstApprox), Pair(second, secondApprox))
    }

}

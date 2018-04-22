package at.fhooe.mc.emg.core.tool.salient

import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.designer.*
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

    @EmgComponentProperty
    var confidenceThreshold: Double = 0.5
    @EmgComponentProperty
    var salientAngleThreshold: Int = 15

    @EmgComponentOutputPort(SalientPoint::class)
    var outputPort: PublishSubject<SalientPoint> = PublishSubject.create()

    override fun updateParameter(confidence: Double, angle: Int) {
        this.confidenceThreshold = confidence
        this.salientAngleThreshold = angle
    }

    override fun start(presenter: EmgPresenter, showViewImmediate: Boolean) {
        toolView?.setup(this, showViewImmediate)
    }

    override fun onViewClosed() {
        points.clear()
    }

    @EmgComponentInputPort(Double::class)
    override fun update(d: Double) {
        points.add(d)
        toolView?.updateChart(DoubleArray(points.size) { it.toDouble() }.toList(), points)
        calculateSalientPoint()
    }

    private fun calculateSalientPoint() {
        Single.fromCallable {
            val (bestFit, angle, errors) = findBestFitWithAngle()
            val yVal = points[bestFit]
            val confidence = calculateConfidence(bestFit, errors)
            SalientPoint(bestFit, yVal, confidence, angle)
        }.subscribeOn(Schedulers.computation()).subscribe(Consumer {
            if ((it.confidence > confidenceThreshold) && (it.angle > salientAngleThreshold)) {
                toolView?.drawSalientPoint(it)
                outputPort.onNext(it)
            } else {
                toolView?.clearSalientPoint()
            }
        })
    }

    private fun findBestFitWithAngle(): Triple<Int, Int, Pair<Double, Double>> {
        return Triple(0, 0, Pair(0.0, 0.0)) // TODO
    }

    private fun calculateConfidence(idx: Int, errors: Pair<Double, Double>): Double {
        return 0.0 // TODO
    }

}

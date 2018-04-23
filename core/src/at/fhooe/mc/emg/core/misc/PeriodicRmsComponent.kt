package at.fhooe.mc.emg.core.misc

import at.fhooe.mc.emg.core.util.rms
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import io.reactivex.subjects.PublishSubject

/**
 * Author:  Martin Macheiner
 * Date:    23.04.2018
 *
 * This class consumes continously EMG data and calculates
 * the RMS value of the signal in a fixed interval
 *
 */
@EmgComponent(type = EmgComponentType.RELAY_SINK)
class PeriodicRmsComponent {

    @EmgComponentProperty
    var capacity = 2000

    @EmgComponentOutputPort(Double::class)
    private var outputPort: PublishSubject<Double> = PublishSubject.create()

    private val buffer: MutableList<Double> = ArrayList(capacity)

    @EmgComponentInputPort(Double::class)
    fun update(x: Double) {

        buffer.add(x)
        if (buffer.size >= capacity) {
            val rms = buffer.toDoubleArray().rms()
            outputPort.onNext(rms)
            buffer.clear()
        }
    }

}
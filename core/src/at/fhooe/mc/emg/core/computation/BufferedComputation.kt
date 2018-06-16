package at.fhooe.mc.emg.core.computation

import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

abstract class BufferedComputation<I, O> {


    var capacity: Int = 512
        set(value) {
            field = value
            // Capacity change updates buffer as well
            buffer = ArrayList(capacity)
        }

    var outputPort: PublishSubject<O> = PublishSubject.create()

    private var buffer: MutableList<I> = ArrayList(capacity)

    abstract fun computeValue(data: List<I>): Single<O>

    fun update(i: I) {

        buffer.add(i)
        if (buffer.size >= capacity) {
            computeValue(buffer.takeLast(capacity))
                    .subscribeOn(Schedulers.computation())
                    .subscribe(Consumer {
                        outputPort.onNext(it)
                    })
            reset()
        }
    }

    open fun reset() {
        buffer.clear()
    }

}
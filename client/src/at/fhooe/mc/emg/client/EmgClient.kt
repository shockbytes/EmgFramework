package at.fhooe.mc.emg.client

import at.fhooe.mc.emg.messaging.EmgMessaging
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

abstract class EmgClient {

    abstract val protocolVersion: EmgMessaging.ProtocolVersion

    private lateinit var timerObservable: Observable<Long>
    private var timerDisposable: Disposable? = null

    private var period: Long = 1000

    init {
        setup()
    }

    private fun setup() {

        timerObservable = Observable.interval(period, TimeUnit.MILLISECONDS)
        setupTransmission()
    }

    fun start() {
        timerDisposable = timerObservable.subscribe {
            send(EmgMessaging.buildClientMessage(provideData(), System.currentTimeMillis(), protocolVersion))
        }
    }

    fun stop() {
        timerDisposable?.dispose()
        tearDown()
    }

    abstract fun setupTransmission()

    abstract fun send(data: String)

    abstract fun provideData(): List<Double>

    abstract fun tearDown()

}
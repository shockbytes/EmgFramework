package at.fhooe.mc.emg.client

import at.fhooe.mc.emg.messaging.EmgMessaging
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

abstract class EmgClient {

    abstract val protocolVersion: EmgMessaging.ProtocolVersion

    private var timerDisposable: Disposable? = null

    private var period: Long = 10

    init {
        setup()
    }

    private fun setup() {
        setupTransmission()
    }

    private fun updateDelay(delayMillis: Long) {
        period = delayMillis

        timerDisposable?.dispose()
        start()
    }

    fun start() {
        timerDisposable = Observable.interval(period, TimeUnit.MILLISECONDS).subscribe {
            send(EmgMessaging.buildClientMessage(provideData(), System.currentTimeMillis(), protocolVersion))
        }
    }

    fun stop() {
        timerDisposable?.dispose()
        tearDown()
    }

    fun handleMessage(data: String) {

        when (EmgMessaging.parseServerMessage(data)) {

            EmgMessaging.ServerMessageType.DELAY -> updateDelay(EmgMessaging.parseFrequencyMessage(data))
            EmgMessaging.ServerMessageType.NA -> println("Cannot identify server message type!")
        }
    }

    abstract fun setupTransmission()

    abstract fun send(data: String)

    abstract fun provideData(): List<Double>

    abstract fun tearDown()

}
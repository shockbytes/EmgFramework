package at.fhooe.mc.emg.client

import at.fhooe.mc.emg.messaging.EmgMessaging
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class EmgClient {

    abstract val protocolVersion: EmgMessaging.ProtocolVersion

    private var timerDisposable: Disposable? = null

    protected var period: Long = 10

    /**
     * Sets up all the necessary stuff for auto connection. If a connection is acquired it automatically
     * calls the #startTransmission() method from the inside
     */
    fun start() {
        setupTransmission()
    }

    private fun updateDelay(delayMillis: Long) {
        period = delayMillis

        timerDisposable?.dispose()
        startTransmission()
    }

    /**
     * Starts the transmission. This means, that the client must already be connected to the sink! Therefore
     * this method is private and can just be called inside when the connection is established
     */
    private fun startTransmission() {
        timerDisposable = Observable.interval(period, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe {
            send(EmgMessaging.buildClientMessage(provideData(), System.currentTimeMillis(), protocolVersion))
        }
    }

    /**
     * Tears down all the connection specific stuff and kills the transmission, if there was one already established
     */
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
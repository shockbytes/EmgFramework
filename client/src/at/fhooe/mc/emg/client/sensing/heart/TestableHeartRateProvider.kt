package at.fhooe.mc.emg.client.sensing.heart

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class TestableHeartRateProvider : HeartRateProvider {

    private var heartRateCallback: ((Int) -> Unit)? = null
    private var stateCallback: ((HeartRateProvider.ConnectionState) -> Unit)? = null

    private var hrDisposable: Disposable? = null

    private val random = Random()

    override fun subscribeForHeartRateUpdates(callback: (Int) -> Unit) {
        this.heartRateCallback = callback
    }

    override fun subscribeToConnectionChanges(callback: (HeartRateProvider.ConnectionState) -> Unit) {
        this.stateCallback = callback
    }

    override fun unsubscribeToConnectionChanges() {
        stateCallback = null
    }

    override fun start() {
        stateCallback?.invoke(HeartRateProvider.ConnectionState.CONNECTED)
        hrDisposable = Observable.interval(5, TimeUnit.SECONDS).subscribeOn(Schedulers.computation()).subscribe {
            val hr = 50 + random.nextInt(50)
            heartRateCallback?.invoke(hr)
        }
    }

    override fun stop() {
        hrDisposable?.dispose()
        stateCallback?.invoke(HeartRateProvider.ConnectionState.DISCONNECTED)
    }
}
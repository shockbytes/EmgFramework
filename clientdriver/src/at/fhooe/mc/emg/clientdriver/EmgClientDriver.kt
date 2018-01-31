package at.fhooe.mc.emg.clientdriver

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.clientdriver.model.EmgPoint
import at.fhooe.mc.emg.messaging.EmgMessaging
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
abstract class EmgClientDriver(var configView: EmgClientDriverConfigView?) {

    open var samplingFrequency: Double = 100.toDouble()
        set(fs) {
            if (fs > 0) {
                field = fs
                sendSamplingFrequencyToClient()
            }
        }

    var currentDataPointer: Int = 0
        protected set

    val rawCallbackSubject: PublishSubject<String> = PublishSubject.create()

    val channeledCallbackSubject: PublishSubject<EmgData> = PublishSubject.create()

    var data: EmgData
        protected set

    val hasConfigView: Boolean
        get() = configView != null

    // ---------------------------------------------------------------

    abstract val name: String

    abstract val shortName: String

    abstract val isDataStorageEnabled: Boolean

    abstract val category: ClientCategory

    abstract val protocolVersion: EmgMessaging.ProtocolVersion

    abstract fun connect(successHandler: Action, errorHandler: Consumer<Throwable>)

    abstract fun disconnect()

    abstract fun sendSamplingFrequencyToClient()

    // ---------------------------------------------------------------
    init {
        data = EmgData(channelWindowWidth)
    }

    fun clearData() {
        data.reset()
        currentDataPointer = 0
    }

    fun processMessage(msg: String) {

        // Always increment x counter value
        currentDataPointer++

        val channels = EmgMessaging.parseClientMessage(msg, protocolVersion)
        channels?.forEachIndexed { idx, value ->
            data.updateChannel(idx, EmgPoint(currentDataPointer.toDouble(), value))
        }

        rawCallbackSubject.onNext(msg)
        channeledCallbackSubject.onNext(data)
    }

    companion object {
        const val channelWindowWidth = 512
    }

}

package at.fhooe.mc.emg.client

import at.fhooe.mc.emg.messaging.EmgMessaging
import io.reactivex.subjects.PublishSubject

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
abstract class EmgClientDriver(val configView: EmgClientDriverConfigView?) {

    open var samplingFrequency: Double = 100.toDouble()
        set(fs) {
            field = fs
            sendSamplingFrequencyToClient()
        }

    var currentDataPointer: Int = 0
        protected set

    val rawCallbackSubject: PublishSubject<String> = PublishSubject.create()

    val channeledCallbackSubject: PublishSubject<ChannelData> = PublishSubject.create()

    var channelData: ChannelData
        protected set

    val hasConfigView: Boolean
        get() = configView != null

    // ---------------------------------------------------------------

    abstract val name: String

    abstract val shortName: String

    abstract val isDataStorageEnabled: Boolean

    abstract val category: ClientCategory

    abstract val protocolVersion: EmgMessaging.ProtocolVersion

    @Throws(Exception::class)
    abstract fun connect()

    abstract fun disconnect()

    abstract fun sendSamplingFrequencyToClient()

    // ---------------------------------------------------------------
    init {
        channelData = ChannelData(channelWindowWidth)
    }

    fun processMessage(msg: String) {

        // Always increment x counter value
        currentDataPointer++

        val channels = EmgMessaging.parseMessage(msg, protocolVersion)
        channels?.forEachIndexed { idx, value ->
            channelData.updateXYSeries(idx, currentDataPointer.toDouble(), value)
        }

        rawCallbackSubject.onNext(msg)
        channeledCallbackSubject.onNext(channelData)
    }

    companion object {
        const val channelWindowWidth = 512
    }

}

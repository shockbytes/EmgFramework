package at.fhooe.mc.emg.client

import EmgMessaging
import io.reactivex.subjects.PublishSubject

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
abstract class EmgClient {

    open var samplingFrequency: Double = 0.toDouble()
        set(fs) {
            field = fs
            sendSamplingFrequencyToClient()
        }
    var currentDataPointer: Int = 0
        protected set

    val rawCallbackSubject: PublishSubject<String> = PublishSubject.create()

    val channeledCallbackSubject: PublishSubject<ChannelData> = PublishSubject.create()

    lateinit var channelData: ChannelData
        protected set

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

    fun processMessage(msg: String) {

        // Always increment x counter value
        currentDataPointer++

        val channels = EmgMessaging.parseMessage(msg, protocolVersion)
        channels?.forEachIndexed{ idx, value ->
            channelData.updateXYSeries(idx, currentDataPointer.toDouble(), value)
        }

        rawCallbackSubject.onNext(msg)
        channeledCallbackSubject.onNext(channelData)
    }

}

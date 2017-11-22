package at.fhooe.mc.emg.client

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
abstract class EmgClient {

    var dataRate: Int = 0
    open var samplingFrequency: Double = 0.toDouble()

    lateinit var channelData: ChannelData
        protected set
    protected var callback: ClientDataCallback? = null

    abstract val name: String

    abstract val shortName: String

    abstract val currentDataPointer: Int

    abstract val isDataStorageEnabled: Boolean

    abstract val category: ClientCategory

    @Throws(Exception::class)
    abstract fun connect(callback: ClientDataCallback)

    abstract fun disconnect()

}

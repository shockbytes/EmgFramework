package at.fhooe.mc.emg.clientdriver

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.clientdriver.model.EmgPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.messaging.MessageInterpreter
import at.fhooe.mc.emg.messaging.model.EmgPacket
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 *
 * EmgClientDriver are the driver software which are necessary in order to communicate with the different
 * EmgClients. They are kind of symmetric in the capabilities they offer and they are loosely coupled together
 * via the messaging module. Usually client and driver has to utilize the same MessageInterpreter implementation with the
 * same protocol version. As the protocol versioning is backwards compatible it is no problem if the client sends in a
 * V3 format and the driver is only capable to handle V1 messages (but it wouldn't work the other way around). This
 * class comes with a lot of abstract members, but most of them are used for UI and other framework functionality.
 * The driver for each client is like a plugin, which is applied not just in one place in the framework. It is an
 * essential part of the whole functionality.
 */
abstract class EmgClientDriver(var configView: EmgClientDriverConfigView?) {

    /**
     * Author:  Martin Macheiner
     * Date:    22.11.2017
     *
     * Enum class for identifying each client category of the driver software.
     */
    enum class ClientCategory {
        SERIAL, SIMULATION, NETWORK, BLUETOOTH
    }


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

    @JvmField
    @EmgComponentOutputPort(EmgData::class)
    val channeledCallbackSubject: PublishSubject<EmgData> = PublishSubject.create()

    var data: EmgData
        protected set

    val hasConfigView: Boolean
        get() = configView != null

    // ---------------------------------------------------------------

    /**
     * Full name of the driver.
     */
    abstract val name: String

    /**
     * Short name of the driver.
     */
    abstract val shortName: String

    /**
     * Indicates if the driver allows data storage or not.
     */
    abstract val isDataStorageEnabled: Boolean

    /**
     * Indicates the ClientCategory corresponding to the Connection type
     */
    abstract val category: ClientCategory

    /**
     * Message parser implementation, which takes care of message sending and receiving for the driver.
     *
     * NOTE: Each subtype of EmgClientDriver must agree to utilize the EmgPacket as the common data type. On the one
     * hand this revokes the advantages of a generic implementation, but on the other hand it eases the development pain.
     */
    abstract var msgInterpreter: MessageInterpreter<EmgPacket>

    /**
     * Tries to connect to the client. This action can take up to some seconds, depending on the ClientCategory.
     *
     * @param successHandler Action invoked when the driver is successfully connected to the client.
     * @param errorHandler Action invoked when the driver catches and exception during injection.
     */
    abstract fun connect(successHandler: Action, errorHandler: Consumer<Throwable>)

    /**
     * Disconnects from the connected client.
     */
    abstract fun disconnect()

    /**
     * Sends the sampling frequency down to the client in form of a ServerMessage.
     */
    abstract fun sendSamplingFrequencyToClient()

    // ---------------------------------------------------------------
    init {
        data = EmgData(channelWindowWidth)
    }
    /**
     * Resets all stored data
     */
    fun clearData() {
        data.reset()
        currentDataPointer = 0
    }

    /**
     * Incoming messages are handling inside this method. The message parser is used to parse a valid EmgPacket from
     * the given string. If the packet is valid, then the data is updated and propagated to the presenter. The raw
     * callback is always propagated to the presenter (for debugging purposes).
     *
     * @param msg String representation of the incoming message.
     */
    protected fun processMessage(msg: String) {

        // Always increment x counter value
        currentDataPointer++

        val packet = msgInterpreter.parseClientMessage(msg)
        packet?.let {

            // Update the data object
            packet.channels.forEachIndexed { idx, value ->
                data.updateChannel(idx, EmgPoint(currentDataPointer.toDouble(), value))
            }
            data.updateHeartRate(it.heartRate)

            // Only call this callback if valid data is received
            channeledCallbackSubject.onNext(data)
        }

        rawCallbackSubject.onNext(msg)
    }

    companion object {
        const val channelWindowWidth = 512
    }

}

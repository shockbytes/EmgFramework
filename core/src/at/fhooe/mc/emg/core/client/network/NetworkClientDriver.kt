package at.fhooe.mc.emg.core.client.network

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentEntryPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentExitPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import at.fhooe.mc.emg.messaging.EmgMessageInterpreter
import at.fhooe.mc.emg.messaging.MessageInterpreter
import at.fhooe.mc.emg.messaging.model.EmgPacket
import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import io.reactivex.Completable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

/**
 * Author:  Martin Macheiner
 * Date:    20.11.2017
 */
@EmgComponent(type = EmgComponentType.DEVICE, displayTitle = "Network device")
class NetworkClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override var msgInterpreter: MessageInterpreter<EmgPacket> = EmgMessageInterpreter(MessageInterpreter.ProtocolVersion.V3)

    override val category: ClientCategory = ClientCategory.NETWORK

    override val name: String
        get() = "Network device @ $ip"

    override val shortName: String = "Network (UDP)"

    override val isDataStorageEnabled: Boolean = true

    @JvmField
    @EmgComponentProperty("localhost", "Ip address of remote device")
    var ip: String = "192.168.8.102"

    @JvmField
    @EmgComponentProperty("5673", "Port of remote device")
    var port: Int = 5673

    private var client: Client? = null

    @EmgComponentEntryPoint
    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {

        Completable.fromAction {

            client = Client()
            client?.kryo?.register(String::class.java)
            client?.start()
            client?.connect(5000, ip, 54557, port)

            // Initialize by sending sampling frequency
            sendSamplingFrequencyToClient()

            client?.addListener(object: Listener() {

                override fun received(connection: Connection?, data: Any?) {
                    super.received(connection, data)

                    if (data != null && data is String) {
                        processMessage(data)
                    }
                }
            })

        }.subscribeOn(Schedulers.io()).subscribe(successHandler, errorHandler)
    }

    @EmgComponentExitPoint
    override fun disconnect() {
        Completable.fromAction {

            // Send disconnect message before closing socket
            sendMessage("disconnect")
            client?.dispose()
            client = null

        }.subscribeOn(Schedulers.io()).subscribe()
    }

    override fun sendSamplingFrequencyToClient() {
        Completable.fromAction {
            sendMessage(msgInterpreter.buildFrequencyMessage(samplingFrequency))
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    fun setSocketOptions(ip: String, port: Int) {
        this.ip = ip
        this.port = port
    }

    private fun sendMessage(msg: String) {
        client?.sendUDP(msg)
    }

}
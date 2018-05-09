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
import io.reactivex.Completable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Author:  Martin Macheiner
 * Date:    20.11.2017
 */
@EmgComponent(type = EmgComponentType.DEVICE, displayTitle = "Network device")
class NetworkClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override var msgInterpreter: MessageInterpreter<EmgPacket> = EmgMessageInterpreter(MessageInterpreter.ProtocolVersion.V1)

    override val category: ClientCategory = ClientCategory.NETWORK

    override val name: String
        get() = "Network device @ $ip"

    override val shortName: String = "Network (UDP)"

    override val isDataStorageEnabled: Boolean = true

    @JvmField
    @EmgComponentProperty("localhost", "Ip address of remote device")
    var ip: String = "localhost"

    @JvmField
    @EmgComponentProperty("5673", "Port of remote device")
    var port: Int = 5673

    private val buffer = ByteArray(64)
    private var datagramSocket: DatagramSocket? = null

    private var isRunning = false

    @EmgComponentEntryPoint
    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {
        Completable.fromAction {

            isRunning = true
            datagramSocket = DatagramSocket()
            datagramSocket?.connect(InetAddress.getByName(ip), port)

            // Initialize by sending sampling frequency
            sendSamplingFrequencyToClient()

            // Everything should be working here
            successHandler.run()

            while (isRunning) {
                val packet = DatagramPacket(buffer, buffer.size)
                datagramSocket?.receive(packet)
                processMessage(String(packet.data))
            }

        }.subscribeOn(Schedulers.io()).subscribe(successHandler, errorHandler)
    }

    @EmgComponentExitPoint
    override fun disconnect() {
        Completable.fromAction {

            // Send disconnect message before closing socket
            sendMessage("disconnect")

            isRunning = false
            datagramSocket?.disconnect()
            datagramSocket?.close()

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
        val bytes = msg.toByteArray()
        val packet = DatagramPacket(bytes, bytes.size)
        datagramSocket?.send(packet)
    }

}
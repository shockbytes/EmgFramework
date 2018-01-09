package at.fhooe.mc.emg.core.client.network

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.messaging.EmgMessaging
import io.reactivex.Completable
import io.reactivex.Observable
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
class NetworkClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override val protocolVersion: EmgMessaging.ProtocolVersion = EmgMessaging.ProtocolVersion.V1

    override val category: ClientCategory = ClientCategory.NETWORK

    override val name: String
        get() = "Network device @ " + ip

    override val shortName: String = "Network"

    override val isDataStorageEnabled: Boolean = true

    var ip: String = "localhost"
    var port: Int = 5673

    private val buffer = ByteArray(64)
    private var datagramSocket: DatagramSocket? = null

    private var isRunning = false

    override fun connect(errorHandler: Consumer<Throwable>) {
        Completable.fromAction {

            isRunning = true
            datagramSocket = DatagramSocket()
            datagramSocket?.connect(InetAddress.getByName(ip), port)

            // Initialize by sending sampling frequency
            sendSamplingFrequencyToClient()

            while (isRunning) {
                val packet = DatagramPacket(buffer, buffer.size)
                datagramSocket?.receive(packet)
                processMessage(String(packet.data))
            }

        }.subscribeOn(Schedulers.io()).subscribe(Action{}, errorHandler)
    }

    override fun disconnect() {
        Observable.defer {
            // Send disconnect message before closing socket
            sendMessage("disconnect")

            isRunning = false
            datagramSocket?.disconnect()
            datagramSocket?.close()

            Observable.empty<Boolean>()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    override fun sendSamplingFrequencyToClient() {
        Observable.defer {
            sendMessage(EmgMessaging.buildFrequencyMessage(samplingFrequency))
            Observable.empty<Boolean>()
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
package at.fhooe.mc.emg.client.network

import EmgMessaging
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.EmgClient
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Author:  Martin Macheiner
 * Date:    20.11.2017
 */
class NetworkClient : EmgClient() {

    override val protocolVersion: EmgMessaging.ProtocolVersion = EmgMessaging.ProtocolVersion.V1

    override val category: ClientCategory = ClientCategory.NETWORK

    override val name: String
        get() = "Network device @ " + ip

    override val shortName: String = "Network"

    override val isDataStorageEnabled: Boolean = true

    private var ip: String = "localhost"
    private var port: Int = 5673
    private val buffer = ByteArray(64)
    private var datagramSocket: DatagramSocket? = null

    private var isRunning = false

    override fun connect() {

        isRunning = true
        datagramSocket = DatagramSocket()
        datagramSocket?.connect(InetAddress.getByName(ip), port)

        // Initialize by sending sampling frequency
        sendSamplingFrequencyToClient()

        Observable.defer{

            while (isRunning) {
                val packet = DatagramPacket(buffer, buffer.size)
                datagramSocket?.receive(packet)
                processMessage(String(packet.data))
            }

            Observable.empty<Boolean>()
        }.subscribeOn(Schedulers.io()).subscribe()
    }

    override fun disconnect() {

        // Send disconnect message before closing socket
        sendMessage("disconnect")

        isRunning = false
        datagramSocket?.disconnect()
        datagramSocket?.close()
    }

    override fun sendSamplingFrequencyToClient() {
        sendMessage(EmgMessaging.buildFrequencyMessage(samplingFrequency))
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
package at.fhooe.mc.emg.client.network

import EmgMessaging
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.EmgClient

/**
 * Author:  Martin Macheiner
 * Date:    20.11.2017
 */
class NetworkClient : EmgClient() {

    override val protocolVersion: EmgMessaging.ProtocolVersion = EmgMessaging.ProtocolVersion.V1

    override val category: ClientCategory = ClientCategory.NETWORK

    override val name: String
        get() = if (ip == null) shortName else "Network device @ " + ip

    override val shortName: String
        get() = "Network"

    override val isDataStorageEnabled: Boolean
        get() = true

    private var ip: String? = null

    override fun connect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendSamplingFrequencyToClient() {
        // TODO Send to connected client
    }

}
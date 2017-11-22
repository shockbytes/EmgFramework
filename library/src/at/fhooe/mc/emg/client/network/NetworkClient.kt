package at.fhooe.mc.emg.client.network

import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.ClientDataCallback
import at.fhooe.mc.emg.client.EmgClient

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */
class NetworkClient : EmgClient() {

    override val category: ClientCategory = ClientCategory.NETWORK

    override val name: String
        get() = if (ip == null) shortName else "Network device @ " + ip!!

    override val shortName: String
        get() = "Network"

    override val currentDataPointer: Int
        get() = 0 // TODO

    override val isDataStorageEnabled: Boolean
        get() = true

    private var ip: String? = null

    override fun connect(callback: ClientDataCallback) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun disconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
package at.fhooe.mc.emg.client

/**
 * Author:  Martin Macheiner
 * Date:    03.07.2017
 */
interface ClientDataCallback {

    fun onChanneledDataAvailable(channelData: ChannelData)

    fun onRawDataAvailable(line: String)
}
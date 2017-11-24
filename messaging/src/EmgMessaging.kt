import at.fhooe.mc.emg.client.ChannelData

/**
 * Author:  Martin Macheiner
 * Date:    24.11.2017
 *
 * This class contains the decoding and encoding logic of the emg protocol, which is used to transfer emg data
 * between the clients and the server application.
 *
 * This ensures that the data stays consistent and independent no matter which transport medium is responsible for
 * data transfer (Medium can be a serial connection, http, tcp, files, bluetooth, etc...).
 *
 */

object EmgMessaging {

    /**
     * @param timestamp Timestamp must be provided, because this module can be built against multiple
     * architectures (with Kotlin Native), where it cannot be assured that System.currentTimeMillis()
     * will work on every platform
     * @param emgData List of emg channels, which should be transferred to the 'server' application
     *
     * @return String representation of the different channels which are transferred
     */
    fun buildMessage(timestamp: Long, vararg emgData: Double): String {

        var msg = timestamp.toString().plus(":")
        for (i in emgData.indices) {
            msg = msg.plus(emgData[i])
            if (i < emgData.size - 1) msg = msg.plus(",")
        }
        return msg
    }

    /**
     * @param The message which contains the EMG data, this message packet SHOULD be built
     * with the {@link #buildMessage(Long, Double...)} method, to ensure, that the data is consistent
     *
     * @return ChannelData with different Emg channels
     *
     */
    fun parseMessage(msg: String) : ChannelData? {
        // TODO Parse message
        return null
    }


}
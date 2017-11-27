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

    private const val paramDelimeter = ":"
    private const val channelDelimeter = ","

    enum class ProtocolVersion {
        V1, V2
    }

    /**
     * @param timestamp Timestamp must be provided, because this module can be built against multiple
     * architectures (with Kotlin Native), where it cannot be assured that System.currentTimeMillis()
     * will work on every platform
     * @param emgData List of emg channels, which should be transferred to the 'server' application
     * @param protocolVersion version of the EMG protocol, this value should be provided by the connected client
     *
     * @return String representation of the different channels which are transferred
     */
    fun buildClientMessage(emgData: List<Double>, timestamp: Long, protocolVersion: ProtocolVersion): String {

        return when (protocolVersion) {

            EmgMessaging.ProtocolVersion.V1 -> buildV1(emgData)
            EmgMessaging.ProtocolVersion.V2 -> buildV2(emgData, timestamp)
        }
    }

    private fun buildV1(emgData: List<Double>): String {

        var msg = ""
        for (i in emgData.indices) {
            msg = msg.plus(emgData[i])
            if (i < emgData.size - 1) msg = msg.plus(channelDelimeter)
        }
        return msg
    }

    private fun buildV2(emgData: List<Double>, timestamp: Long): String {

        val msg = timestamp.toString().plus(paramDelimeter)
        return msg.plus(buildV1(emgData))
    }

    // ----------------------------------------------------------------------------------------------------

    fun buildFrequencyMessage(fs: Double): String {
        val millis = (1.0 / fs * 1000).toInt()
        return "delay=" + millis + "\r\n"
    }

    // ----------------------------------------------------------------------------------------------------

    /**
     * @param msg The message which contains the EMG data, this message packet SHOULD be built
     * with the {@link #buildClientMessage(List<Double>, Long, ProtocolVersion)} method, to ensure,
     * that the data is consistent
     * @param protocolVersion protocol version provided by the client (server could also provide it, but so it will
     * fall back to the supported version of the client
     *
     * @return List of all available channels
     */
    fun parseMessage(msg: String, protocolVersion: ProtocolVersion): List<Double>? {

        return when (protocolVersion) {

            EmgMessaging.ProtocolVersion.V1 -> parseV1(msg)
            EmgMessaging.ProtocolVersion.V2 -> parseV2(msg)
        }
    }

    private fun parseV1(msg: String): List<Double>? {
        return if (msg.contains(",")) { // > than 1 channel

            val values = msg.split(channelDelimeter.toRegex())
                    .dropLastWhile { it.trim().isEmpty() }.toTypedArray()
                    .map { v -> v.trim().toDouble() }

            // Do not process damaged packages
            return if (values.isNotEmpty()) values else null

        } else {
            if (Character.isDigit(msg[0])) arrayListOf(msg.toDouble()) else arrayListOf(Double.MIN_VALUE)
        }
    }

    private fun parseV2(msg: String): List<Double>? {

        val params = msg.split(paramDelimeter.toRegex()).dropLastWhile { it.trim().isEmpty() }
        // TODO Somehow incorporate timestamps and break compat with #parseMessage()
        // val timestamps = params[0].map { s -> s.toDouble() }
        return if (params.size > 1) parseV1(params[1]) else null
    }



}
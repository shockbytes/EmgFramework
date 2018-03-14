package at.fhooe.mc.emg.messaging

import at.fhooe.mc.emg.messaging.model.ServerMessage

/**
 * @author  Martin Macheiner
 * Date:    24.01.2018
 * <p>
 * Abstract interface for message handling both on client and clientdriver side. This decoupling enables both
 * sides to easily switch the implementation of the protocol. The generic implementation provides an even further
 * method of customization of the behavior, as it allows to to replace the data packet class.
 * The message parser implementation and the {@link ProtocolVersion} must match on client and driver side.
 *
 */
interface MessageParser<T> {

    /**
     * Different clients can provide a different degree of information. In order to make them somehow
     * backwards compatible it is necessary to introduce some kind of versioning. The {@link MessageParser} provides
     * there the enum {@link ProtocolVersion}, which exactly defines the data format of the transmission.
     * <p>
     * NOTE: When talking about a usual EMG transmission, then the data format below is the best choice, but it is
     * legitimate for every implementation of MessageParser to define their own versioning format.
     */
    enum class ProtocolVersion {
        // Easiest protocol, just multiple channel values divided by the channel delimiter.
        // Example: 1,2,3
        V1,
        // Incorporates timestamp. This version should tackle the problem of latencies. Now every packet has its stamp.
        // Example: 687801928:1,2,3
        V2,
        // Adds the possibility to measure and send heart rate as well. This is especially useful when a Conconi test
        // should be conducted. Because of the data overhead it is not recommended to use this, when it is not necessary.
        // Example: 687801928:1,2,3:65
        V3
    }

    /**
     * Utilized protocol version of implementation.
     */
    val protocolVersion: ProtocolVersion

    // --------------- Methods called from the driver side ---------------

    /**
     *
     * Parses a EmgPacket from the corresponding string representation. The message packet SHOULD be built
     * with the {@link #buildClientMessage(EmgPacket)} method, in order to ensure
     * that the data is consistent.
     *
     * @param msg String representation of utilized packet type T
     *
     * @return A valid packet of type T, based on the utilized ProtocolVersion, or null, if the packet is damaged.
     */
    fun parseClientMessage(msg: String): T?

    /**
     *
     * {@link#buildFrequencyMessage(Double)} is targeted for the driver software. This message is transmitted
     * to the client and the client will adapt the sending frequency according to the received value.
     * <p>
     * NOTE: The transferred unit is no longer Hz. The Hz are transformed into milliseconds delay between cycles.
     * This step removes unnecessary computation from the receiving client.
     *
     * @param fs Sampling frequency in Hz
     *
     * @return Transferable string representation of the sampling frequency.
     */
    fun buildFrequencyMessage(fs: Double): String

    // --------------- Methods called from the client side ---------------

    /**
     *
     * {@link #buildClientMessage(T)} is only interesting for client devices. Depending on the protocol version this
     * method builds a correct string representation of the packet, defined according to the protocol standard.
     *
     * @param packet Packet which contains all the information which should be transferred. Depending on the
     * protocol version it should at least contain the channels with EMG data
     *
     * @return Transferable string representation of the utilized packet type
     */
    fun buildClientMessage(packet: T): String

    /**
     *
     * Parse a message received from the server. The message is wrapper class for the actual data.
     * Server message has a message type, which is a unique identifier. Messages can optionally have a
     * payload field.
     *
     * @param msg String representation of a ServerMessage
     *
     * @return The parsed server message or null, if the protocol format is corrupt
     */
    fun parseServerMessage(msg: String): ServerMessage?

    /**
     *
     * In case the server side wants to adapt the sending frequency, the client will receive frequency messages,
     * which can be easily parsed with this utility method.
     *
     * @param msg String representation of a ServerMessage with MessageType.FREQUENCY
     *
     * @return Delay in milliseconds between execution cycles (equivalent to sampling frequency)
     */
    fun parseFrequencyMessage(msg: String): Long
}
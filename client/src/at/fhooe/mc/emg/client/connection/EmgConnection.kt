package at.fhooe.mc.emg.client.connection

import io.reactivex.Flowable
import io.reactivex.functions.Consumer

/**
 * @author  Martin Macheiner
 * Date:    24.01.2018.
 *
 * All connection and transmission specific logic is encapsulated in this interface. This makes the {@link EmgClient}
 * more flexible in utilizing different connection types over various mediums (Bluetooth, Wifi, ...). Messages are
 * sent and received through this interface.
 */
interface EmgConnection {

    /**
     * Send the string representation of the message (composed by the {@link at.fhooe.mc.emg.messaging.MessageParser}
     * interface). The connection does not care if it is a valid string representation.
     *
     * @param msg String representation of EMG data object (mainly EmgPacket).
     */
    fun sendMessage(msg: String)

    /**
     * Sets up the connection and waits for an incoming connection from the server.
     *
     * @param successHandler Callback method for successful connection with remote device name.
     * @param errorHandler Callback method for erroneous connection setup with error source.
     */
    fun setup(successHandler: Consumer<String>? = null, errorHandler: Consumer<Throwable>? = null)

    /**
     * Incoming messages indicates, that the server wants to talk to the client in form of
     * {@link at.fhooe.mc.emg.messaging.model.ServerMessage}s. In this case the client must be aware of incoming
     * messages and can listen to them via this method.
     *
     * @return A Flowable which will emit items every time a new message is being received.
     */
    fun subscribeToIncomingMessages(): Flowable<String>

    /**
     * Clean up specific resources after each disconnect, in order to ensure a proper connection the next
     * reconnect.
     */
    fun cleanupAfterDisconnect()

    /**
     * Clean up all resources when the whole EmgClient is shut down.
     */
    fun tearDown()

}
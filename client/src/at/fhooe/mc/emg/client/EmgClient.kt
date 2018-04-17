package at.fhooe.mc.emg.client

import at.fhooe.mc.emg.client.connection.EmgConnection
import at.fhooe.mc.emg.client.sensing.EmgSensor
import at.fhooe.mc.emg.client.sensing.heart.HeartRateProvider
import at.fhooe.mc.emg.messaging.MessageParser
import at.fhooe.mc.emg.messaging.model.EmgPacket
import at.fhooe.mc.emg.messaging.model.ServerMessage
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author  Martin Macheiner
 * Date:    24.01.2018.
 *
 * Excluding the abstract methods (which should never be called directly!), the abstract scaffold provides
 * two main methods of interest, which describes the usage of the EmgClient. Use #start() to setup all resources
 * and wait for incoming connections. Use #stop() to shutdown and free all resources. That's it.
 */
abstract class EmgClient {

    /**
     * Message parser implementation, which takes care of message sending and receiving for the client.
     *
     * NOTE: Each subtype of EmgClient must agree to utilize the EmgPacket as the common data type. On the other
     * hand this revokes the advantages of a generic implementation, but on the other hand it eases the development.
     */
    abstract val msgParser: MessageParser<EmgPacket>

    /**
     * The component which makes the actual sensing of the hardware beneath. Depends on the target platform.
     */
    abstract val emgSensor: EmgSensor

    /**
     * All data transmission relevant topics are handled through this interface. The implementation depends
     * on the used technology standard (Bluetooth, Wifi, Serial) used on the target platform.
     */
    abstract val connection: EmgConnection

    /**
     * Every client which wants to be compatible with protocol version 3 must incorporate a heart rate provider interface.
     * Prior to V3 a fake provider must be provided, as this field must not be null.
     */
    abstract val heartRateProvider: HeartRateProvider

    private var timerDisposable: Disposable? = null
    private var msgDisposable: Disposable? = null

    private var currentHeartRate: Int = -1

    /**
     * protected open to get overriden by subclasses,
     * in order to adapt the period during initialization
     */
    protected open var period: Long = 10

    /**
     * Debug listener for client devices, will be removed in the future
     */
    var debugDataListener: ((EmgPacket) -> Unit)? = null

    /**
     * Updates the delay between transmissions, which is the equivalent of adapting the sampling frequency fs
     * on the server side. Each adaption kills the current timer disposable and starts a new transmission with the
     * updated delay between cycles.
     *
     * @param delayMillis delay between cycles in milliseconds
     */
    private fun updateDelay(delayMillis: Long) {
        period = delayMillis

        timerDisposable?.dispose()
        startTransmission()
    }

    /**
     * Sends the string representation of the data through the provided {@link EmgConnection} interface.
     *
     * @param data String representation of transmitted data.
     */
    private fun send(data: String) {
        connection.sendMessage(data)
    }

    /**
     * Provide a list of EMG data, which will be transferred to the server. Directly query the provided
     * {@link EmgSensor} for the data.
     *
     * @return list of EMG data, grouped in so-called channels.
     */
    private fun provideData(): List<Double> {
        return emgSensor.provideEmgValues()
    }

    /**
     * Cleanup resources after disconnection to the server. Also call the abstract method #cleanupAfterDisconnect()
     * to ensure that subclasses clean up their resources as well.
     */
    private fun closeConnectionAfterDisconnect() {
        stopTransmission()
        connection.cleanupAfterDisconnect()
        msgDisposable?.dispose()
        cleanupAfterDisconnect()
    }

    /**
     * Starts the transmission of packets to the server and also listens and proceeds incoming server messages.
     */
    private fun startDataTransfer() {
        startTransmission()

        // If connected, handle incoming messages in #handleMessage(String)
        msgDisposable = connection.subscribeToIncomingMessages().subscribe({
            handleMessage(it)
        }, {
            it.printStackTrace()
            closeConnectionAfterDisconnect()
        })
    }

    /**
     * Starts the transmission. This means, that the client must already be connected to the sink! Therefore
     * this method is protected and can just be called inside the client when the connection is established.
     * <p>
     * The method creates a timer observable. On subscribe it builds and sends the data through the EmgConnection.
     * It also exposes the packet through a debug listener interface to the client application.
     */
    private fun startTransmission() {
        timerDisposable = Observable.interval(period, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe {
                    val packet = EmgPacket(provideData(), System.currentTimeMillis(), currentHeartRate)
                    send(msgParser.buildClientMessage(packet))
                    debugDataListener?.invoke(packet)
                }
    }

    /**
     * Stops the transmission stream of data packets.
     * <p>
     * NOTE: This does not stop the connected sensors from reading values.
     */
    private fun stopTransmission() {
        timerDisposable?.dispose()
    }


    /**
     * Sets up sensor, connection and heart rate provider and calls the #setup() method to
     * indicate subclasses to setup their resources. If a connection is established the subclasses
     * are notified through the #onConnected(String) method.
     */
    fun start() {
        emgSensor.setup()
        connection.setup(Consumer {
            onConnected(it)
            startDataTransfer()
        }, Consumer {
            onConnectionFailed(it)
        })
        heartRateProvider.start()
        heartRateProvider.subscribeForHeartRateUpdates { currentHeartRate = it }

        setup()
    }

    /**
     * Tears down the transmission, as well as the sensor, the connection and the heart rate provider. Subclasses
     * have to cleanup their resources in the abstract method #cleanup().
     */
    fun stop() {
        timerDisposable?.dispose()
        msgDisposable?.dispose()

        emgSensor.tearDown()
        connection.tearDown()
        heartRateProvider.stop()
        cleanup()
    }

    /**
     * Parse incoming server messages and react according to the message type.
     *
     * @param data String representation of ServerMessage
     */
    private fun handleMessage(data: String) {
        when (msgParser.parseServerMessage(data)?.type) {
            ServerMessage.MessageType.FREQUENCY -> updateDelay(msgParser.parseFrequencyMessage(data))
            ServerMessage.MessageType.NA -> println("Cannot identify server message type!")
        }
    }

    /**
     * Setup all device and platform specific component.
     * Connection, sensor, and heart rate provider are already set up at this moment.
     */
    abstract fun setup()

    /**
     * Counter method of #setup(). This method cleans up all device and platform specific logic.
     * Connection, sensor, and heart rate provider are already released at this moment.
     */
    abstract fun cleanup()

    /**
     * Cleans up all resources after a device disconnects from the client.
     * <p>
     * NOTE: This method cleans up temporary resources and keeps resources, which are needed for a later
     * reconnect of another client.
     * <p>
     * For example: This method should not free resources which are either
     * necessary for sensing or sending data via the connection.
     */
    abstract fun cleanupAfterDisconnect()

    /**
     * Callback method for a successful connection to a remote device.
     *
     * @param device Name of connected device, if available
     */
    abstract fun onConnected(device: String)

    /**
     * Callback method for an exception, which occurred while trying to connect to a remote device.
     *
     * @param t Throwable causing the connection failure.
     */
    abstract fun onConnectionFailed(t: Throwable)

}
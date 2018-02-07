package at.fhooe.mc.emg.client

import at.fhooe.mc.emg.client.connection.EmgConnection
import at.fhooe.mc.emg.client.sensing.EmgSensor
import at.fhooe.mc.emg.messaging.MessageParser
import at.fhooe.mc.emg.messaging.model.EmgPacket
import at.fhooe.mc.emg.messaging.model.ServerMessage
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author Martin Macheiner
 * Date: 24.01.2018.
 **
 */
abstract class EmgClient {

    abstract var msgParser: MessageParser<EmgPacket>
    abstract val emgSensor: EmgSensor
    abstract val connection: EmgConnection

    private var timerDisposable: Disposable? = null
    private var msgDisposable: Disposable? = null

    protected var period: Long = 10

    private fun updateDelay(delayMillis: Long) {
        period = delayMillis

        timerDisposable?.dispose()
        startTransmission()
    }

    private fun send(data: String) {
        connection.sendMessage(data)
    }

    private fun provideData(): List<Double> {
        return emgSensor.provideEmgValues()
    }

    private fun closeConnectionAfterDisconnect() {
        stopTransmission()
        connection.cleanupAfterDisconnect()
        msgDisposable?.dispose()
        cleanupAfterDisconnect()
    }

    private fun startDataTransfer() {
        startTransmission()

        // If connected request read access and integrate #handleMessage()
        msgDisposable = connection.subscribeToIncomingMessages().subscribe({
            handleMessage(it)
        }, {
            it.printStackTrace()
            closeConnectionAfterDisconnect()
        })
    }

    /**
     * Starts the transmission. This means, that the client must already be connected to the sink! Therefore
     * this method is private and can just be called inside when the connection is established
     */
    protected fun startTransmission() {
        timerDisposable = Observable.interval(period, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .subscribe {
                    send(msgParser.buildClientMessage(EmgPacket(provideData(), System.currentTimeMillis())))
                }
    }

    /**
     * Stops the reading of the sensor and disposes the sending of the messages
     */
    protected fun stopTransmission() {
        timerDisposable?.dispose()
    }


    /**
     * Sets up all the necessary stuff for auto connection.
     */
    fun start() {
        emgSensor.setup()
        connection.setup(Consumer {
            onConnected(it)
            startDataTransfer()
        }, Consumer {
            onConnectionFailed(it)
        })

        setup()
    }

    /**
     * Tears down all the connection specific stuff and kills the transmission, if there was one already established
     */
    fun stop() {
        timerDisposable?.dispose()
        msgDisposable?.dispose()

        emgSensor.tearDown()
        connection.tearDown()
        cleanup()
    }

    private fun handleMessage(data: String) {

        when (msgParser.parseServerMessage(data)?.type) {

            ServerMessage.MessageType.FREQUENCY -> updateDelay(msgParser.parseFrequencyMessage(data))
            ServerMessage.MessageType.NA -> println("Cannot identify server message type!")
        }
    }

    /**
     * Setup all device and platform specific components. Connection and sensor is already set up at this moment.
     */
    abstract fun setup()

    /**
     * Counter method of #setup(). This method cleans up all device and platform specific logic.
     */
    abstract fun cleanup()

    /**
     * Cleans up all resources after a device disconnects from the client. NOTE: This method cleans up temporary
     * resources and keeps resources which are needed for a later reconnect of another client. For example: This
     * method should not free resources which are either necessary for sensing or sending data via the connection.
     */
    abstract fun cleanupAfterDisconnect()

    /**
     * Callback method for a successful connection to a remote device.
     */
    abstract fun onConnected(device: String)

    /**
     * Callback method for an exception, which occurred while trying to connect to a remote device.
     */
    abstract fun onConnectionFailed(t: Throwable)

}
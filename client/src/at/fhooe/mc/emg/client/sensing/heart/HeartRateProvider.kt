package at.fhooe.mc.emg.client.sensing.heart

/**
 * @author  Martin Macheiner
 * Date:    06.02.2018
 *
 * In order to stay compatible with the V3 protocol standard of {@link at.fhooe.mc.emg.messaging.MessageInterpreter } the
 * framework needs a way to incorporate heart rate data acquisition. Usually heart rate monitoring can't be done on
 * device (except the unlikely if the target client runs with on Android Wear), therefore it's assumed, that the
 * monitoring device is an external entity (SmartWatch, chest belt, ...). Therefore the interface must also provide
 * a way for connection state callbacks. The {@link EmgClient} provides a reference to the HeartRateProvider to
 * seamlessly integrate it into transmission process.
 *
 */
interface HeartRateProvider {

    /**
     * Connection state of the heart rate provider, as heart rate provider are likely to be an
     * external entity (SmartWatch, chest belt, ...).
     */
    enum class ConnectionState {
        CONNECTING, CONNECTED, DISCONNECTED
    }

    /**
     * Subscribe for heart rate updates with the callback parameter.
     * <p>
     * NOTE: Maybe this method will be deprecated in the future and will be replaced with an observable instead.
     *
     * @param callback Callback functions which will be triggered every time new data is available.
     */
    fun subscribeForHeartRateUpdates(callback: (Int) -> Unit)

    /**
     * Subscribe to connection state changes of the device, as it is likely that heart rate provider has to
     * connect with an external entity (SmartWatch, chest belt, ...).
     *
     * @param callback Callback function for observing connection state changes.
     */
    fun subscribeToConnectionChanges(callback: (ConnectionState) -> Unit)

    /**
     * Free the connection subscription set in #subscribeToConnectionChanges((ConnectionState) -> Unit).
     */
    fun unsubscribeToConnectionChanges()

    /**
     * Setup the connection and start reading data.
     */
    fun start()

    /**
     * Stop connection and free callback resources here.
     */
    fun stop()

}
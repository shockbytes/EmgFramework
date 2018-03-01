package at.fhooe.mc.emg.client.sensing.heart

interface HeartRateProvider {

    enum class ConnectionState {
        CONNECTED, DISCONNECTED
    }

    fun subscribeForHeartRateUpdates(callback: (Int) -> Unit)

    fun subscribeForConnectionChanges(callback: (ConnectionState) -> Unit)

    fun start()

    fun stop()

}
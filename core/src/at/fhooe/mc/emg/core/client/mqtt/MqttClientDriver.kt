package at.fhooe.mc.emg.core.client.mqtt

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentEntryPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentExitPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import at.fhooe.mc.emg.messaging.EmgMessageInterpreter
import at.fhooe.mc.emg.messaging.MessageInterpreter
import at.fhooe.mc.emg.messaging.model.EmgPacket
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence


@EmgComponent(type = EmgComponentType.DEVICE, displayTitle = "MQTT device")
class MqttClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override val name: String
        get() = "Mqtt device @ $ip"

    override val shortName: String = "MQTT"

    override val isDataStorageEnabled: Boolean = true

    override val category: ClientCategory = ClientCategory.NETWORK

    override var msgInterpreter: MessageInterpreter<EmgPacket> = EmgMessageInterpreter(MessageInterpreter.ProtocolVersion.V3)

    @JvmField
    @EmgComponentProperty("localhost", "IP address of server")
    var ip: String = "localhost"

    @JvmField
    @EmgComponentProperty("6706", "Port of server")
    var port: Int = 6706

    private var client: MqttClient? = null

    private var qos = 2

    @EmgComponentEntryPoint
    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {

        try {

            val brokerUrl = "tcp://$ip:$port"

            client = MqttClient(brokerUrl, "emg_consumer", MemoryPersistence())
            client?.setCallback(object : MqttCallbackExtended {
                override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                    successHandler.run()
                    sendSamplingFrequencyToClient()
                }

                override fun messageArrived(topic: String?, message: MqttMessage?) {
                    if (message != null) {
                        processMessage(String(message.payload))
                    }
                }

                override fun connectionLost(cause: Throwable?) {
                    errorHandler.accept(cause)
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                    // Not needed
                }

            })
            client?.connect(MqttConnectOptions())
            client?.subscribe(topicProducerMessages)

        } catch (e: MqttException) {
            e.printStackTrace()
            errorHandler.accept(e)
        }

    }

    @EmgComponentExitPoint
    override fun disconnect() {
        client?.unsubscribe(topicProducerMessages)
        client?.disconnectForcibly(1000, 1000, true)
    }

    override fun sendSamplingFrequencyToClient() {
        val payload = msgInterpreter.buildFrequencyMessage(samplingFrequency).toByteArray()
        client?.publish(topicConsumerMessages, payload, qos, false)
    }

    fun setSocketOptions(ip: String, port: Int) {
        this.ip = ip
        this.port = port
    }

    companion object {

        private const val topicProducerMessages = "emg_p"
        private const val topicConsumerMessages = "emg_c"
    }

}
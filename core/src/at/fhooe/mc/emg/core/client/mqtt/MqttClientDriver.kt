package at.fhooe.mc.emg.core.client.mqtt

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.annotation.EmgComponentEntryPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentExitPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentProperty
import at.fhooe.mc.emg.messaging.EmgMessageParser
import at.fhooe.mc.emg.messaging.MessageParser
import at.fhooe.mc.emg.messaging.model.EmgPacket
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import org.fusesource.hawtbuf.Buffer
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.mqtt.client.*


@EmgComponent(type = EmgComponentType.DEVICE)
class MqttClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override val name: String
        get() = "Mqtt device @ $ip"

    override val shortName: String = "MQTT"

    override val isDataStorageEnabled: Boolean = true

    override val category: ClientCategory = ClientCategory.NETWORK

    override var msgParser: MessageParser<EmgPacket> = EmgMessageParser(MessageParser.ProtocolVersion.V3)

    @EmgComponentProperty
    var ip: String = "localhost"
    @EmgComponentProperty
    var port: Int = 5673

    private var mqtt: MQTT = MQTT()

    private var connection: CallbackConnection? = null

    @EmgComponentEntryPoint
    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {

        mqtt.setHost(ip, port)

        connection = mqtt.callbackConnection()
        connection?.connect(object : Callback<Void> {
            override fun onSuccess(value: Void?) {
                // Initialize by sending sampling frequency
                sendSamplingFrequencyToClient()
            }

            override fun onFailure(value: Throwable?) {
                errorHandler.accept(value)
            }
        })
        connection?.listener(object : Listener {
            override fun onFailure(value: Throwable?) {
                errorHandler.accept(value)
            }

            override fun onPublish(topic: UTF8Buffer?, body: Buffer?, ack: Runnable?) {
                if (body != null) {
                    processMessage(String(body.data))
                }
            }

            override fun onConnected() {
                successHandler.run()
            }

            override fun onDisconnected() {
                errorHandler.accept(IllegalAccessException("Client disconnected!"))
            }
        })
    }

    @EmgComponentExitPoint
    override fun disconnect() {
        connection?.disconnect(null)
    }

    override fun sendSamplingFrequencyToClient() {
        val payload = msgParser.buildFrequencyMessage(samplingFrequency).toByteArray()
        connection?.publish("EMG", payload, QoS.EXACTLY_ONCE, true, null)
    }

    fun setSocketOptions(ip: String, port: Int) {
        this.ip = ip
        this.port = port
    }

}
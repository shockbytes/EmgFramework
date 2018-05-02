package at.fhooe.mc.emg.desktop

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.client.mqtt.MqttClientDriver
import at.fhooe.mc.emg.core.client.network.NetworkClientDriver
import at.fhooe.mc.emg.core.client.simulation.SimulationClientDriver
import at.fhooe.mc.emg.desktop.client.bluetooth.DesktopBluetoothClientDriver
import at.fhooe.mc.emg.desktop.client.serial.DesktopSerialClientDriver
import at.fhooe.mc.emg.messaging.EmgMessageParser
import at.fhooe.mc.emg.messaging.JsonMessageParser
import at.fhooe.mc.emg.messaging.MessageParser
import at.fhooe.mc.emg.messaging.ProtoBufMessageParser
import at.fhooe.mc.emg.messaging.model.EmgPacket
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.text.DecimalFormat

class DriverComparisonTest {

    @Test
    fun testSimulationDriver() {

        val name = "simulation"
        for (round in 0 until ROUNDS) {
            // Json
            simulationDriver.simulationSource = simulationDriver.simulationSources.find { it.name.contains("-json") }
            testDriver(simulationDriver, JsonMessageParser(), name, Type.JSON, round)

            // Emg
            simulationDriver.simulationSource = simulationDriver.simulationSources.find { it.name.contains("-emg") }
            testDriver(simulationDriver, EmgMessageParser(MessageParser.ProtocolVersion.V3), name, Type.SELF, round)

            // ProtoBuf
            simulationDriver.simulationSource = simulationDriver.simulationSources.find { it.name.contains("-proto") }
            testDriver(simulationDriver, ProtoBufMessageParser(MessageParser.ProtocolVersion.V3), name, Type.PROTO, round)
        }
    }

    @Test
    fun testNetworkDriver() {

        val name = "network"
        for (round in 0 until ROUNDS) {
            // Json
            testDriver(networkDriver, JsonMessageParser(), name, Type.JSON, round)
            // Emg
            testDriver(networkDriver, EmgMessageParser(MessageParser.ProtocolVersion.V3), name, Type.SELF, round)
            // ProtoBuf
            testDriver(networkDriver, ProtoBufMessageParser(MessageParser.ProtocolVersion.V3), name, Type.PROTO, round)
        }
    }

    @Test
    fun testMqttDriver() {

        val name = "mqtt_com"
        for (round in 0 until ROUNDS) {
            // Json
            testDriver(mqttDriver, JsonMessageParser(), name, Type.JSON, round)
            // Emg
            testDriver(mqttDriver, EmgMessageParser(MessageParser.ProtocolVersion.V3), name, Type.SELF, round)
            // ProtoBuf
            testDriver(mqttDriver, ProtoBufMessageParser(MessageParser.ProtocolVersion.V3), name, Type.PROTO, round)
        }
    }

    @Test
    fun testSerialDriver() {

        val name = "serial_com"
        for (round in 0 until ROUNDS) {
            // Json
            testDriver(serialDriver, JsonMessageParser(), name, Type.JSON, round)
            // Emg
            testDriver(serialDriver, EmgMessageParser(MessageParser.ProtocolVersion.V3), name, Type.SELF, round)
            // ProtoBuf
            testDriver(serialDriver, ProtoBufMessageParser(MessageParser.ProtocolVersion.V3), name, Type.PROTO, round)
        }
    }

    @Test
    fun testBluetoothDriver() {

        val name = "bluetooth"
        for (round in 0 until ROUNDS) {
            // Json
            testDriver(bluetoothDriver, JsonMessageParser(), name, Type.JSON, round)
            // Emg
            testDriver(bluetoothDriver, EmgMessageParser(MessageParser.ProtocolVersion.V3), name, Type.SELF, round)
            // ProtoBuf
            testDriver(bluetoothDriver, ProtoBufMessageParser(MessageParser.ProtocolVersion.V3), name, Type.PROTO, round)
        }
    }

    private fun testDriver(driver: EmgClientDriver, parser: MessageParser<EmgPacket>,
                           name: String, type: Type, round: Int) {

        driver.msgParser = parser
        val action = Action {
            println("$name#$type started round $round")
            val start = System.currentTimeMillis()
            driver.channeledCallbackSubject.takeWhile { it.emgSize <= THRESHOLD }.subscribe({ }, { }, {
                val metric = (System.currentTimeMillis() - start) / 1000.0
                println("$name#$type finished round $round")
                driver.disconnect()
                driver.clearData()

                map[name]?.get(type.ordinal)?.add(metric)
            })
        }
        driver.connect(action, Consumer {})
        Thread.sleep(THREAD_TIMEOUT)
    }

    companion object {

        enum class Type {
            JSON, SELF, PROTO
        }

        private const val SAMPLING_FREQUENCY: Double = 1000.0
        private const val THRESHOLD: Int = (10 * SAMPLING_FREQUENCY).toInt() // 10 seconds, based on sampling frequency
        private const val THREAD_TIMEOUT = 20000L // 20 seconds
        private const val ROUNDS = 5

        private val map: MutableMap<String, MutableList<MutableList<Double>>> = HashMap()

        private lateinit var simulationDriver: SimulationClientDriver
        private lateinit var networkDriver: NetworkClientDriver
        private lateinit var mqttDriver: MqttClientDriver
        private lateinit var serialDriver: DesktopSerialClientDriver
        private lateinit var bluetoothDriver: DesktopBluetoothClientDriver

        @JvmStatic
        @BeforeClass
        fun setup() {

            setupSimulationDriver()
            setupNetworkDriver()
            setupMqttDriver()
            setupSerialDriver()
            setupBluetoothDriver()

            setupOutputMap()
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {

            val formatter = DecimalFormat("#00.000")
            val header = "\nName\t\t|\t\tJson\t\t\tEmg\t\t\t\tProto\n-------------------------------------------------------------\n"
            val data = map.entries.joinToString("\n") { (name, values) ->
                "$name:\t|\t\t${values.joinToString("\t\t\t") { formatter.format(it.average()) }}"
            }

            val output = header.plus(data)
            println(output)
        }

        private fun setupSimulationDriver() {
            val simFolder = "${System.getProperty("user.dir")}/test/simdata"
            simulationDriver = SimulationClientDriver(null, simFolder)
            simulationDriver.isEndlessLoopEnabled = true
            simulationDriver.samplingFrequency = SAMPLING_FREQUENCY
        }

        private fun setupNetworkDriver() {
            networkDriver = NetworkClientDriver()
            networkDriver.setSocketOptions("SET IP TO ANDROID DEVICE", 5684)
            networkDriver.samplingFrequency = SAMPLING_FREQUENCY
        }

        private fun setupMqttDriver() {
            mqttDriver = MqttClientDriver()
            mqttDriver.samplingFrequency = SAMPLING_FREQUENCY
        }

        private fun setupSerialDriver() {
            serialDriver = DesktopSerialClientDriver()
            serialDriver.dataRate = DesktopSerialClientDriver.supportedDataRates.last()
            serialDriver.portName = "COM3"
            serialDriver.samplingFrequency = SAMPLING_FREQUENCY
        }

        private fun setupBluetoothDriver() {
            bluetoothDriver = DesktopBluetoothClientDriver()
            bluetoothDriver.channel = "SET CHANNEL ACCORDING TO ANDROID DEVICE"
            bluetoothDriver.remoteDeviceMacAddress = "SET ACCORDING TO ANDROID DEVICE"
            bluetoothDriver.samplingFrequency = SAMPLING_FREQUENCY
        }

        private fun setupOutputMap() {
            map["simulation"] = mutableListOf(mutableListOf(0.0), mutableListOf(0.0), mutableListOf(0.0))
            map["network"] = mutableListOf(mutableListOf(0.0), mutableListOf(0.0), mutableListOf(0.0))
            map["mqtt_com"] = mutableListOf(mutableListOf(0.0), mutableListOf(0.0), mutableListOf(0.0))
            map["serial_com"] = mutableListOf(mutableListOf(0.0), mutableListOf(0.0), mutableListOf(0.0))
            map["bluetooth"] = mutableListOf(mutableListOf(0.0), mutableListOf(0.0), mutableListOf(0.0))
        }

    }

}
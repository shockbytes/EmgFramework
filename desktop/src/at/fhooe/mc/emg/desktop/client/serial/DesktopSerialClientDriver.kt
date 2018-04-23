package at.fhooe.mc.emg.desktop.client.serial

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.designer.annotation.EmgComponent
import at.fhooe.mc.emg.designer.EmgComponentType
import at.fhooe.mc.emg.messaging.EmgMessageParser
import at.fhooe.mc.emg.messaging.MessageParser
import at.fhooe.mc.emg.messaging.model.EmgPacket
import gnu.io.*
import io.reactivex.Completable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.*

@EmgComponent(type = EmgComponentType.DEVICE)
class DesktopSerialClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv), SerialPortEventListener {

    private var ports: List<CommPortIdentifier>? = null

    private var inputReader: BufferedReader? = null
    private var outputWriter: BufferedWriter? = null
    private var connectionPort: SerialPort? = null

    private var portName: String? = null

    override val name: String
        get() = if (portName == null) shortName else "Serial device @ $portName"

    override val shortName: String = "Serial"

    override val isDataStorageEnabled: Boolean = true

    override val msgParser: MessageParser<EmgPacket> = EmgMessageParser(MessageParser.ProtocolVersion.V1)

    override val category: ClientCategory = ClientCategory.SERIAL

    var dataRate: Int = 0

    init {
        initializePorts()
    }

    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {
        Completable.fromAction {

            connectionPort = getPortByName(portName).open(javaClass.name, timeout) as SerialPort
            dataRate = defaultDataRate
            setupConnectionParams()

            inputReader = BufferedReader(InputStreamReader(connectionPort?.inputStream))
            outputWriter = BufferedWriter(OutputStreamWriter(connectionPort?.outputStream))
            connectionPort?.addEventListener(this)
            connectionPort?.notifyOnDataAvailable(true)

        }.subscribeOn(Schedulers.io()).subscribe(successHandler, errorHandler)
    }

    override fun disconnect() {

        connectionPort?.removeEventListener()
        connectionPort?.close()

        inputReader?.close()
        outputWriter?.close()

        connectionPort = null
    }

    override fun serialEvent(event: SerialPortEvent) {

        if (event.eventType == SerialPortEvent.DATA_AVAILABLE) {

            try {
                val msg = inputReader?.readLine()
                if (msg != null && msg.isNotEmpty()) {
                    processMessage(msg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun sendSamplingFrequencyToClient() {
        try {
            outputWriter?.write(msgParser.buildFrequencyMessage(samplingFrequency))
            outputWriter?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(UnsupportedCommOperationException::class)
    private fun setupConnectionParams() {
        connectionPort?.setSerialPortParams(dataRate,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE)
    }

    private fun initializePorts() {
        @Suppress("UNCHECKED_CAST")
        ports = Collections.list<CommPortIdentifier>(CommPortIdentifier.getPortIdentifiers()
                as Enumeration<CommPortIdentifier>?)

        if (ports?.isNotEmpty() == true) {
            selectSerialPort(ports!![0].name)
        }
    }

    @Throws(NoSuchPortException::class)
    private fun getPortByName(name: String?): CommPortIdentifier {
        return CommPortIdentifier.getPortIdentifier(name)
    }

    private fun setPortName(portName: String) {
        this.portName = portName
    }

    fun getAvailablePortNames(forceUpdate: Boolean): List<String> {
        if (forceUpdate) {
            initializePorts()
        }
        return ports?.map { it.name } ?: listOf()
    }

    fun selectSerialPort(port: String) {
        setPortName(port)
    }

    companion object {

        private const val timeout = 5000
        const val defaultDataRate = 115200

        val supportedDataRates = intArrayOf(4800, 9600, 19200, 57600, 115200, 230400)
    }
}
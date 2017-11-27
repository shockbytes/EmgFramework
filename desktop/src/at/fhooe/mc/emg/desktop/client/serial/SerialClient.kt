package at.fhooe.mc.emg.desktop.client.serial

import EmgMessaging
import at.fhooe.mc.emg.client.ClientCategory
import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.client.EmgClientConfigView
import gnu.io.*
import java.io.*
import java.util.*
import kotlin.streams.toList

class SerialClient(cv: EmgClientConfigView? = null) : EmgClient(cv), SerialPortEventListener {

    private var ports: List<CommPortIdentifier>? = null

    private var inputReader: BufferedReader? = null
    private var outputWriter: BufferedWriter? = null
    private var connectionPort: SerialPort? = null

    private var portName: String? = null

    override val name: String
        get() = if (portName == null) shortName else "Serial device @ " + portName!!

    override val shortName: String = "Serial"

    override val isDataStorageEnabled: Boolean = true

    override val protocolVersion: EmgMessaging.ProtocolVersion = EmgMessaging.ProtocolVersion.V1

    override val category: ClientCategory = ClientCategory.SERIAL

    var dataRate: Int = 0

    init {
        initializePorts()
    }

    @Throws(Exception::class)
    override fun connect() {

        connectionPort = getPortByName(portName).open(javaClass.name, timeout) as SerialPort
        dataRate = defaultDataRate
        setupConnectionParams()

        inputReader = BufferedReader(InputStreamReader(connectionPort?.inputStream))
        outputWriter = BufferedWriter(OutputStreamWriter(connectionPort?.outputStream))
        connectionPort?.addEventListener(this)
        connectionPort?.notifyOnDataAvailable(true)
    }

    override fun disconnect() {

        synchronized(this) {

            connectionPort?.removeEventListener()
            connectionPort?.close()

            inputReader?.close()
            outputWriter?.close()

            connectionPort = null
        }
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
            outputWriter?.write(EmgMessaging.buildFrequencyMessage(samplingFrequency))
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

        if (ports!!.isNotEmpty()) {
            setSerialPortSelected(ports!![0].name)
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
        if (ports != null) {
            return ports!!.stream().map<String>({ it.name }).toList()
        }
        return arrayListOf()
    }

    fun setSerialPortSelected(port: String) {
        setPortName(port)
    }

    companion object {

        private val timeout = 5000
        val defaultDataRate = 115200

        val supportedDataRates = intArrayOf(4800, 9600, 19200, 57600, 115200, 230400)
    }
}
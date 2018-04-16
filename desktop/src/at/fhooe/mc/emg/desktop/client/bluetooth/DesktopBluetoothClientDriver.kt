package at.fhooe.mc.emg.desktop.client.bluetooth

import at.fhooe.mc.emg.clientdriver.ClientCategory
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.clientdriver.EmgClientDriverConfigView
import at.fhooe.mc.emg.messaging.EmgMessageParser
import at.fhooe.mc.emg.messaging.MessageParser
import at.fhooe.mc.emg.messaging.model.EmgPacket
import com.intel.bluetooth.MicroeditionConnector
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.io.*
import javax.bluetooth.RemoteDevice
import javax.bluetooth.UUID
import javax.microedition.io.StreamConnection

/**
 * @author  Martin Macheiner
 * Date:    26.01.2018
 *
 */

class DesktopBluetoothClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override val name: String
        get() = "Bluetooth @ ${remoteDevice?.getFriendlyName(false)}"

    override val shortName = "Bluetooth"

    override val isDataStorageEnabled = true

    override val category = ClientCategory.BLUETOOTH

    override val msgParser: MessageParser<EmgPacket> = EmgMessageParser(MessageParser.ProtocolVersion.V3)

    // Debug Mac address
    // Moto G5(s):      D4:63:C6:39:DD:23
    // Rasperry Pi 3:   22:22:20:E8:93:4

    // These properties can be changed in the ConfigView, therefore they aren't private
    var remoteDeviceMacAddress: String = "22:22:20:E8:93:47"
    var uuid: UUID = UUID("5f77cdab8f4847849958d2736f4727c5", false)
    var channel: String = "2"

    private var remoteDevice: RemoteDevice? = null
    private var connection: StreamConnection? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var readerDisposable: Disposable? = null

    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {
        Completable.fromAction {

            remoteDevice = EmgBluetoothRemoteDevice(normalizedMac())
            connection = MicroeditionConnector.open(buildUrl()) as StreamConnection
            writer = PrintWriter(BufferedWriter(OutputStreamWriter(connection?.openOutputStream())))
            reader = BufferedReader(InputStreamReader(connection?.openInputStream()))
            subscribeToDataTransfer(errorHandler)

        }.subscribeOn(Schedulers.io()).subscribe(successHandler, errorHandler)
    }

    override fun disconnect() {
        readerDisposable?.dispose()
        writer?.close()
        reader?.close()
        connection?.close()
    }

    override fun sendSamplingFrequencyToClient() {
        writer?.print(msgParser.buildFrequencyMessage(samplingFrequency))
        writer?.flush()
    }

    private fun buildUrl(): String {

        val scheme = "btspp"
        val params = "authenticate=false;encrypt=false;master=false"
        return "$scheme://${normalizedMac()}:$channel;$params"
    }

    private fun subscribeToDataTransfer(errorHandler: Consumer<Throwable>) {

        readerDisposable = Observable.create { e: ObservableEmitter<String> ->

            if (reader != null) {
                reader?.lines()?.forEach {
                    e.onNext(it)
                }
            } else {
                e.onError(Throwable("Reader is not present!"))
            }
            e.onComplete()

        }.subscribeOn(Schedulers.io()).subscribe({
            processMessage(it)
        }, { errorHandler.accept(it) })

    }

    private fun normalizedMac() = remoteDeviceMacAddress.replace(":", "")

}
package at.fhooe.mc.emg.desktop.client.bluetooth

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

@EmgComponent(type = EmgComponentType.DEVICE, displayTitle = "Bluetooth device")
class DesktopBluetoothClientDriver(cv: EmgClientDriverConfigView? = null) : EmgClientDriver(cv) {

    override val name: String
        get() = "Bluetooth @ ${remoteDevice?.getFriendlyName(false)}"

    override val shortName = "Bluetooth"

    override val isDataStorageEnabled = true

    override val category = ClientCategory.BLUETOOTH

    override var msgInterpreter: MessageInterpreter<EmgPacket> = EmgMessageInterpreter(MessageInterpreter.ProtocolVersion.V3)

    @JvmField
    @EmgComponentProperty("22:22:F3:CA:80:14", "Remote MAC address")
    var remoteDeviceMacAddress: String = "22:22:F3:CA:80:14"

    @JvmField
    @EmgComponentProperty("2", "Bluetooth Channel")
    var channel: String = "2"

    var uuid: UUID = UUID("5f77cdab8f4847849958d2736f4727c5", false)

    private var remoteDevice: RemoteDevice? = null
    private var connection: StreamConnection? = null
    private var writer: PrintWriter? = null
    private var reader: BufferedReader? = null
    private var readerDisposable: Disposable? = null

    @EmgComponentEntryPoint
    override fun connect(successHandler: Action, errorHandler: Consumer<Throwable>) {
        Completable.fromAction {

            remoteDevice = EmgBluetoothRemoteDevice(normalizedMac())
            connection = MicroeditionConnector.open(buildUrl()) as StreamConnection
            writer = PrintWriter(BufferedWriter(OutputStreamWriter(connection?.openOutputStream())))
            reader = BufferedReader(InputStreamReader(connection?.openInputStream()))
            subscribeToDataTransfer(errorHandler)

        }.subscribeOn(Schedulers.io()).subscribe(successHandler, errorHandler)
    }

    @EmgComponentExitPoint
    override fun disconnect() {
        readerDisposable?.dispose()
        writer?.close()
        reader?.close()
        connection?.close()
    }

    override fun sendSamplingFrequencyToClient() {
        writer?.print(msgInterpreter.buildFrequencyMessage(samplingFrequency))
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
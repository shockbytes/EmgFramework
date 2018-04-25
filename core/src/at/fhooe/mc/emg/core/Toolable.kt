package at.fhooe.mc.emg.core

import at.fhooe.mc.emg.clientdriver.model.EmgData
import at.fhooe.mc.emg.core.storage.DataStorage
import at.fhooe.mc.emg.core.tool.Tool
import io.reactivex.functions.Action

interface Toolable {

    val currentDataPointer: Int

    val dataForFrequencyAnalysis: DoubleArray

    fun connectToClient(successHandler: Action?)

    fun disconnectFromClient(writeFileOnDisconnectFileName: String?)

    fun exportData(filename: String, dataStorage: DataStorage)

    fun getSingleChannelDataSection(start: Int, stop: Int, channel: Int): EmgData

    fun registerToolForUpdates(t: Tool)

    fun unregisterToolUpdates(t: Tool)

}
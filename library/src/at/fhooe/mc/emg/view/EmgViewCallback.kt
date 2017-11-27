package at.fhooe.mc.emg.view

import at.fhooe.mc.emg.client.EmgClient
import at.fhooe.mc.emg.storage.DataStorage
import at.fhooe.mc.emg.util.Configuration
import at.fhooe.mc.emg.util.FrequencyAnalysis

/**
 * Author:  Martin Macheiner
 * Date:    26.11.2017
 */

interface EmgViewCallback {

    fun closeView(config: Configuration)

    fun setSelectedClient(client: EmgClient)

    fun connectToClient()

    fun disconnectFromClient(writeFileOnDisconnectFileName: String?)

    fun exportData(filename: String, dataStorage: DataStorage)

    fun setSamplingFrequency(frequency: Double)

    fun setSimulationPlaybackLoopEnabled(isEnabled: Boolean)

    fun requestFrequencyAnalysis(type: FrequencyAnalysis.AnalysisType)

}
package at.fhooe.mc.emg.core.view

import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.storage.DataStorage
import at.fhooe.mc.emg.core.util.Configuration

/**
 * Author:  Martin Macheiner
 * Date:    26.11.2017
 */

interface EmgViewCallback {

    fun closeView(config: Configuration)

    fun setSelectedClient(client: EmgClientDriver)

    fun connectToClient()

    fun disconnectFromClient(writeFileOnDisconnectFileName: String?)

    fun exportData(filename: String, dataStorage: DataStorage)

    fun setSamplingFrequency(frequency: Double)

    fun setSimulationPlaybackLoopEnabled(isEnabled: Boolean)

    fun requestFrequencyAnalysis(method: FrequencyAnalysisMethod.Method)

}
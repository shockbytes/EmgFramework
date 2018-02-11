package at.fhooe.mc.emg.core.view

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.storage.DataStorage
import at.fhooe.mc.emg.core.util.config.EmgConfig
import io.reactivex.functions.Action

/**
 * Author:  Martin Macheiner
 * Date:    26.11.2017
 */

interface EmgViewCallback {

    fun closeView(config: EmgConfig)

    fun setSelectedClient(client: EmgClientDriver)

    fun connectToClient(successHandler: Action? = null)

    fun disconnectFromClient(writeFileOnDisconnectFileName: String? = null)

    fun exportData(filename: String, dataStorage: DataStorage)

    fun setSamplingFrequency(frequency: Double)

    fun setSimulationPlaybackLoopEnabled(isEnabled: Boolean)

    fun requestFrequencyAnalysisView(method: FrequencyAnalysisMethod.Method)

    fun setVisualViewEnabled(visualEnabled: Boolean)

    fun isDataStorageEnabled(): Boolean

    fun isHeartRateSensingSupported(): Boolean
}
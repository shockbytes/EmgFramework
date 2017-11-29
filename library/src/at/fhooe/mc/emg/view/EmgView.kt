package at.fhooe.mc.emg.view

import at.fhooe.mc.emg.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.client.EmgClientDriver
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.filter.Filter
import at.fhooe.mc.emg.tools.Tool
import at.fhooe.mc.emg.util.Configuration

/**
 * Author:  Mescht
 * Date:    22.11.2017
 */
interface EmgView {

    fun setupView(viewCallback: EmgViewCallback, config: Configuration)

    fun reset()

    fun updateStatus(status: String)

    fun setDeviceControlsEnabled(isEnabled: Boolean)

    fun onRawClientDataAvailable(raw: String)

    fun onChanneledClientDataAvailable(cd: ChannelData, filters: List<Filter>)

    // -------------------------------------------------------

    // ------------------ Utility list views -----------------

    fun setupToolsView(tools: List<Tool>, controller: EmgController)

    fun setupFilterViews(filter: List<Filter>)

    fun setupEmgClientDriverView(clients: List<EmgClientDriver>, defaultClient: EmgClientDriver)

    fun setupEmgClientDriverConfigViews(clients: List<EmgClientDriver>)

    // -------------------------------------------------------

    fun showFrequencyAnalysis(method: FrequencyAnalysisMethod)

}
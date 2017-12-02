package at.fhooe.mc.emg.core.view

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.EmgController
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.util.config.EmgConfig

/**
 * Author:  Mescht
 * Date:    22.11.2017
 */
interface EmgView {

    fun setupView(viewCallback: EmgViewCallback, config: EmgConfig)

    fun reset()

    fun updateStatus(status: String)

    fun lockDeviceControls(isLocked: Boolean)

    fun onRawClientDataAvailable(raw: String)

    // -------------------------------------------------------

    // ------------------ Utility list views -----------------

    fun setupToolsView(tools: List<Tool>, controller: EmgController)

    fun setupFilterViews(filter: List<Filter>)

    fun setupEmgClientDriverView(clients: List<EmgClientDriver>, defaultClient: EmgClientDriver)

    fun setupEmgClientDriverConfigViews(clients: List<EmgClientDriver>)

    // -------------------------------------------------------

    fun showFrequencyAnalysisView(method: FrequencyAnalysisMethod)

}
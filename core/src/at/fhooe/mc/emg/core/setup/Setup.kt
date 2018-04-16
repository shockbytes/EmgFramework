package at.fhooe.mc.emg.core.setup

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.EmgBuildComponent
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.tools.Tool

interface Setup {

    val tools: List<Tool>

    val filter: List<Filter>

    val driver: List<EmgClientDriver>

    val frequencyAnalysisMethods: List<FrequencyAnalysisMethod>

    val components: List<EmgBuildComponent>

    val fileStorage: FileStorage

    val configStorage: EmgConfigStorage

}
package at.fhooe.mc.emg.core.setup

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.tools.Tool
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage

interface Setup {

    val tools: List<Tool>

    val filter: List<Filter>

    val driver: List<EmgClientDriver>

    val components: List<Class<*>>

    val fileStorage: FileStorage

    val configStorage: EmgConfigStorage

}
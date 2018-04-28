package at.fhooe.mc.emg.core.injection

import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage

interface PlatformConfiguration {

    val simulationFolder: String

    val fileStorage: FileStorage

    val configStorage: EmgConfigStorage

}
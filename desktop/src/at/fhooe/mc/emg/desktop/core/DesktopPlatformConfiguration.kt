package at.fhooe.mc.emg.desktop.core

import at.fhooe.mc.emg.core.injection.PlatformConfiguration
import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.storage.SimpleFileStorage
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.storage.config.JsonEmgConfigStorage
import java.io.File

class DesktopPlatformConfiguration : PlatformConfiguration {

    override val simulationFolder: String = System.getProperty("user.dir") + "/data/simulation"

    override val dataFolder: String = "${System.getProperty("user.dir")}/data/"

    override val fileStorage: FileStorage = SimpleFileStorage()

    override val configStorage: EmgConfigStorage = JsonEmgConfigStorage(File(System.getProperty("user.dir") + "/data/config.json"))

}

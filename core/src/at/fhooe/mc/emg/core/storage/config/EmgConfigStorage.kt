package at.fhooe.mc.emg.core.storage.config

import at.fhooe.mc.emg.core.util.EmgConfig

/**
 * Author:  Mescht
 * Date:    01.12.2017
 */
interface EmgConfigStorage {

    val emgConfig: EmgConfig

    fun store(config: EmgConfig)

}
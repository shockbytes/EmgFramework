package at.fhooe.mc.emg.core.util.config

/**
 * Author:  Mescht
 * Date:    01.12.2017
 */
interface EmgConfigStorage {

    fun load(): EmgConfig

    fun store(config: EmgConfig)

}
package at.fhooe.mc.emg.core.storage

import at.fhooe.mc.emg.clientdriver.ChannelData

interface DataStorage {

    fun store(path: String, data: ChannelData): Boolean

}

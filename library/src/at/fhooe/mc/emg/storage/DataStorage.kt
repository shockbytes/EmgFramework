package at.fhooe.mc.emg.storage

import at.fhooe.mc.emg.client.ChannelData

interface DataStorage {

    fun store(path: String, data: ChannelData): Boolean

}

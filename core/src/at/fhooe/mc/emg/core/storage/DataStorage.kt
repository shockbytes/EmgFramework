package at.fhooe.mc.emg.core.storage

import at.fhooe.mc.emg.clientdriver.model.EmgData

interface DataStorage {

    fun store(path: String, data: EmgData): Boolean

}

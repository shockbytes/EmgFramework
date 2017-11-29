package at.fhooe.mc.emg.core.tools.conconi

import at.fhooe.mc.emg.clientdriver.ChannelData
import java.io.Serializable
import java.util.*

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
internal class ConconiData : Serializable {

    private val roundData: MutableList<ChannelData>

    val roundCount: Int
        get() = roundData.size

    init {
        roundData = ArrayList()
    }

    fun addRoundData(crd: ChannelData) {
        roundData.add(crd)
    }

    fun getRoundData(index: Int): ChannelData {
        return roundData[index]
    }

}

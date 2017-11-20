package at.fhooe.mc.emg.tools.conconi

import at.fhooe.mc.emg.client.ChannelData

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
interface ConconiToolListener {

    fun onCountdownTick(seconds: Int)

    fun onTick(seconds: Int, goal: Int)

    fun onRoundDataAvailable(data: ChannelData, round: Int)

}

package at.fhooe.mc.emg.tools.conconi

import at.fhooe.mc.emg.client.ChannelData

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */

interface ConconiView {

    fun setup(viewCallback: ConconiViewCallback)

    fun onCountdownTick(seconds: Int)

    fun onTick(seconds: Int, goal: Int)

    fun onRoundDataAvailable(data: ChannelData, round: Int)

}
package at.fhooe.mc.emg.tools.conconi

import at.fhooe.mc.emg.client.ChannelData
import at.fhooe.mc.emg.tools.ToolView

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */

interface ConconiView : ToolView<ConconiViewCallback> {

    fun onCountdownTick(seconds: Int)

    fun onTick(seconds: Int, goal: Int)

    fun onRoundDataAvailable(data: ChannelData, round: Int)

}
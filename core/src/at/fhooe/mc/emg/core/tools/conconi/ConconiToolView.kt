package at.fhooe.mc.emg.core.tools.conconi

import at.fhooe.mc.emg.core.tools.ToolView

/**
 * Author:  Mescht
 * Date:    20.11.2017
 */

interface ConconiToolView : ToolView<ConconiToolViewCallback> {

    fun onCountdownTick(seconds: Int)

    fun onTick(seconds: Int, goal: Int)

    fun onRoundDataAvailable(data: ConconiRoundData, round: Int)

    fun onPlayCountdownSound()

}
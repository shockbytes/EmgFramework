package at.fhooe.mc.emg.core.util

/**
 * Author:  Mescht
 * Date:    01.12.2017
 */
data class EmgConfig(var isWriteToLogEnabled: Boolean = false,
                     var isCopyToSimulationEnabled: Boolean = false,
                     var isSimulationEndlessLoopEnabled: Boolean = false,
                     var runningAverageWindowSize: Int = 30,
                     var savitzkyGolayFilterWidth: Int = 10)
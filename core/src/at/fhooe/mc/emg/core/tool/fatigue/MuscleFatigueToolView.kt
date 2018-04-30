package at.fhooe.mc.emg.core.tool.fatigue

import at.fhooe.mc.emg.core.tool.ToolView

interface MuscleFatigueToolView : ToolView<MuscleFatigueToolViewCallback> {

    fun update(values: List<Pair<Double, Double>>)

    fun showError(s: String)

    fun clear()

}
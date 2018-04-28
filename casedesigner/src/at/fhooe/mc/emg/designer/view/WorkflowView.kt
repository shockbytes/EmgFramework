package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.WorkflowViewCallback

interface WorkflowView {

    fun startView(callback: WorkflowViewCallback)

    fun close()

    fun onStart()

    fun onStop()

}
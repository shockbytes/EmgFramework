package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.model.Workflow

interface WorkflowItemViewManager {

    fun startConsumer(item: Workflow.StartableViewConsumer)

    fun showPlatformView(item: Workflow.ViewableConsumer)

    fun releaseViews()
}
package at.fhooe.mc.emg.designer.view

import at.fhooe.mc.emg.designer.model.Workflow

interface WorkflowItemViewManager {

    fun startProducer(item: Workflow.StartableProducer)

    fun showPlatformView(item: Workflow.ViewableConsumer)

    fun releaseViews()
}
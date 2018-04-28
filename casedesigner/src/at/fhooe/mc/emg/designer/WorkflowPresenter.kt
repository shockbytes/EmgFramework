package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.model.Workflow
import at.fhooe.mc.emg.designer.view.WorkflowView

class WorkflowPresenter(private val workflow: Workflow, private val view: WorkflowView?): WorkflowViewCallback {

    init {
        view?.startView(this)
    }

    override fun start() {
        workflow.start()
        view?.onStart()
    }

    override fun stop() {
        workflow.stop()
        view?.onStop()
    }

    override fun close() {
        view?.close()
    }

    override fun tearDown() {
        stop()
        workflow.release()
    }

}
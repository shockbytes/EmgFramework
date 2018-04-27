package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.model.Workflow
import at.fhooe.mc.emg.designer.view.WorkflowView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class WorkflowPresenter(private val workflow: Workflow, private val view: WorkflowView?) {

    init {

        // TODO Just testing
        start()

        var disposable: Disposable? = null
        disposable = Observable.interval(20, TimeUnit.SECONDS).subscribe {
            stop()
            disposable?.dispose()
        }
    }

    fun start() {
        workflow.start()
    }

    fun stop() {
        workflow.stop()
    }
}
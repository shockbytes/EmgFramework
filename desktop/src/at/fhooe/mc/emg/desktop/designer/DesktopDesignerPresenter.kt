package at.fhooe.mc.emg.desktop.designer

import at.fhooe.mc.emg.core.storage.FileStorage
import at.fhooe.mc.emg.core.storage.SimpleFileStorage
import at.fhooe.mc.emg.designer.DesignerPresenter
import at.fhooe.mc.emg.designer.WorkflowPresenter
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.designer.model.Workflow
import at.fhooe.mc.emg.designer.model.WorkflowConfiguration
import at.fhooe.mc.emg.designer.view.ComponentPropertyView
import at.fhooe.mc.emg.designer.view.DesignerView
import at.fhooe.mc.emg.desktop.core.DesktopPlatformConfiguration
import at.fhooe.mc.emg.desktop.view.DesktopComponentPropertyView
import at.fhooe.mc.emg.desktop.view.DesktopWorkflowView
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File

class DesktopDesignerPresenter(view: DesignerView,
                               designerComponents: List<EmgBaseComponent>,
                               designerPipes: List<EmgComponentPipe<Any, Any>>)
    : DesignerPresenter(view, designerComponents, designerPipes) {

    private val fileStorage: FileStorage = SimpleFileStorage()

    override val workflowConfig = WorkflowConfiguration(DesktopPlatformConfiguration().simulationFolder)

    override val componentPropertyView: ComponentPropertyView = DesktopComponentPropertyView()

    override fun saveToFile(file: File, content: String): Completable {
        return fileStorage.storeFile(file.absolutePath, content)
    }

    override fun openFile(file: File): Single<String?> {
        return fileStorage.loadFromFileAsString(file.absolutePath)
    }

    override fun startWorkflowPresenter(workflow: Workflow) {
        WorkflowPresenter(workflow, DesktopWorkflowView())
    }

}
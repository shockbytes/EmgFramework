package at.fhooe.mc.emg.desktop.core

import at.fhooe.mc.emg.clientdriver.EmgClientDriver
import at.fhooe.mc.emg.core.EmgPresenter
import at.fhooe.mc.emg.core.analysis.FrequencyAnalysisMethod
import at.fhooe.mc.emg.core.filter.Filter
import at.fhooe.mc.emg.core.storage.config.EmgConfigStorage
import at.fhooe.mc.emg.core.tool.Tool
import at.fhooe.mc.emg.core.view.VisualView
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.desktop.designer.DesktopDesignerPresenter
import at.fhooe.mc.emg.desktop.designer.view.DesktopDesignerView
import at.fhooe.mc.emg.desktop.view.DesktopEmgView
import at.fhooe.mc.emg.desktop.view.XChartVisualView
import java.io.File
import javax.swing.JComponent

/**
 * Author:  Martin Macheiner
 * Date:    22.11.2017
 *
 * The DesktopEmgPresenter is the main entry point for the Desktop solution. It provides a Swing-based
 * visual view representation and a desktop-ready implementation of the Acquisition Case Designer.
 *
 */
class DesktopEmgPresenter(c: List<EmgClientDriver>,
                          t: List<Tool>,
                          f: List<Filter>,
                          fam: List<FrequencyAnalysisMethod>,
                          ebc: Pair<List<EmgBaseComponent>, List<EmgComponentPipe<*,*>>>,
                          cs: EmgConfigStorage,
                          v: DesktopEmgView<JComponent>) : EmgPresenter(c, t, f, fam, ebc, cs, v) {

    override val visualView: VisualView<JComponent> = XChartVisualView()

    init {
        v.setVisualView(visualView)
    }

    override fun openAcquisitionCaseDesigner() {
        DesktopDesignerPresenter(DesktopDesignerView(), designerComponents.first, designerComponents.second).start()
    }

    override fun openAcquisitionCaseDesignerFile(file: File) {
        DesktopDesignerPresenter(DesktopDesignerView(), designerComponents.first, designerComponents.second).start(file)
    }


}
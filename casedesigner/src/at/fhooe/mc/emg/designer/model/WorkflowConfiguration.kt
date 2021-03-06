package at.fhooe.mc.emg.designer.model

import at.fhooe.mc.emg.designer.ComponentViewType
import at.fhooe.mc.emg.designer.view.WorkflowItemViewManager

class WorkflowConfiguration(val simulationDeviceFolder: String,
                            val itemViewManager: WorkflowItemViewManager,
                            val viewType: ComponentViewType)
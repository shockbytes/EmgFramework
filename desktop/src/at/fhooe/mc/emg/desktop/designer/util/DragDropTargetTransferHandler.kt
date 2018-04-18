package at.fhooe.mc.emg.desktop.designer.util

import javax.swing.TransferHandler

class DragDropTargetTransferHandler: TransferHandler() {

    override fun canImport(support: TransferSupport?): Boolean {
        return support?.dataFlavors?.contains(DragDropTransferHandler.COMPONENT_DATA_FLAVOR) ?: false
    }

    override fun importData(support: TransferSupport?): Boolean {
        return true
    }
}
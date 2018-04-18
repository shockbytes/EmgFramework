package at.fhooe.mc.emg.desktop.designer.util

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.desktop.designer.DesktopDesignerHelper
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.TransferHandler

class DragDropTransferHandler(private val emgComponent: EmgBaseComponent): TransferHandler() {

    init {
        dragImage = DesktopDesignerHelper.componentIcon(emgComponent)?.image
    }

    override fun createTransferable(c: JComponent?): Transferable {
        return EmgComponentTransferable(emgComponent)
    }

    override fun getSourceActions(c: JComponent?): Int {
        return COPY_OR_MOVE
    }

    class EmgComponentTransferable(private val emgComponent: EmgBaseComponent) : Transferable {

        override fun getTransferData(flavor: DataFlavor?) = emgComponent

        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
            return flavor?.equals(COMPONENT_DATA_FLAVOR) ?: false
        }

        override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(COMPONENT_DATA_FLAVOR)

    }

    companion object {
        val COMPONENT_DATA_FLAVOR = DataFlavor(EmgBaseComponent::class.java, "kotlin/emgComponent")
    }

}
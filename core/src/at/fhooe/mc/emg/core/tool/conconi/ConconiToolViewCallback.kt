package at.fhooe.mc.emg.core.tool.conconi

import at.fhooe.mc.emg.core.tool.ToolViewCallback
import io.reactivex.Single
import io.reactivex.functions.Consumer

/**
 * Author:  Mescht
 * Date:    08.07.2017
 */
interface ConconiToolViewCallback : ToolViewCallback {

    var useRunningAverageFilter: Boolean

    fun onStartClicked()

    fun onStopClicked()

    fun onSaveClicked(filename: String?, errorHandler: Consumer<Throwable>)

    fun onLoadClicked(filename: String?, errorHandler: Consumer<Throwable>)

    fun requestStoredConconiFiles(directory: String, concatToBase: Boolean): Single<List<String>?>

}

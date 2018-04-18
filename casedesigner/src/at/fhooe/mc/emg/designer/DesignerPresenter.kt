package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.draw.model.Origin
import at.fhooe.mc.emg.designer.util.GsonComponentDeserializer
import at.fhooe.mc.emg.designer.util.GsonComponentSerializer
import at.fhooe.mc.emg.designer.view.DesignerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File


/**
 * Author:  Martin Macheiner
 * Date:    16.04.2018
 */
abstract class DesignerPresenter(private val view: DesignerView,
                                 private val designerComponents: List<EmgBaseComponent>) : DesignerViewCallback {

    private val gson: Gson
    private var hasModelChanged = false
    private val interactionComponents: MutableList<EmgBaseComponent> = mutableListOf()

    init {
        gson = initializeGson()
    }

    abstract fun openFile(file: File): Single<String?>

    abstract fun saveToFile(file: File, content: String): Completable

    override fun open(file: File) {
        openFile(file)
                .map { json ->
                    gson.fromJson(json,
                            object : TypeToken<ArrayList<EmgBaseComponent>>() {}.type) as List<EmgBaseComponent>
                }
                .subscribe({ components ->
                    interactionComponents.clear()
                    interactionComponents.addAll(components)
                    updateView()
                }, { throwable ->
                    view.showStatusMessage("Cannot open file -> ${throwable.message}")
                })
    }

    override fun save(file: File) {
        val content = gson.toJson(interactionComponents)
        saveToFile(file, content).subscribe({
            view.showStatusMessage("File successfully stored in ${file.absolutePath}")
            hasModelChanged = false
        }, { throwable ->
            view.showStatusMessage("Cannot store file -> ${throwable.message}")
        })
    }

    override fun stop() {
        if (hasModelChanged) {
            val file = view.askForStorageBeforeQuit()
            if (file != null) {
                save(file)
            }
        }
        reset()
    }

    override fun reset() {
        interactionComponents.clear()
        updateView()
    }

    override fun run() {
        println("Run")
        // TODO Execute workflow
    }

    override fun addComponentByDoubleClick(component: EmgBaseComponent) {
        val c = component.copyWithOrigin(20, 20)
        interactionComponents.add(c)
        updateView()
    }

    override fun addComponent(component: EmgBaseComponent, x: Int, y: Int) {
        val c = component.copyWithOrigin(x, y)
        interactionComponents.add(c)
        updateView()
    }

    override fun removeComponent(component: EmgBaseComponent) {
        interactionComponents.remove(component)
        updateView()
    }

    override fun moveComponent(component: EmgBaseComponent, xNew: Int, yNew: Int) {
        interactionComponents
                .firstOrNull {
                    (it.name == component.name) && (it.origin == component.origin) }
                ?.let {
                    val idx = interactionComponents.indexOf(it)
                    it.origin = Origin(xNew, yNew)
                    interactionComponents[idx] = it

                    updateView()
                }
    }

    override fun connectComponents(component1: EmgBaseComponent, component2: EmgBaseComponent) {
        // TODO Connect components
    }

    fun start(file: File? = null) {
        view.show(this, designerComponents)
        if (file != null) {
            open(file)
        }
    }

    private fun updateView() {
        view.components = interactionComponents

        // Call this here, because an update always indicates that the model has changed since last time
        hasModelChanged = true
    }

    private fun initializeGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(EmgBaseComponent::class.java, GsonComponentSerializer())
                .registerTypeAdapter(EmgBaseComponent::class.java, GsonComponentDeserializer())
                .create()
    }

}
package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.internal.ConnectorComponent
import at.fhooe.mc.emg.designer.component.model.Origin
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.designer.model.Workflow
import at.fhooe.mc.emg.designer.util.GsonComponentDeserializer
import at.fhooe.mc.emg.designer.util.GsonComponentSerializer
import at.fhooe.mc.emg.designer.util.GsonSingleComponentSerializer
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
                                 private val designerComponents: List<EmgBaseComponent>,
                                 private val designerPipes: List<EmgComponentPipe<Any, Any>>) : DesignerViewCallback {

    private val gson: Gson
    private var hasModelChanged = false
    private val interactionComponents: MutableList<EmgBaseComponent> = mutableListOf()

    init {
        gson = initializeGson()
    }

    abstract fun openFile(file: File): Single<String?>

    abstract fun saveToFile(file: File, content: String): Completable

    abstract fun startWorkflowPresenter(workflow: Workflow)

    override fun open(file: File) {
        openFile(file)
                .map { json ->
                    renewConnectors(gson.fromJson(json,
                            object : TypeToken<ArrayList<EmgBaseComponent>>() {}.type) as List<EmgBaseComponent>)
                }
                .subscribe({ components ->
                    interactionComponents.clear()
                    interactionComponents.addAll(components)
                    updateView()
                    view.showStatusMessage("${file.absolutePath} loaded")
                }, { throwable ->
                    view.showStatusMessage("Cannot open file -> ${throwable.message}")
                })
    }

    override fun save(file: File) {
        val content = gson.toJson(interactionComponents,
                object : TypeToken<ArrayList<EmgBaseComponent>>() {}.type)
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

    override fun validate() {
        ComponentLogic.validate(interactionComponents, designerPipes).subscribe({
            view.showStatusMessage("Validation successful!")
        }, { throwable ->
            view.showStatusMessage("Validation error -> ${throwable.message}")
        })
    }

    override fun run() {
        ComponentLogic.build(interactionComponents, designerPipes).subscribe({ workflow ->
            view.showStatusMessage("Build successful!")
            startWorkflowPresenter(workflow)
        }, { throwable ->
            view.showStatusMessage("Build error -> ${throwable.message}")
            throwable.printStackTrace()
        })
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
                    (it.name == component.name) && (it.origin == component.origin)
                }
                ?.let {
                    it.origin = Origin(xNew, yNew)
                    interactionComponents[interactionComponents.indexOf(it)] = it
                    updateView()
                }
    }

    override fun connectComponents(component1: EmgBaseComponent, component2: EmgBaseComponent) {

        when (ComponentLogic.connect(component1, component2,
                interactionComponents.mapNotNull { it as? ConnectorComponent })) {

            ComponentLogic.ConnectionResult.NO_INPUT ->
                view.showStatusMessage("Component ${component2.name} has no input port!")
            ComponentLogic.ConnectionResult.NO_OUTPUT ->
                view.showStatusMessage("Component ${component1.name} has no output port!")
            ComponentLogic.ConnectionResult.SAME_ELEMENT ->
                view.showStatusMessage("Cannot connect same component -> ${component1.name}")
            ComponentLogic.ConnectionResult.INPUT_ALREADY_CONNECTED ->
                view.showStatusMessage("Component ${component2.name} is already connected!")
            ComponentLogic.ConnectionResult.GRANT -> { // Connect the components with a connector component
                interactionComponents.add(ConnectorComponent(component1, component2))
                updateView()
            }
        }
    }

    override fun showDetails(component: EmgBaseComponent) {
        println("show properties for ${component.name}!")
    }

    override fun drawBackground(draw: Boolean) {
        view.drawBackgroundForInteractionView(draw)
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
                .registerTypeAdapter(EmgBaseComponent::class.java, GsonSingleComponentSerializer())
                .create()
    }

    private fun renewConnectors(list: List<EmgBaseComponent>): List<EmgBaseComponent> {
        val renewed = list.filter { it !is ConnectorComponent }.toMutableList()
        return list.mapNotNull { it as? ConnectorComponent }
                .mapNotNullTo((renewed)) { c ->

                    val start = renewed.find { it == c.start }
                    val end = renewed.find { it == c.end }
                    if (start != null && end != null) {
                        ConnectorComponent(start, end)
                    } else {
                        null
                    }
                }
    }

}
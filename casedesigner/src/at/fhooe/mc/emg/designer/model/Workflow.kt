package at.fhooe.mc.emg.designer.model

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.EmgDeviceComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.designer.component.util.EmgComponentParameter
import at.fhooe.mc.emg.designer.util.ComponentInspection
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Author:  Martin Macheiner
 * Date:    26.04.2018
 *
 * Implementation for defining the workflow of the created acquisition case. This class
 * holds the components and the corresponding pipes, which will be used to execute the actual code.
 *
 */
class Workflow(private val flow: MutableList<WorkflowItem> = mutableListOf()) {

    private val startPoint: StartableProducer
        get() = flow.mapNotNull { it.producer as? StartableProducer }.first()

    private var isStarted: Boolean = false

    fun start() {

        if (!isStarted) {
            startPoint.entryPoint.invoke(startPoint.instance,
                    Action { println("Successfully connected") },
                    Consumer { throwable: Throwable ->
                        throwable.printStackTrace()
                        isStarted = false
                    })
            isStarted = true
        }
    }

    fun stop() {
        if (isStarted) {
            startPoint.exitPoint.invoke(startPoint.instance)
            isStarted = false
        }
    }

    fun release() {
        flow.clear()
    }

    // ------------------------------------------------------------------------------------------------

    class WorkflowItem(val producer: Producer,
                       val consumer: List<Consumer?>)

    class Consumer(val qualifiedName: String,
                   val instance: Any,
                   val method: Method,
                   val pipe: EmgComponentPipe<Any, Any>)

    open class Producer(val instance: Any,
                        val field: Field)

    class StartableProducer(instance: Any,
                            field: Field,
                            val entryPoint: Method,
                            val exitPoint: Method,
                            val startableType: StartableType) : Producer(instance, field)

    enum class StartableType {
        DEVICE, TOOL
    }

    class Builder(private val flowConfig: WorkflowConfiguration) {

        private val builderItems: MutableList<WorkflowItem> = mutableListOf()

        /**
         * Connect the producer and consumer via the pipe, before adding to list
         */
        fun addItem(item: WorkflowItem) {

            // Connect component instances
            (item.producer.field.get(item.producer.instance) as PublishSubject<*>).subscribe { data ->
                item.consumer.forEach { consumer ->
                    val transformed = consumer?.pipe?.pipe(data)
                    consumer?.method?.invoke(consumer.instance, transformed)
                }
            }
            // Add item to flow in the end
            builderItems.add(item)
        }

        fun producerOf(c: EmgBaseComponent, reused: Consumer? = null): Producer {

            // TODO Somehow check for ToolViews and EmgComponentPlatformViews and display them
            val field = ComponentInspection.getOutputPort(c)
            val instance: Any
            val producer =  if (c is EmgDeviceComponent) {
                instance = startProducerInstantiation(c, reused)
                val entryPoint = ComponentInspection.getDeviceEntryPoint(c)
                val exitPoint = ComponentInspection.getDeviceExitPoint(c)
                StartableProducer(instance, field, entryPoint, exitPoint, StartableType.DEVICE)
            } else {
                instance = reused?.instance ?: Class.forName(c.qualifiedName).newInstance()
                Producer(instance, field)
            }

            // Set components only if a fresh instance is created
            if (reused == null) {
                setComponentProperties(instance, c.parameter)
            }
            return producer
        }

        fun consumerOf(c: EmgBaseComponent, pipe: EmgComponentPipe<Any, Any>): Consumer {
            val instance = Class.forName(c.qualifiedName).newInstance()
            setComponentProperties(instance, c.parameter)
            val method = ComponentInspection.getInputPort(c)
            return Consumer(c.qualifiedName, instance, method, pipe)
        }

        fun build(): Workflow {
            return Workflow(builderItems)
        }

        private fun setComponentProperties(instance: Any, properties: List<EmgComponentParameter>) {
            properties.forEach {
                ComponentInspection.setProperty(instance, it.name, it.value, it.type)
            }
        }

        private fun startProducerInstantiation(component: EmgBaseComponent, reused: Consumer?): Any {

            if (reused != null) {
                return reused.instance
            }

            val dc = Class.forName(component.qualifiedName)
            return if (dc.name.contains("Simulation")) {
                // This constructor is always valid
                val constructor = dc.constructors.find { it.parameterCount == 2 }!!
                constructor.newInstance(null, flowConfig.simulationDeviceFolder)
            } else {
                dc.newInstance()
            }
        }
    }

    // ------------------------------------------------------------------------------------------------

}

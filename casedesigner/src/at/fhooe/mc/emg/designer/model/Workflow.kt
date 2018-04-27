package at.fhooe.mc.emg.designer.model

import at.fhooe.mc.emg.designer.annotation.EmgComponentEntryPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentExitPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.EmgDeviceComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.reflections.ReflectionUtils
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

    private val startPoint: StartProducer
        get() = flow.mapNotNull { it.producer as? StartProducer }.first()

    fun start() {

        startPoint.entryPoint.invoke(startPoint.instance,
                Action { println("Successfully connected") },
                Consumer { throwable: Throwable -> throwable.printStackTrace() })
    }

    fun stop() {
        startPoint.exitPoint.invoke(startPoint.instance)
    }


    class Builder {

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

        fun build(): Workflow {
            return Workflow(builderItems)
        }
    }

    class WorkflowItem(val producer: Producer, val consumer: List<Consumer?>)

    class Consumer(val qualifiedName: String, val instance: Any, val method: Method, val pipe: EmgComponentPipe<Any, Any>) {

        companion object {

            fun of(c: EmgBaseComponent, pipe: EmgComponentPipe<Any, Any>): Consumer {
                val instance = Class.forName(c.qualifiedName).newInstance()
                val method = ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                        ReflectionUtils.withAnnotation(EmgComponentInputPort::class.java)).first()
                return Consumer(c.qualifiedName, instance, method, pipe)
            }
        }

    }

    open class Producer(val instance: Any, val field: Field) {

        companion object {

            fun of(c: EmgBaseComponent, reused: Consumer? = null): Producer {
                val field = getOutputPort(c)
                return if (c is EmgDeviceComponent) {
                    val instance = startProducerInstantiation(c, reused)
                    val entryPoint = ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                            ReflectionUtils.withAnnotation(EmgComponentEntryPoint::class.java)).first()
                    val exitPoint = ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                            ReflectionUtils.withAnnotation(EmgComponentExitPoint::class.java)).first()
                    StartProducer(instance, field, entryPoint, exitPoint)
                } else {
                    val instance = reused?.instance ?: Class.forName(c.qualifiedName).newInstance()
                    Producer(instance, field)
                }
            }
        }
    }

    class StartProducer(instance: Any, field: Field,
                        val entryPoint: Method, val exitPoint: Method) : Producer(instance, field)


    // TODO REMOVE THIS ASAP, BECAUSE ThIS CODE IS PART OF THE INJECTION PACKAGE!!!
    companion object {

        val SIMULATION_DRIVER_DESKTOP_PATH: String = System.getProperty("user.dir") + "/data/simulation"


        fun getOutputPort(c: EmgBaseComponent): Field {

            // TODO Handle the RelayPorts
            // Check in super class if in concrete class no output port is defined
            return ReflectionUtils.getFields(Class.forName(c.qualifiedName),
                    ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java)).firstOrNull()
                    ?: ReflectionUtils.getSuperTypes(Class.forName(c.qualifiedName))
                            .mapNotNull { cls ->
                                ReflectionUtils
                                        .getFields(cls, ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java))
                                        .firstOrNull()
                            }.first()
        }

        fun startProducerInstantiation(component: EmgBaseComponent, reused: Consumer?): Any {

            if (reused != null) {
                return reused.instance
            }

            val dc = Class.forName(component.qualifiedName)
            return if (dc.name.contains("Simulation")) {
                val constructor = dc.getConstructor(String::class.java)
                constructor.newInstance(SIMULATION_DRIVER_DESKTOP_PATH)
            } else {
                dc.newInstance()
            }
        }

    }
}

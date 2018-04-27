package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.annotation.EmgComponentEntryPoint
import at.fhooe.mc.emg.designer.annotation.EmgComponentInputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentOutputPort
import at.fhooe.mc.emg.designer.annotation.EmgComponentRelayPort
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.EmgDeviceComponent
import at.fhooe.mc.emg.designer.component.internal.ConnectorComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.designer.model.Workflow
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.reflections.ReflectionUtils
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

object ComponentLogic {

    enum class ConnectionResult {
        NO_INPUT, NO_OUTPUT, SAME_ELEMENT, INPUT_ALREADY_CONNECTED, GRANT
    }

    class ValidationException(message: String) : Exception(message)

    fun connect(component1: EmgBaseComponent,
                component2: EmgBaseComponent,
                connectors: List<ConnectorComponent>): ConnectionResult {

        // Components are the same
        if (component1 == component2) {
            return ConnectionResult.SAME_ELEMENT
        }
        // Component 1 has no output port
        if (!component1.portConfiguration.second) {
            return ConnectionResult.NO_OUTPUT
        }
        // Component 2 has no input port
        if (!component2.portConfiguration.first) {
            return ConnectionResult.NO_INPUT
        }
        // Component 2 has already another connection at the input port
        if (connectors.find { it.end == component2 } != null) {
            return ConnectionResult.INPUT_ALREADY_CONNECTED
        }
        return ConnectionResult.GRANT
    }

    fun validate(components: List<EmgBaseComponent>, pipes: List<EmgComponentPipe<*, *>>): Completable {
        // Perform validation checks
        return Completable.fromCallable {
            checkForDriverComponent(components)
            checkComponentPorts(components)
            checkComponentConnections(components, pipes)
        }
    }

    fun build(components: List<EmgBaseComponent>, pipes: List<EmgComponentPipe<Any, Any>>): Single<Workflow> {
        return Single.fromCallable {

            // Throws an error if not validated
            validate(components, pipes).blockingGet()

            val connectors = components.mapNotNull { it as? ConnectorComponent }
            buildWorkflow(connectors, pipes)
        }
    }

    @Throws(ValidationException::class)
    private fun checkComponentConnections(components: List<EmgBaseComponent>,
                                          pipes: List<EmgComponentPipe<*, *>>) {

        components
                .mapNotNull { it as? ConnectorComponent }
                .forEach {

                    val (consumes, _, _) = getPortConnectivityInformation(it.end)
                    val (_, produces, _) = getPortConnectivityInformation(it.start)

                    // Find a suiting pipe for the port types,
                    // if nothing found throw an ValidationException
                    pipes.find {
                        val (input, output) = it.ports
                        (input == produces) && (output == consumes)
                    } ?: throw ValidationException("No suitable pipe for connecting $consumes with " +
                            "$produces (cause by ${it.start.name} and ${it.end.name})")

                }
    }

    @Throws(ValidationException::class)
    private fun checkComponentPorts(components: List<EmgBaseComponent>) {

        components
                .filter { it !is ConnectorComponent }
                .forEach { c ->

                    val (inputEnabled, outputEnabled) = c.portConfiguration
                    val (consumes, produces, _) = getPortConnectivityInformation(c)

                    // Check input/output port configuration
                    if (inputEnabled && consumes == null) {
                        throw ValidationException("Input cannot be enabled without explicitly annotating input method - (${c.qualifiedName})")
                    }
                    if (outputEnabled && produces == null) {
                        throw ValidationException("Output cannot be enabled without explicitly annotating output field - (${c.qualifiedName})")
                    }
                }
    }

    @Throws(ValidationException::class)
    private fun checkForDriverComponent(components: List<EmgBaseComponent>) {
        // Check for at least 1 driver
        if (components.none { it is EmgDeviceComponent }) {
            throw ValidationException("No device component defined!")
        }

        // Check for valid entry point
        components
                .mapNotNull { it as? EmgDeviceComponent }
                .forEach { c ->
                    ReflectionUtils
                            .getMethods(Class.forName(c.qualifiedName), ReflectionUtils.withAnnotation(EmgComponentEntryPoint::class.java))
                            .firstOrNull()
                            ?: throw ValidationException("Device component ${c.name} does not provide a valid entry point")
                }
    }

    /**
     *
     * The method extracts the consumer type, producer type and the usage of a relay port from the given component
     *
     * @param c Component of interest
     *
     * @return a Triple of format (consumes, produces, isRelayPort)
     */
    private fun getPortConnectivityInformation(c: EmgBaseComponent): Triple<KClass<*>?, KClass<*>?, Boolean> {

        val clazz = Class.forName(c.qualifiedName)
        val inputPort = ReflectionUtils.getMethods(clazz,
                ReflectionUtils.withAnnotation(EmgComponentInputPort::class.java)).firstOrNull()
        var outputPort = ReflectionUtils.getFields(clazz,
                ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java)).firstOrNull()
        val relayPort = ReflectionUtils.getMethods(clazz,
                ReflectionUtils.withAnnotation(EmgComponentRelayPort::class.java)).firstOrNull()

        // Check in super class if in concrete class no output port is defined
        if (outputPort == null && relayPort == null) {
            outputPort = ReflectionUtils.getSuperTypes(Class.forName(c.qualifiedName))
                    .mapNotNull { cls ->
                        ReflectionUtils
                                .getFields(cls, ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java))
                                .firstOrNull()
                    }.firstOrNull()
        }

        // Extra check if output port has the right signature
        if (outputPort != null) {
            if (Modifier.isPrivate(outputPort.modifiers) || (outputPort.type == PublishSubject::class)) {
                throw ValidationException("Output port in ${outputPort.declaringClass} is not from type PublishSubject or public")
            }
        }

        val consumes: KClass<*>?
        val produces: KClass<*>?
        if (relayPort != null) {
            val relay = relayPort.annotations
                    ?.find { a -> a.annotationClass == EmgComponentRelayPort::class }
                    .let { (it as? EmgComponentRelayPort) }
            consumes = relay?.consumes
            produces = relay?.produces
        } else {
            consumes = inputPort?.annotations
                    ?.find { a -> a.annotationClass == EmgComponentInputPort::class }
                    .let { (it as? EmgComponentInputPort)?.consumes }
            produces = outputPort?.annotations
                    ?.find { a -> a.annotationClass == EmgComponentOutputPort::class }
                    .let { (it as? EmgComponentOutputPort)?.produces }
        }
        return Triple(consumes, produces, relayPort != null)
    }

    private fun buildWorkflow(connectors: List<ConnectorComponent>,
                              pipes: List<EmgComponentPipe<Any, Any>>): Workflow {

        val workflowBuilder = Workflow.Builder()
        val relayComponentList: MutableList<Workflow.Consumer> = mutableListOf()
        // TODO Replace relayport components with a pipe
        connectors
                .groupBy { it.start } // Group output components by same input component
                .forEach { map ->

                    val start = map.key
                    val endpoints = map.value

                    // Create producer and consumer and corresponding pipes
                    // A producer instance is already stored inside a consumer instance --> Reuse it, otherwise workflow is not working
                    val reusedConsumer = relayComponentList.find { it.qualifiedName == start.qualifiedName }
                    val producer = if (reusedConsumer != null) {
                        Workflow.Producer.of(start, reusedConsumer)
                    } else {
                        Workflow.Producer.of(start)
                    }

                    val (_, _, hasRelayPort) = getPortConnectivityInformation(start)
                    val consumer = endpoints.map {
                        val (pipe, hasOutput) = findSuitablePipe(start, it.end, pipes)
                        val consumer = Workflow.Consumer.of(it.end, pipe)
                        if (hasOutput) {
                            relayComponentList.add(consumer)
                        }
                        consumer
                    }
                    // Let the workflow object wire them together
                    workflowBuilder.addItem(Workflow.WorkflowItem(producer, consumer))
                }
        return workflowBuilder.build()
    }

    /**
     * @return The suitable pipe and a boolean which indicates if the consumer is also a producer, wrapped inside a Pair
     */
    private fun findSuitablePipe(start: EmgBaseComponent,
                                 end: EmgBaseComponent,
                                 pipes: List<EmgComponentPipe<Any, Any>>): Pair<EmgComponentPipe<Any, Any>, Boolean> {

        val (_, startProduces, _) = getPortConnectivityInformation(start)
        val (endConsumes, endProduces, _) = getPortConnectivityInformation(end)
        val hasOutput = endProduces != null

        val pipe = pipes.find {
            val (pipeConsumes, pipeProduces) = it.ports
            (startProduces == pipeConsumes) && (pipeProduces == endConsumes)
        } ?: throw IllegalArgumentException("There is no valid pipe for ${start.name} / ${end.name} ")

        return Pair(pipe, hasOutput)
    }

}
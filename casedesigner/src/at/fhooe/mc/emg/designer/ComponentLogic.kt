package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.annotation.EmgComponentEntryPoint
import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.EmgDeviceComponent
import at.fhooe.mc.emg.designer.component.internal.ConnectorComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import at.fhooe.mc.emg.designer.model.Workflow
import at.fhooe.mc.emg.designer.model.WorkflowConfiguration
import at.fhooe.mc.emg.designer.util.ComponentInspection
import at.fhooe.mc.emg.designer.util.ComponentInspection.getPortConnectivityInformation
import io.reactivex.Completable
import io.reactivex.Single
import org.reflections.ReflectionUtils

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

    fun build(components: List<EmgBaseComponent>,
              pipes: List<EmgComponentPipe<Any, Any>>,
              workflowConfig: WorkflowConfiguration): Single<Workflow> {
        return Single.fromCallable {

            // Throws an error if not validated
            validate(components, pipes).blockingGet()

            val connectors = components.mapNotNull { it as? ConnectorComponent }
            buildWorkflow(connectors, pipes, workflowConfig)
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

    private fun buildWorkflow(connectors: List<ConnectorComponent>,
                              pipes: List<EmgComponentPipe<Any, Any>>,
                              workflowConfig: WorkflowConfiguration): Workflow {

        val workflowBuilder = Workflow.Builder(workflowConfig)
        val relayComponentList: MutableList<Workflow.Consumer> = mutableListOf()

        // TODO Squash relay ports into adjacent consumer
        connectors
                .groupBy { it.start } // Group output components by same input component
                .forEach { map ->

                    val start = map.key
                    val endpoints = map.value

                    // Create producer and consumer and corresponding pipes
                    // A producer instance is already stored inside a consumer instance --> Reuse it, otherwise workflow is not working
                    val reusedConsumer = relayComponentList.find { it.qualifiedName == start.qualifiedName }
                    val producer = workflowBuilder.producerOf(start, reusedConsumer)

                    val (_, _, hasRelayPort) = ComponentInspection.getPortConnectivityInformation(start)
                    val consumer = endpoints.map {
                        val (pipe, hasOutput) = findSuitablePipe(start, it.end, pipes)
                        val consumer = workflowBuilder.consumerOf(it.end, pipe)
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
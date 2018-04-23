package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.EmgDeviceComponent
import at.fhooe.mc.emg.designer.component.internal.ConnectorComponent
import at.fhooe.mc.emg.designer.component.pipe.EmgComponentPipe
import io.reactivex.Single
import org.reflections.ReflectionUtils
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

    fun validate(components: List<EmgBaseComponent>, pipes: List<EmgComponentPipe<*, *>>): Single<Boolean> {
        return Single.fromCallable {
            // Perform validation checks
            checkForDriverComponent(components)
            checkComponentPorts(components)
            checkComponentConnections(components, pipes)
            true
        }
    }

    fun run(components: List<EmgBaseComponent>, pipes: List<EmgComponentPipe<*, *>>): Single<Boolean> {
        return Single.fromCallable {

            // Throws an error if not valid
            validate(components, pipes).blockingGet()

            // TODO Execute run logic
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

        val inputPort = ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentInputPort::class.java)).firstOrNull()
        var outputPort = ReflectionUtils.getFields(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java)).firstOrNull()
        val relayPort = ReflectionUtils.getMethods(Class.forName(c.qualifiedName),
                ReflectionUtils.withAnnotation(EmgComponentRelayPort::class.java)).firstOrNull()

        if (outputPort == null && relayPort == null) {
           outputPort = ReflectionUtils.getSuperTypes(Class.forName(c.qualifiedName))
                   .mapNotNull { cls ->
                       ReflectionUtils
                               .getFields(cls, ReflectionUtils.withAnnotation(EmgComponentOutputPort::class.java))
                               .firstOrNull()
                   }.firstOrNull()
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

}
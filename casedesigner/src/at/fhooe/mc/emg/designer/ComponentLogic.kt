package at.fhooe.mc.emg.designer

import at.fhooe.mc.emg.designer.component.EmgBaseComponent
import at.fhooe.mc.emg.designer.component.internal.ConnectorComponent
import io.reactivex.Single

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

    // TODO Return some kind of tree here
    @Throws(ValidationException::class)
    fun build(components: List<EmgBaseComponent>): Single<Boolean> {
        throw ValidationException("Validation function not implemented yet...")
    }
}
package at.fhooe.mc.emg.designer

/**
 *
 * <li> FILTER:         Filter components have an input and an output port, they filter the signal
 * <li> SOURCE:         Client-side device, not utilized at the moment
 * <li> SINK:           Sink components provide display functionality and have no output port
 * <li> RELAY_SINK:     Sink components which provide additionally an output value and an output port
 * <li> TOOL:           Complex computation components with no output port, use RELAY_SINK if tool provides an output value
 * <li> DEVICE:         Driver component for interacting with client devices
 *
 */
enum class EmgComponentType {
    FILTER, SOURCE, SINK, RELAY_SINK, TOOL, DEVICE
}
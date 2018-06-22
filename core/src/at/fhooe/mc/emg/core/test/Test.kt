package at.fhooe.mc.emg.core.test

class Test(val subject: TestSubject, val properties: MutableMap<String, String>) {

    fun putProperty(key: String, p: Property) {
        properties[key] = p.value.toString()
    }

    data class Property(val value: Any, val type: DataType)

    enum class DataType {
        NOMINAL, ORDINAL, INTERVAL, RATIO, NA
    }

}
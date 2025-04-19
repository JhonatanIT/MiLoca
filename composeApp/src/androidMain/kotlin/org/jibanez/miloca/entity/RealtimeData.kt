package org.jibanez.miloca.entity

data class RealtimeData(
    var acceleration: AccelerationType = AccelerationType.STOP,
    var flat: FlatType = FlatType.TILTED,
    var orientation: OrientationType = OrientationType.PORTRAIT,
    var isSafe: Boolean = true,
    var light: LightType = LightType.NORMAL,
    var location: LocationData? = LocationData(),
    var timestamp: Long = System.currentTimeMillis()
)

data class LocationData(
    val latitude: Double = 0.0, val longitude: Double = 0.0, val altitude: Double = 0.0
)

enum class AccelerationType(val value: String) {
    STOP("stopped"),
    WALKING("walking"),
    RUNNING("running"),
    TRANSPORT("on transport")
}

enum class FlatType(val value: String) {
    UP("screen up"),
    DOWN("screen down"),
    TILTED("tilted")
}

enum class OrientationType(val value: String) {
    LANDSCAPE("landscape"),
    PORTRAIT("portrait"),
    TILTED("tilted")
}

enum class LightType(val value: String) {
    VERY_DARK("very dark"),
    DARK("dark"),
    DIM("dim"),
    NORMAL("normal"),
    BRIGHT("bright"),
    VERY_BRIGHT("very bright")
}

enum class SensorType(val value: String) {
    ACCELERATION("Acceleration"),
    FLAT("Flat"),
    ORIENTATION("Orientation"),
    LIGHT("Light")
}


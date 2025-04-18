package org.jibanez.miloca.entity

data class RealtimeData(
    var acceleration: AccelerationType = AccelerationType.STOP,
    var flat: FlatType = FlatType.TILTED,
    var orientation: OrientationType = OrientationType.PORTRAIT,
    var isSafe: Boolean = true,
    var light: LightType = LightType.NORMAL,
    var location: Location? = Location(),
    var timestamp: Long = System.currentTimeMillis()
)

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0
)

enum class AccelerationType {
    STOP, WALKING, RUNNING, TRANSPORT
}

enum class FlatType {
    UP, DOWN, TILTED
}

enum class OrientationType {
    LANDSCAPE, PORTRAIT, TILTED
}

enum class LightType {
    DARK, DIM, NORMAL, BRIGHT
}

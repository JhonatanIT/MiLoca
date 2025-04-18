package org.jibanez.miloca.service.sensor

import org.jibanez.miloca.entity.AccelerationType
import org.jibanez.miloca.entity.FlatType
import org.jibanez.miloca.entity.LightType
import org.jibanez.miloca.entity.OrientationType

object SensorDataProcessor {
    fun processAcceleration(magnitude: Float): AccelerationType = when {
        magnitude < 1f -> AccelerationType.STOP
        magnitude < 8f -> AccelerationType.WALKING
        magnitude < 50f -> AccelerationType.RUNNING
        else -> AccelerationType.TRANSPORT
    }

    fun processFlat(z: Float): FlatType = when {
        z > 7f -> FlatType.UP
        z < -7f -> FlatType.DOWN
        else -> FlatType.TILTED
    }

    fun processOrientation(x: Float, y: Float): OrientationType = when {
        x > 3f || x < -3f -> OrientationType.LANDSCAPE
        y > 3f || y < -3f -> OrientationType.PORTRAIT
        else -> OrientationType.TILTED
    }

    fun processLight(lux: Float): LightType = when {
        lux <= 3f -> LightType.VERY_DARK
        lux <= 50f -> LightType.DARK
        lux <= 200f -> LightType.DIM
        lux <= 500f -> LightType.NORMAL
        lux <= 1000f -> LightType.BRIGHT
        else -> LightType.VERY_BRIGHT
    }
}
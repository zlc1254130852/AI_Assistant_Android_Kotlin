package com.example.AI_Assistant

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Window


class NightModeSwitch(context: Context, window: Window) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    var window = window

    init {
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {

        if (event.values[0] < 30.0f) {
            enableNightMode()
        } else {
            disableNightMode()
        }
    }

    private fun enableNightMode() {
        window.setBackgroundDrawableResource(R.color.colorAccent)
    }

    private fun disableNightMode() {
        window.setBackgroundDrawableResource(R.color.colorPrimary)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // 当传感器的精度改变时的处理
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }
}

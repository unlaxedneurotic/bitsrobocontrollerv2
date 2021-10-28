package com.example.robocontrollerv2

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.example.robocontrollerv2.databinding.ActivityGyroControllerBinding
import com.example.robocontrollerv2.databinding.ActivityGyroTempControllerBinding
import com.google.android.material.slider.Slider
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GyroTempControllerActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager:SensorManager
    private var accelerometer: Sensor ?= null
    private lateinit var square:TextView
    private lateinit var binding:ActivityGyroTempControllerBinding
    private val myController = GyroController()

    var numberOfMessages = 0
    var requiredWaitTime = 100
    var lastSaved = System.currentTimeMillis()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGyroTempControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        square = binding.myView
        binding.maxSpeedVal.text="%.2f".format(myController.max_speed)
        setUpSensor()
    }

    public fun incrementMaxSpeed(view: View){
        myController.max_speed+=0.05
        binding.maxSpeedVal.text = "%.2f".format(myController.max_speed)
    }

    public fun decrementMaxSpeed(view:View){
        if(myController.max_speed<=0.0) return
        myController.max_speed-=0.05
        binding.maxSpeedVal.text = "%.2f".format(myController.max_speed)
    }

    private fun setUpSensor(){
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also{
            sensorManager.registerListener(this,it,SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            var sides = event.values[0]
            var updown =event.values[1] * -1

            if (updown > 6) updown = 6f
            if (updown < -6) updown = -6f
            if (sides > 6) sides = 6f
            if (sides < -6) sides = -6f
            if (sides > -1 && sides < 1) sides = 0f
            if (updown > -1 && updown < 1) updown = 0f

            if (sides >= 1) sides = (sides-1) / 5f
            if (sides <= -1) sides = (sides+1) / 5f
            if (updown >= 1) updown = (updown-1) / 5f
            if (updown <= -1) updown = (updown+1) / 5f

//            square.apply {
//                rotationX = updown.toFloat() * -1
//                rotationY = sides.toFloat()
//                rotation = -sides.toFloat()
//                translationX = sides * -1f
//                translationY = updown.toFloat() * -1
//            }
//            val updown_per = updown / 15f * 100
//            val sides_per = sides / 15f * 100

            square.text = "updown: %.2f\nsides: %.2f\nMessages Sent: $numberOfMessages".format(
                updown,
                sides
            )
//                square.text = "updown: $updown\nsides: $sides"

            if(System.currentTimeMillis()-lastSaved>requiredWaitTime) {
                lastSaved = System.currentTimeMillis()
                numberOfMessages++
                myController.changeSpeedAndDirection(updown, sides)
                MyGlobal.globalConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
            }
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onDestroy() {
        myController.changeSpeedAndDirection(0f,0f)
        MyGlobal.globalConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}
class GyroController(){
    var max_speed:Double = 0.1
    var speed:Double = 0.0
    var displaystring:String = "Stop"
    val twistdata = TwistMessage()
    var publish_topic:String = MyGlobal.globalConnector.publishtopic

    companion object{
        const val TAG = "GyroController"
    }
    fun changeSpeedAndDirection(x:Float, z:Float){

        for(i in 0..2){
            twistdata.linear[i] = 0.0
            twistdata.angular[i] = 0.0
        }

        if(max_speed<0) return

        twistdata.linear[0] = x*max_speed
        twistdata.angular[2] = z*max_speed
        if(x<0) twistdata.angular[2] *= -1.0 //correction for direction
        Log.i(GyroController.TAG, "x: $x, z: $z")

//        displaystring = when(mode){
//            0-> "Stop"
//            1-> "Forward"
//            2-> "Strafe Right"
//            3-> "Rotate Right"
//            4-> "Reverse Strafe Right"
//            5-> "Reverse"
//            6-> "Reverse Strafe Left"
//            7-> "Rotate Left"
//            8-> "Strafe Left"
//            else-> "Invalid"
//        }


        displaystring+= " Speed: %.2f".format(speed)
    }

    fun generateJSONMessage(): String {
        return """
            {"twist_message": 
             {
              "linear" : { 
                "x":${twistdata.linear[0]},
                "y":${twistdata.linear[1]},
                "z":${twistdata.linear[2]}
               },
              "angular":{
                "x":${twistdata.angular[0]},
                "y":${twistdata.angular[1]},
                "z":${twistdata.angular[2]}
              }
             }
            }""".trimIndent()
    }
}
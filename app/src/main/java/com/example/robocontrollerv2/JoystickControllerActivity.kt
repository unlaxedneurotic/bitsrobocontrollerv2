package com.example.robocontrollerv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.robocontrollerv2.databinding.ActivityJoystickControllerBinding
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class JoystickControllerActivity : AppCompatActivity() {

    companion object{
        const val TAG = "JOYSTICK"
    }
    private lateinit var binding: ActivityJoystickControllerBinding
    private lateinit var myJoystick:JoystickView
    private val myController = JoystickController()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoystickControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myJoystick = binding.joystick
        myJoystick.setOnMoveListener(object:JoystickView.OnMoveListener{
            override fun onMove(angle: Int, strength: Int) {
//                Log.i(TAG,"Angle: $angle, Strength: $strength")
                if(strength<10){
                    myJoystick.resetButtonPosition()
                    myController.changeSpeedAndDirection(0,0)
                    binding.valDisplay.text = "Strength: 0%"
                }
                else {
                    myController.changeSpeedAndDirection(angle, strength)
                    binding.valDisplay.text = "Strength: $strength%"
                }
                MyGlobal.globalConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
            }
        }, 1000)
        binding.maxSpeedVal.text="%.2f".format(myController.max_speed)
    }
    public fun incrementMaxSpeed(view: View){
        myController.max_speed+=0.05
        binding.maxSpeedVal.text = "%.2f".format(myController.max_speed)
        myController.changeSpeedAndDirection()
        MyGlobal.globalConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
    }

    public fun decrementMaxSpeed(view: View){
        if(myController.max_speed<=0.0) return
        myController.max_speed-=0.05
        binding.maxSpeedVal.text = "%.2f".format(myController.max_speed)
        myController.changeSpeedAndDirection()
        MyGlobal.globalConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
    }
    override fun onDestroy() {
        myController.changeSpeedAndDirection(0,0)
        MyGlobal.globalConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
        super.onDestroy()
    }
}

class JoystickController(){
    var max_speed:Double = 0.1
    var angle:Int =0
    var strength:Int=0
    val twistdata = TwistMessage()
    var publish_topic:String = MyGlobal.globalConnector.publishtopic

    fun changeSpeedAndDirection(new_angle:Int=angle, new_strength:Int=strength){
        angle=new_angle
        strength=new_strength

        for(i in 0..2){
            twistdata.linear[i] = 0.0
            twistdata.angular[i] = 0.0
        }
        val speed = max_speed*new_strength/100
        val adjustedangle = (new_angle+270)%360
        Log.i(JoystickControllerActivity.TAG,"Angle: $new_angle, Adjusted: $adjustedangle, Strength: $new_strength")
        twistdata.linear[0] = cos(adjustedangle.toDouble()* PI/180)*speed
        twistdata.angular[2] = sin(adjustedangle.toDouble()* PI/180)*speed
        if(twistdata.linear[0]<0) twistdata.angular[2] *= -1.0 //correction for direction

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

package com.example.robocontrollerv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.robocontrollerv2.databinding.ActivityButtonControllerBinding
import com.google.android.material.slider.Slider

class ButtonControllerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityButtonControllerBinding
    private val myController = ButtonController()
    private val myConnector = MyGlobal.globalConnector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityButtonControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.speedSlider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                // Responds to when slider's touch event is being started
            }

            override fun onStopTrackingTouch(slider: Slider) {
                myController.changeSpeedAndDirection(new_speed = slider.value.toDouble())
                binding.speedDisplay.text = myController.displaystring
                myConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
            }
        })
    }

    public fun changeDirection(v: View) {
        when (v.id) {
            binding.buttonStop.id -> myController.changeSpeedAndDirection(0)
            binding.buttonForw.id -> myController.changeSpeedAndDirection(1)
            binding.buttonStrafeForwRight.id -> myController.changeSpeedAndDirection(2)
            binding.buttonRight.id -> myController.changeSpeedAndDirection(3)
            binding.buttonStrafeBackRight.id -> myController.changeSpeedAndDirection(4)
            binding.buttonBack.id -> myController.changeSpeedAndDirection(5)
            binding.buttonStrafeBackLeft.id -> myController.changeSpeedAndDirection(6)
            binding.buttonLeft.id -> myController.changeSpeedAndDirection(7)
            binding.buttonStrafeForwLeft.id -> myController.changeSpeedAndDirection(8)
        }
        myConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
        binding.speedDisplay.text = myController.displaystring
    }
    public fun incrementMaxSpeed(view: View){
        myController.max_speed+=0.05
        binding.maxSpeedVal.text = "%.2f".format(myController.max_speed)
        myController.changeSpeedAndDirection()
        binding.speedDisplay.text = myController.displaystring
        myConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
    }

    public fun decrementMaxSpeed(view:View){
        if(myController.max_speed<=0.0) return
        myController.max_speed-=0.05
        binding.maxSpeedVal.text = "%.2f".format(myController.max_speed)
        myController.changeSpeedAndDirection()
        binding.speedDisplay.text = myController.displaystring
        myConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
    }
    override fun onDestroy() {
        myController.changeSpeedAndDirection(mode=0)
        MyGlobal.globalConnector.sendData(myController.publish_topic, myController.generateJSONMessage())
        super.onDestroy()
    }
}
class ButtonController(){
    var max_speed:Double = 0.1
    var speed:Double = 0.0
    var displaystring:String = "Stop"
    val twistdata = TwistMessage()
    var current_mode = 0
    var publish_topic:String = MyGlobal.globalConnector.publishtopic

    fun changeSpeedAndDirection(mode:Int = current_mode, new_speed:Double = speed){
//        For a tele_op_twist like keyboard, considering stop as 0
//        Forward button as 1 and counting rest in a clockwise fashion
        speed=new_speed
        current_mode = mode
        for(i in 0..2){
            twistdata.linear[i] = 0.0
            twistdata.angular[i] = 0.0
        }
        displaystring = when(mode){
            0-> "Stop"
            1-> "Forward"
            2-> "Strafe Right"
            3-> "Rotate Right"
            4-> "Reverse Strafe Right"
            5-> "Reverse"
            6-> "Reverse Strafe Left"
            7-> "Rotate Left"
            8-> "Strafe Left"
            else-> "Invalid"
        }
        when(mode){
            1-> twistdata.linear[0] = speed*max_speed
            2-> {
                twistdata.linear[0] = speed*max_speed
                twistdata.angular[2] = -1 * speed*max_speed
            }
            3 -> twistdata.angular[2] = -1 * speed*max_speed
            4-> {
                twistdata.linear[0] = -1*speed*max_speed
                twistdata.angular[2] = speed*max_speed
            }
            5-> twistdata.linear[0] = speed*-1*max_speed
            6-> {
                twistdata.linear[0] = speed*-1*max_speed
                twistdata.angular[2] = speed*-1*max_speed
            }
            7-> twistdata.angular[2] = speed*max_speed
            8-> {
                twistdata.linear[0] = speed*max_speed
                twistdata.angular[2] = speed*max_speed
            }
        }
        displaystring+= " Speed: %.2f".format(speed*max_speed)
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

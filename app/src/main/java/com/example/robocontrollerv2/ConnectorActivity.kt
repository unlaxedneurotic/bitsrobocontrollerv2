package com.example.robocontrollerv2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.example.robocontrollerv2.databinding.ActivityConnectorBinding

const val CONNECTOR_OBJECT = "CONNECTOR_OBJECT"
class ConnectorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectorBinding
    private lateinit var appContext: Context
    private lateinit var myConnector:Connector
    private var controllerMode:String = "button"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appContext = applicationContext
        MyGlobal.globalConnector = Connector(appContext)
        myConnector = MyGlobal.globalConnector
        binding.proceedButton.setOnClickListener{ goToController() }
        binding.connectButton.setOnClickListener{ initiateConnection() }
        binding.disconnectButton.setOnClickListener{ closeConnection() }
    }
    fun onChoiceSelect(view: View){
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_button ->
                    if (checked) {
                        controllerMode = "button"
                    }
                R.id.radio_gyro ->
                    if (checked) {
                        controllerMode = "gyro"
                    }
                R.id.radio_joystick ->
                    if(checked){
                        controllerMode = "joystick"
                    }
            }
        }
    }
    private fun setStatusDisplay(newStatus:String){
        binding.statusDisplay.text = newStatus
    }

    private fun initiateConnection(){

        if(myConnector.isConnected()){
            setStatusDisplay("Disconnect before trying again")
            return
        }
        setStatusDisplay("Connecting")
        val host = binding.hostText.editText!!.text.toString()
//        println("url: $url")
        val port_text = binding.portNumText.editText!!.text.toString()
        println("port:$port_text")
        val port = if(port_text!="") port_text.toInt() else -1
        if (host != ""){
            if(port<0) myConnector.connectToBroker(host = host,update_func = this::updateConnectionStatus)
            else myConnector.connectToBroker(host = host,port=port,update_func = this::updateConnectionStatus)
        }
        else myConnector.connectToBroker(update_func = this::updateConnectionStatus)
        val topic = binding.topicText.editText!!.text.toString()
        if(topic!=""){
            myConnector.publishtopic = topic
        }
        else {
            myConnector.publishtopic = "robobits/test"
        }
    }

    private fun closeConnection(){
        MyGlobal.globalConnector.disconnectFromBroker()
        setStatusDisplay("Connection Closed")
    }

    private fun updateConnectionStatus(status:String){
        setStatusDisplay(status)
    }

    private fun goToController(){
        if(controllerMode=="button") intent = Intent(this, ButtonControllerActivity::class.java)
        if(controllerMode=="gyro") intent = Intent(this, GyroTempControllerActivity::class.java)
        if(controllerMode=="joystick") intent = Intent(this, JoystickControllerActivity::class.java)
        startActivity(intent)
    }



}
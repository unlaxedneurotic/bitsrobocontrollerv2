package com.example.robocontrollerv2

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.Serializable

class Connector (private val context:Context){
    private var mqttClient: MqttAndroidClient? = null
    var publishtopic = "robobits/test"

    companion object{
        const val TAG = "MQTT CLIENT"
    }

    fun isConnected():Boolean{
        if(mqttClient!=null){
            return mqttClient!!.isConnected
        }
        return false
    }

    fun connectToBroker(host:String = "broker.hivemq.com", port:Int = 1883, update_func:(String)->Unit){
        if(mqttClient!=null){
            if(mqttClient!!.isConnected) return
        }
        val serverURL = "tcp://$host:$port"
        Log.i(TAG,"Connecting to URL: $serverURL")
        update_func("Connecting to URL: $serverURL")
        mqttClient = MqttAndroidClient(context, serverURL, "Robo Client")
        mqttClient!!.setCallback(object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "MESSAGE RECEIVED")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "Connection lost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {

            }
        })
        val options = MqttConnectOptions()
        options.connectionTimeout = 60
        try{
            mqttClient!!.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    update_func("Connected to $serverURL")
                    Log.d(TAG, "Connection Success")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    update_func("Connection Failed")
                    Log.d(TAG, "Connection Failure ${exception.toString()}")
                }

            })
        } catch(e:MqttException){
            e.printStackTrace()
        }
    }

    fun disconnectFromBroker(){
        if(mqttClient==null){
            return
        }
        try{
            mqttClient!!.close()
            mqttClient = null
            Log.d(TAG, "CONNECTION CLOSED")

        } catch(e:MqttException){
            e.printStackTrace()
        }
    }

    fun sendData(topic:String = publishtopic, payload:String){
        if(mqttClient==null) return
        if(!mqttClient!!.isConnected){ return }

        try{
            val msg = MqttMessage()
            msg.payload = payload.toByteArray()
            msg.qos = 1
            msg.isRetained = false
            mqttClient!!.publish(topic, msg, null, object : IMqttActionListener{
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(TAG, "Send Failed")
                }

                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Send Success")
                }
            })
        } catch (e:MqttException){
            e.printStackTrace()
        }
        return
    }

}
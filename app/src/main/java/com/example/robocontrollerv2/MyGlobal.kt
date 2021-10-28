package com.example.robocontrollerv2

import android.annotation.SuppressLint

class MyGlobal {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var globalConnector:Connector
    }
}
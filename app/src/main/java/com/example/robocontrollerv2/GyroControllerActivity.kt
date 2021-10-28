package com.example.robocontrollerv2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.robocontrollerv2.databinding.ActivityGyroControllerBinding

class GyroControllerActivity : AppCompatActivity() {

    private lateinit var binding:ActivityGyroControllerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGyroControllerBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}
package com.example.robocontrollerv2.ui.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ControllerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Controller Fragment"
    }
    val text: LiveData<String> = _text
}
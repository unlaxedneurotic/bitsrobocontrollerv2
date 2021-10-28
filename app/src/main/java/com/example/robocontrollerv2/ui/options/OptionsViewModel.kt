package com.example.robocontrollerv2.ui.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OptionsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Options Fragment"
    }
    val text: LiveData<String> = _text
}
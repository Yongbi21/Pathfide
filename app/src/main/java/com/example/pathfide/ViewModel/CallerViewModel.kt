package com.example.pathfide.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CallerViewModel : ViewModel() {
    private val _firstName = MutableLiveData<String>()
    val firstName: LiveData<String> get() = _firstName

    private val _lastName = MutableLiveData<String>()
    val lastName: LiveData<String> get() = _lastName

    fun setCallerData(firstName: String, lastName: String) {
        _firstName.value = firstName
        _lastName.value = lastName
    }

    fun getUserName(): String {
        return "${_firstName.value} ${_lastName.value}"
    }
}
package com.example.pathfide.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName
    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> get() = _userId


    fun setFullName(name: String) {
        _fullName.value = name
    }

    fun setUserId(id: String) {
        _userId.value = id
    }

}

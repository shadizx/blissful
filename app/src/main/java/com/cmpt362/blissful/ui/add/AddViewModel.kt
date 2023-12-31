package com.cmpt362.blissful.ui.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddViewModel : ViewModel() {
    var isPublic = MutableLiveData<Boolean>().apply {
        value = true
    }
    var newImage = MutableLiveData<String>()
}
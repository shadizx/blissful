package com.cmpt362.blissful.db.like

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LikeViewModel(private val repository: LikeRepository) : ViewModel() {
    fun insert(like: Like): LiveData<String> {
        val likeIdLiveData = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO) {
            val likeId = repository.insert(like)
            likeIdLiveData.postValue(likeId)
        }
        return likeIdLiveData
    }

    fun delete(likeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(likeId)
        }
    }

    fun delete(userId: String, postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(userId, postId)
        }
    }

    fun hasUserLikedPost(userId: String, postId: String): Boolean {
        var hasUserLikedPost = false
        viewModelScope.launch(Dispatchers.IO) {
            hasUserLikedPost = repository.hasUserLikedPost(userId, postId)
        }
        return hasUserLikedPost
    }
}
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

    fun getLikesByUserId(userId: String): LiveData<Set<String>> {
        val likesLiveData = MutableLiveData<Set<String>>()
        viewModelScope.launch(Dispatchers.IO) {
            val likes = repository.getPostsThatUserLiked(userId)
            likesLiveData.postValue(likes)
        }
        return likesLiveData
    }
}

class LikeViewModelFactory(private val repository: LikeRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LikeViewModel::class.java))
            return LikeViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
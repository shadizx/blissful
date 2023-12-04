package com.cmpt362.blissful.db.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel(private val repository: PostRepository) : ViewModel() {
    val allPosts: LiveData<List<Post>> = repository.allPosts.asLiveData()
    fun getPublicPosts(): LiveData<List<Post>> =
        repository.getPublicPosts().asLiveData()

    fun getPostsByUserId(userId: String): LiveData<List<Post>> =
        repository.getPostsByUserId(userId).asLiveData()

    fun getPostsWithoutUserId(userId: String): LiveData<List<Post>> =
        repository.getPostsWithoutUserId(userId).asLiveData()

    fun insert(post: Post): LiveData<String> {
        val postIdLiveData = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO) {
            val postId = repository.insert(post)
            postIdLiveData.postValue(postId)
        }
        return postIdLiveData
    }

    fun likePost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.likePost(postId)
        }
    }

    fun unlikePost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.unlikePost(postId)
        }
    }
}

class PostViewModelFactory(private val repository: PostRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PostViewModel::class.java))
            return PostViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

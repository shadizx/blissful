package com.cmpt362.blissful.db.post

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    val allPosts: LiveData<List<Post>> = repository.allPosts.asLiveData()

    fun getPublicPosts(): LiveData<List<Post>> =
        repository.getPublicPosts().asLiveData()

    fun getPostsByUserId(userId: String): LiveData<List<Post>> =
        repository.getPostsByUserId(userId).asLiveData()

    fun getPostsWithoutUserId(userId: String): LiveData<List<Post>> =
        repository.getPostsWithoutUserId(userId).asLiveData()

    fun getPostById(postId: String): LiveData<Post?> = repository.getPostById(postId).asLiveData()

    fun getPostsBetweenPostTime(startTime: Calendar, endTime: Calendar): LiveData<List<Post>> =
        repository.getPostsBetweenPostTime(startTime, endTime).asLiveData()

    fun insert(post: Post): LiveData<String> {
        val postIdLiveData = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO) {
            val postId = repository.insert(post)
            postIdLiveData.postValue(postId)
        }
        return postIdLiveData
    }

    fun update(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(post)
        }
    }

    fun delete(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(postId)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAll()
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

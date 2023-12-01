package com.cmpt362.blissful.db.post

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class PostViewModel(private val repository: PostRepository) : ViewModel() {

    /**
     * For queries returning Flow object, we don't need to make them run in coroutines as they are already doing so and run asynchronously.
     */

    val allPublicPosts: LiveData<List<Post>> = repository.getAllPosts().asLiveData()

    fun getPostsByUserId(userId: String): LiveData<List<Post>> =
        repository.getPostsByUserId(userId).asLiveData()

    fun getPostsWithoutUserId(userId: String): LiveData<List<Post>> =
        repository.getPostsWithoutUserId(userId).asLiveData()

    fun getPostById(postId: Int): LiveData<Post> = repository.getPostById(postId).asLiveData()

    fun getPostsBetweenPostTime(startTime: Calendar, endTime: Calendar): LiveData<List<Post>> =
        repository.getPostsBetweenPostTime(startTime, endTime).asLiveData()

    fun insert(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(post)
        }
    }

    fun update(post: Post) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.update(post)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
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

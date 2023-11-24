package com.cmpt362.blissful.db.user

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val allUsersLiveData: LiveData<List<User>> = repository.allUsers.asLiveData()

    fun insert(user: User): LiveData<Int> {
        val id = MutableLiveData<Int>()
        viewModelScope.launch(Dispatchers.IO) {
            id.postValue(repository.insert(user))
        }
        return id
    }

    fun checkUserForLogin(username: String, password: String): LiveData<Boolean> {
        val isUserExist = MutableLiveData<Boolean>()
        viewModelScope.launch {
            isUserExist.value = repository.isUserExist(username, password)
        }
        return isUserExist
    }

    fun checkIsUsernameTaken(username: String): LiveData<Boolean> {
        val isUsernameTaken = MutableLiveData<Boolean>()
        viewModelScope.launch {
            isUsernameTaken.value = repository.isUsernameTaken(username)
        }
        return isUsernameTaken
    }

    fun getIdForUser(username: String): LiveData<Int> {
        val id = MutableLiveData<Int>()
        viewModelScope.launch {
            id.value = repository.getIdForUser(username)
        }
        return id
    }
}

class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java))
            return UserViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
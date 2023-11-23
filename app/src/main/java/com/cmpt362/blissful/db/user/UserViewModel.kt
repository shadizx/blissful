package com.cmpt362.blissful.db.user

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val allUsersLiveData: LiveData<List<User>> = repository.allUsers.asLiveData()

    fun insert(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(user)
        }
    }

    fun deleteFirst() {
        viewModelScope.launch(Dispatchers.IO) {
            val userList = allUsersLiveData.value
            if (!userList.isNullOrEmpty()) {
                val userId = userList[0].userId
                repository.delete(userId)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            val userList = allUsersLiveData.value
            if (!userList.isNullOrEmpty())
                repository.deleteAll()
        }
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
}

class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java))
            return UserViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
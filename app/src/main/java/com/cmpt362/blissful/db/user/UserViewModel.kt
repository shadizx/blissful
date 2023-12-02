package com.cmpt362.blissful.db.user

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    val allUsersLiveData: LiveData<List<User>> = repository.allUsers.asLiveData()

    fun insert(user: User): LiveData<String> {
        val userIdLiveData = MutableLiveData<String>()
        viewModelScope.launch(Dispatchers.IO) {
            val userId = repository.insert(user)
            userIdLiveData.postValue(userId)
        }
        return userIdLiveData
    }

    fun checkUserForLogin(username: String, password: String): LiveData<Boolean> {
        val isUserExistLiveData = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            val isUserExist = repository.isUserExist(username, password)
            isUserExistLiveData.postValue(isUserExist)
        }
        return isUserExistLiveData
    }

    fun checkIsUsernameTaken(username: String): LiveData<Boolean> {
        val isUsernameTakenLiveData = MutableLiveData<Boolean>()
        viewModelScope.launch(Dispatchers.IO) {
            val isUsernameTaken = repository.isUsernameTaken(username)
            isUsernameTakenLiveData.postValue(isUsernameTaken)
        }
        return isUsernameTakenLiveData
    }

    fun getIdForUser(username: String): MutableLiveData<String?> {
        val idLiveData = MutableLiveData<String?>()
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.getIdForUser(username)
            idLiveData.postValue(id)
        }
        return idLiveData
    }

    fun getUsernameForUserId(userId: String): LiveData<String?> {
        val usernameLiveData = MutableLiveData<String?>()
        viewModelScope.launch(Dispatchers.IO) {
            val username = repository.getUsernameForUserId(userId)
            usernameLiveData.postValue(username)
        }
        return usernameLiveData
    }

    fun getProfileImgUrlByUserId(userId: String): LiveData<String?> {
        val liveData = MutableLiveData<String?>()
        viewModelScope.launch {
            val url = repository.getProfileImgUrlByUserId(userId)
            liveData.postValue(url)
        }
        return liveData
    }

    fun updateProfileImgUrl(userId: String, newUrl: String) {
        viewModelScope.launch {
            repository.updateProfileImgUrl(userId, newUrl)
        }
    }
}

class UserViewModelFactory(private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java))
            return UserViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

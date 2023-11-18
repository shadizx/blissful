package com.cmpt362.blissful.db

import androidx.lifecycle.*
import java.lang.IllegalArgumentException


class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val allUsersLiveData: LiveData<List<User>> = repository.allUsers.asLiveData()

    fun insert(user: User) {
        repository.insert(user)
    }

    fun deleteFirst(){
        val userList = allUsersLiveData.value
        if (!userList.isNullOrEmpty()){
            val id = userList[0].id
            repository.delete(id)
        }
    }

    fun deleteAll(){
        val userList = allUsersLiveData.value
        if (!userList.isNullOrEmpty())
            repository.deleteAll()
    }
}

class UserViewModelFactory (private val repository: UserRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass, which is UserViewModel in this case.
        if(modelClass.isAssignableFrom(UserViewModel::class.java))
            return UserViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
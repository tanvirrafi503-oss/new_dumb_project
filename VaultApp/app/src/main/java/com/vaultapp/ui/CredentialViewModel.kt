package com.vaultapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vaultapp.data.Credential
import com.vaultapp.data.CredentialRepository
import kotlinx.coroutines.launch

class CredentialViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = CredentialRepository(app)

    val allCredentials = repo.allCredentials
    val searchQuery = MutableLiveData("")

    fun search(query: String) = repo.search(query)

    fun insert(credential: Credential) = viewModelScope.launch {
        repo.insert(credential)
    }

    fun update(credential: Credential) = viewModelScope.launch {
        repo.update(credential)
    }

    fun delete(credential: Credential) = viewModelScope.launch {
        repo.delete(credential)
    }

    fun deleteById(id: Long) = viewModelScope.launch {
        repo.deleteById(id)
    }

    suspend fun getById(id: Long) = repo.getById(id)
}

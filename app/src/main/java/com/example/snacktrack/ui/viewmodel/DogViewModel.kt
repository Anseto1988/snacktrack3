package com.example.snacktrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.repository.DogRepository

class DogViewModel(context: Context) : ViewModel() {
    
    private val dogRepository = DogRepository(context)
    
    private val _dogs = MutableStateFlow<List<Dog>>(emptyList())
    val dogs: StateFlow<List<Dog>> = _dogs.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadDogs()
    }
    
    fun loadDogs() {
        viewModelScope.launch {
            _isLoading.value = true
            dogRepository.getDogs().collect { dogList ->
                _dogs.value = dogList
                _isLoading.value = false
            }
        }
    }
    
    fun saveDog(dog: Dog) {
        viewModelScope.launch {
            _isLoading.value = true
            dogRepository.saveDog(dog)
                .onSuccess {
                    loadDogs()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Speichern: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun deleteDog(dogId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            dogRepository.deleteDog(dogId)
                .onSuccess {
                    loadDogs()
                }
                .onFailure { e ->
                    _errorMessage.value = "Fehler beim Löschen: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 
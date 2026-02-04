package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.model.SessionTemplate
import com.gymtracker.app.data.model.TemplateExercise
import com.gymtracker.app.data.model.TemplateWithExercises
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateViewModel(private val repository: GymRepository) : ViewModel() {

    // Liste de tous les templates avec leurs exercices
    val allTemplatesWithExercises: Flow<List<TemplateWithExercises>> = repository.allTemplatesWithExercises

    // Template actuellement en édition
    private val _editingTemplateId = MutableStateFlow<Long?>(null)
    val editingTemplateId: StateFlow<Long?> = _editingTemplateId.asStateFlow()

    // Exercices du template en édition
    val editingTemplateExercises: Flow<List<TemplateExercise>> = _editingTemplateId
        .filterNotNull()
        .flatMapLatest { templateId ->
            repository.getExercisesForTemplate(templateId)
        }

    // Noms d'exercices pour l'autocomplétion
    val exerciseNames: Flow<List<String>> = repository.allExerciseNames

    // Messages UI
    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // === Gestion des templates ===

    fun createTemplate(name: String, description: String = "") {
        viewModelScope.launch {
            val templateId = repository.createTemplate(name, description)
            _editingTemplateId.value = templateId
            _uiMessage.emit("Template \"$name\" créé")
        }
    }

    fun updateTemplate(template: SessionTemplate) {
        viewModelScope.launch {
            repository.updateTemplate(template)
            _uiMessage.emit("Template mis à jour")
        }
    }

    fun deleteTemplate(template: SessionTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
            if (_editingTemplateId.value == template.id) {
                _editingTemplateId.value = null
            }
            _uiMessage.emit("Séance \"${template.name}\" supprimée")
        }
    }

    fun selectTemplateForEditing(templateId: Long?) {
        _editingTemplateId.value = templateId
    }

    // === Gestion des exercices de template ===

    fun addExerciseToTemplate(name: String, defaultSetsCount: Int = 3, restTimeSeconds: Int = 90) {
        viewModelScope.launch {
            _editingTemplateId.value?.let { templateId ->
                repository.addTemplateExercise(templateId, name, defaultSetsCount, restTimeSeconds)
            }
        }
    }

    fun updateTemplateExercise(exercise: TemplateExercise) {
        viewModelScope.launch {
            repository.updateTemplateExercise(exercise)
        }
    }

    fun deleteTemplateExercise(exercise: TemplateExercise) {
        viewModelScope.launch {
            repository.deleteTemplateExercise(exercise)
        }
    }

    fun swapExerciseOrder(exercise1: TemplateExercise, exercise2: TemplateExercise) {
        viewModelScope.launch {
            // Échanger les orderIndex des deux exercices
            val newExercise1 = exercise1.copy(orderIndex = exercise2.orderIndex)
            val newExercise2 = exercise2.copy(orderIndex = exercise1.orderIndex)
            repository.updateTemplateExercise(newExercise1)
            repository.updateTemplateExercise(newExercise2)
        }
    }

    // Factory
    companion object {
        fun provideFactory(repository: GymRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TemplateViewModel(repository) as T
                }
            }
        }
    }
}

package com.gymtracker.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymtracker.app.data.model.*
import com.gymtracker.app.data.repository.GymRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: GymRepository
) : ViewModel() {

    // === Mensurations ===

    val allMeasurements: StateFlow<List<Measurement>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestMeasurement: StateFlow<Measurement?> = repository.latestMeasurement
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _measurementToEdit = MutableStateFlow<Measurement?>(null)
    val measurementToEdit: StateFlow<Measurement?> = _measurementToEdit.asStateFlow()

    fun setMeasurementToEdit(measurement: Measurement?) {
        _measurementToEdit.value = measurement
    }

    fun saveMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            if (measurement.id == 0L) {
                repository.addMeasurement(measurement)
            } else {
                repository.updateMeasurement(measurement)
            }
            _measurementToEdit.value = null
        }
    }

    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            repository.deleteMeasurement(measurement)
        }
    }

    // === Badges ===

    private val _userStats = MutableStateFlow(UserStats())
    val userStats: StateFlow<UserStats> = _userStats.asStateFlow()

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    private val _unlockedBadgesCount = MutableStateFlow(0)
    val unlockedBadgesCount: StateFlow<Int> = _unlockedBadgesCount.asStateFlow()

    // Badges débloqués (stockés avec leur date de déverrouillage)
    private val unlockedBadgesMap = mutableMapOf<String, Long>()

    private val _lastMonthStats = MutableStateFlow<MonthlyStats?>(null)
    val lastMonthStats: StateFlow<MonthlyStats?> = _lastMonthStats.asStateFlow()

    init {
        loadStats()
        viewModelScope.launch {
            _lastMonthStats.value = repository.getLastMonthStats()
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            val stats = repository.getUserStats()
            _userStats.value = stats

            // Générer les badges avec leur état
            val badgeList = BadgeDefinitions.ALL_BADGES.map { def ->
                def.toBadge(stats, unlockedBadgesMap)
            }

            // Marquer les nouveaux badges débloqués
            badgeList.forEach { badge ->
                if (badge.isUnlocked && !unlockedBadgesMap.containsKey(badge.id)) {
                    unlockedBadgesMap[badge.id] = System.currentTimeMillis()
                }
            }

            _badges.value = badgeList
            _unlockedBadgesCount.value = badgeList.count { it.isUnlocked }
        }
    }

    fun getBadgesByCategory(category: BadgeCategory): List<Badge> {
        return badges.value.filter { it.category == category }
    }

    companion object {
        fun provideFactory(repository: GymRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(repository) as T
                }
            }
        }
    }
}


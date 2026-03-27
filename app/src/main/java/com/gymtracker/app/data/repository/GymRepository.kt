package com.gymtracker.app.data.repository

import com.gymtracker.app.data.dao.ExerciseDao
import com.gymtracker.app.data.dao.ExerciseSetDao
import com.gymtracker.app.data.dao.MeasurementDao
import com.gymtracker.app.data.dao.SessionTemplateDao
import com.gymtracker.app.data.dao.TemplateExerciseDao
import com.gymtracker.app.data.dao.WorkoutDao
import com.gymtracker.app.data.model.*
import com.gymtracker.app.data.SettingsManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar

class GymRepository(
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val exerciseSetDao: ExerciseSetDao,
    private val sessionTemplateDao: SessionTemplateDao,
    private val templateExerciseDao: TemplateExerciseDao,
    private val measurementDao: MeasurementDao
) {
    // Workouts
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()
    val allWorkoutsWithExercises: Flow<List<WorkoutWithExercises>> = workoutDao.getAllWorkoutsWithExercises()
    val completedWorkoutsWithExercises: Flow<List<WorkoutWithExercises>> = workoutDao.getCompletedWorkoutsWithExercises()
    
    suspend fun createWorkout(name: String = ""): Long {
        val workout = Workout(name = name)
        return workoutDao.insert(workout)
    }
    
    suspend fun updateWorkout(workout: Workout) = workoutDao.update(workout)
    
    suspend fun deleteWorkout(workout: Workout) = workoutDao.delete(workout)
    
    suspend fun deleteWorkoutById(workoutId: Long) {
        workoutDao.deleteById(workoutId)
    }

    suspend fun getWorkoutById(id: Long) = workoutDao.getWorkoutById(id)
    
    suspend fun getWorkoutWithExercises(id: Long) = workoutDao.getWorkoutWithExercises(id)
    
    suspend fun getActiveWorkout() = workoutDao.getActiveWorkout()
    
    suspend fun completeWorkout(workoutId: Long, duration: Int, notes: String = "") {
        workoutDao.completeWorkout(workoutId, duration, notes)
    }
    
    // Exercises
    fun getExercisesWithSetsForWorkout(workoutId: Long): Flow<List<ExerciseWithSets>> =
        exerciseDao.getExercisesWithSetsForWorkout(workoutId)
    
    val allExerciseNames: Flow<List<String>> = exerciseDao.getAllExerciseNames()
    
    suspend fun addExercise(workoutId: Long, name: String, restTimeSeconds: Int = 180): Long {
        val maxOrder = exerciseDao.getMaxOrderIndex(workoutId) ?: -1
        val exercise = Exercise(
            workoutId = workoutId,
            name = name,
            orderIndex = maxOrder + 1,
            restTimeSeconds = restTimeSeconds
        )
        return exerciseDao.insert(exercise)
    }
    
    suspend fun deleteExercise(exercise: Exercise) = exerciseDao.delete(exercise)
    
    // Sets
    fun getSetsForExercise(exerciseId: Long): Flow<List<ExerciseSet>> =
        exerciseSetDao.getSetsForExercise(exerciseId)
    
    suspend fun addSet(exerciseId: Long, reps: Int = 0, weight: Float = 0f, miorep: Int? = null): Long {
        val maxSetNumber = exerciseSetDao.getMaxSetNumber(exerciseId) ?: 0
        val set = ExerciseSet(
            exerciseId = exerciseId,
            setNumber = maxSetNumber + 1,
            reps = reps,
            weight = weight,
            miorep = miorep
        )
        return exerciseSetDao.insert(set)
    }
    
    suspend fun updateSet(set: ExerciseSet) = exerciseSetDao.update(set)
    
    suspend fun updateSetValues(setId: Long, reps: Int, weight: Float, miorep: Int?) {
        exerciseSetDao.updateSetValues(setId, reps, weight, miorep)
    }
    
    suspend fun toggleSetCompletion(setId: Long, completed: Boolean) {
        exerciseSetDao.updateSetCompletion(setId, completed)
    }
    
    suspend fun deleteSet(set: ExerciseSet) = exerciseSetDao.delete(set)
    
    // Progress Data
    fun getProgressForExercise(exerciseName: String): Flow<ExerciseProgress> {
        return completedWorkoutsWithExercises.map { workouts ->
            val dataPoints = workouts
                .flatMap { workout ->
                    workout.exercises
                        .filter { it.exercise.name.equals(exerciseName, ignoreCase = true) }
                        .map { exerciseWithSets ->
                            val sets = exerciseWithSets.sets.filter { it.isCompleted }
                            if (sets.isNotEmpty()) {
                                val maxWeight = sets.maxOf { it.weight }
                                // Volume total prend en compte les mioreps (1 miorep = 1/3 rep)
                                val totalVolume = sets.sumOf { set ->
                                    val effectiveReps = calculateEffectiveReps(set.reps, set.miorep)
                                    (set.weight * effectiveReps).toDouble()
                                }.toFloat()
                                // Le meilleur set est celui avec le meilleur 1RM estimé (incluant mioreps)
                                val bestSet = sets.maxByOrNull { calculate1RM(it.weight, it.reps, it.miorep) }

                                ProgressDataPoint(
                                    date = workout.workout.date,
                                    maxWeight = maxWeight,
                                    totalVolume = totalVolume,
                                    bestSet = BestSetInfo(
                                        weight = bestSet?.weight ?: 0f,
                                        reps = bestSet?.reps ?: 0,
                                        miorep = bestSet?.miorep,
                                        estimated1RM = bestSet?.let { calculate1RM(it.weight, it.reps, it.miorep) } ?: 0f
                                    )
                                )
                            } else null
                        }
                        .filterNotNull()
                }
                .sortedBy { it.date }
            
            ExerciseProgress(
                exerciseName = exerciseName,
                dataPoints = dataPoints
            )
        }
    }
    
    // Export
    suspend fun exportAllData(): ExportData {
        val workouts = allWorkoutsWithExercises.first()
        val measurements = allMeasurements.first()
        return ExportData(
            workouts = workouts.map { it.toExport() },
            measurements = measurements.map { it.toExport() }
        )
    }

    suspend fun exportWorkoutsData(): ExportData {
        val workouts = allWorkoutsWithExercises.first()
        return ExportData(
            workouts = workouts.map { it.toExport() },
            measurements = emptyList()
        )
    }

    suspend fun exportTemplatesData(): TemplateExportData {
        val templates = allTemplatesWithExercises.first()
        return TemplateExportData(
            templates = templates.map { it.toExport() }
        )
    }

    // === Templates ===

    val allTemplates: Flow<List<SessionTemplate>> = sessionTemplateDao.getAllTemplates()
    val allTemplatesWithExercises: Flow<List<TemplateWithExercises>> = sessionTemplateDao.getAllTemplatesWithExercises()

    suspend fun createTemplate(name: String, description: String = ""): Long {
        val template = SessionTemplate(name = name, description = description)
        return sessionTemplateDao.insert(template)
    }

    suspend fun updateTemplate(template: SessionTemplate) = sessionTemplateDao.update(template)

    suspend fun deleteTemplate(template: SessionTemplate) = sessionTemplateDao.delete(template)

    suspend fun getTemplateById(id: Long) = sessionTemplateDao.getTemplateById(id)

    suspend fun getTemplateWithExercises(id: Long) = sessionTemplateDao.getTemplateWithExercises(id)

    // Template Exercises
    suspend fun addTemplateExercise(templateId: Long, name: String, defaultSetsCount: Int = 3, restTimeSeconds: Int = 180): Long {
        val maxOrder = templateExerciseDao.getMaxOrderIndex(templateId) ?: -1
        val exercise = TemplateExercise(
            templateId = templateId,
            name = name,
            orderIndex = maxOrder + 1,
            defaultSetsCount = defaultSetsCount,
            restTimeSeconds = restTimeSeconds
        )
        return templateExerciseDao.insert(exercise)
    }

    suspend fun updateTemplateExercise(exercise: TemplateExercise) = templateExerciseDao.update(exercise)

    suspend fun deleteTemplateExercise(exercise: TemplateExercise) = templateExerciseDao.delete(exercise)

    fun getExercisesForTemplate(templateId: Long): Flow<List<TemplateExercise>> =
        templateExerciseDao.getExercisesForTemplate(templateId)

    // === Créer une séance à partir d'un template ===

    suspend fun createWorkoutFromTemplate(templateId: Long): Long {
        val template = sessionTemplateDao.getTemplateWithExercises(templateId)
            ?: throw IllegalArgumentException("Template not found")

        // Créer la séance avec le nom du template
        val workoutId = workoutDao.insert(Workout(name = template.template.name))

        // Ajouter les exercices du template à la séance
        for (templateExercise in template.exercises.sortedBy { it.orderIndex }) {
            val exerciseId = exerciseDao.insert(
                Exercise(
                    workoutId = workoutId,
                    name = templateExercise.name,
                    orderIndex = templateExercise.orderIndex,
                    restTimeSeconds = templateExercise.restTimeSeconds,
                    supersetGroupId = templateExercise.supersetGroupId
                )
            )

            // Récupérer TOUTES les séries de la dernière séance pour cet exercice
            val lastSets = exerciseSetDao.getAllSetsFromLastWorkoutByExerciseName(templateExercise.name)

            if (lastSets.isNotEmpty()) {
                // Utiliser les séries de la dernière séance
                lastSets.forEachIndexed { index, lastSet ->
                    exerciseSetDao.insert(
                        ExerciseSet(
                            exerciseId = exerciseId,
                            setNumber = index + 1,
                            weight = lastSet.weight,
                            reps = lastSet.reps,
                            miorep = lastSet.miorep
                        )
                    )
                }
            } else {
                // Pas d'historique, créer des séries vides
                for (setNumber in 1..templateExercise.defaultSetsCount) {
                    exerciseSetDao.insert(
                        ExerciseSet(
                            exerciseId = exerciseId,
                            setNumber = setNumber,
                            weight = 0f,
                            reps = 0,
                            miorep = null
                        )
                    )
                }
            }
        }

        return workoutId
    }

    // === Récupérer les dernières valeurs d'un exercice ===

    suspend fun getLastSetValuesForExercise(exerciseName: String): ExerciseSet? {
        return exerciseSetDao.getLastCompletedSetByExerciseName(exerciseName)
    }

    suspend fun getAllSetsFromLastWorkout(exerciseName: String): List<ExerciseSet> {
        return exerciseSetDao.getAllSetsFromLastWorkoutByExerciseName(exerciseName)
    }

    // === Supprimer toutes les données ===

    suspend fun deleteAllData() {
        // Supprimer toutes les séances (les exercices et sets seront supprimés en cascade)
        workoutDao.deleteAll()
        // Supprimer tous les templates (les exercices de template seront supprimés en cascade)
        sessionTemplateDao.deleteAll()
    }

    // === Importer des données ===

    suspend fun importData(exportData: ExportData): Pair<Int, Int> {
        var workoutsImported = 0
        var exercisesImported = 0

        for (workoutExport in exportData.workouts) {
            // Créer la séance
            val workoutId = workoutDao.insert(
                Workout(
                    date = workoutExport.date,
                    name = workoutExport.name,
                    notes = workoutExport.notes,
                    durationMinutes = workoutExport.durationMinutes,
                    isCompleted = workoutExport.isCompleted
                )
            )
            workoutsImported++

            // Ajouter les exercices
            for (exerciseExport in workoutExport.exercises) {
                val exerciseId = exerciseDao.insert(
                    Exercise(
                        workoutId = workoutId,
                        name = exerciseExport.name,
                        orderIndex = exerciseExport.orderIndex
                    )
                )
                exercisesImported++

                // Ajouter les séries
                for (setExport in exerciseExport.sets) {
                    exerciseSetDao.insert(
                        ExerciseSet(
                            exerciseId = exerciseId,
                            setNumber = setExport.setNumber,
                            reps = setExport.reps,
                            weight = setExport.weight,
                            miorep = setExport.miorep,
                            isCompleted = setExport.isCompleted,
                            timestamp = setExport.timestamp
                        )
                    )
                }
            }
        }

        // Importer les mensurations
        var measurementsImported = 0
        for (measurementExport in exportData.measurements) {
            measurementDao.insert(
                Measurement(
                    date = measurementExport.date,
                    weight = measurementExport.weight,
                    bodyFat = measurementExport.bodyFat,
                    armLeft = measurementExport.armLeft,
                    armRight = measurementExport.armRight,
                    chest = measurementExport.chest,
                    waist = measurementExport.waist,
                    hips = measurementExport.hips,
                    thighLeft = measurementExport.thighLeft,
                    thighRight = measurementExport.thighRight,
                    calfLeft = measurementExport.calfLeft,
                    calfRight = measurementExport.calfRight,
                    shoulders = measurementExport.shoulders,
                    neck = measurementExport.neck,
                    forearmLeft = measurementExport.forearmLeft,
                    forearmRight = measurementExport.forearmRight,
                    notes = measurementExport.notes
                )
            )
            measurementsImported++
        }

        return Pair(workoutsImported, exercisesImported)
    }

    suspend fun importWorkoutsData(exportData: ExportData): Pair<Int, Int> {
        return importData(exportData.copy(measurements = emptyList()))
    }

    suspend fun importTemplatesData(exportData: TemplateExportData): Pair<Int, Int> {
        var templatesImported = 0
        var exercisesImported = 0

        for (templateExport in exportData.templates) {
            val templateId = sessionTemplateDao.insert(
                SessionTemplate(
                    name = templateExport.name,
                    description = templateExport.description,
                    createdAt = templateExport.createdAt
                )
            )
            templatesImported++

            templateExport.exercises
                .sortedBy { it.orderIndex }
                .forEach { exerciseExport ->
                    templateExerciseDao.insert(
                        TemplateExercise(
                            templateId = templateId,
                            name = exerciseExport.name,
                            orderIndex = exerciseExport.orderIndex,
                            defaultSetsCount = exerciseExport.defaultSetsCount,
                            restTimeSeconds = exerciseExport.restTimeSeconds,
                            supersetGroupId = exerciseExport.supersetGroupId
                        )
                    )
                    exercisesImported++
                }
        }

        return Pair(templatesImported, exercisesImported)
    }

    // === Mensurations ===

    val allMeasurements: Flow<List<Measurement>> = measurementDao.getAllMeasurements()
    val latestMeasurement: Flow<Measurement?> = measurementDao.getLatestMeasurement()

    suspend fun addMeasurement(measurement: Measurement): Long {
        return measurementDao.insert(measurement)
    }

    suspend fun updateMeasurement(measurement: Measurement) {
        measurementDao.update(measurement)
    }

    suspend fun deleteMeasurement(measurement: Measurement) {
        measurementDao.delete(measurement)
    }

    suspend fun getMeasurementById(id: Long): Measurement? {
        return measurementDao.getMeasurementById(id)
    }

    fun getMeasurementsInRange(startDate: Long, endDate: Long): Flow<List<Measurement>> {
        return measurementDao.getMeasurementsInRange(startDate, endDate)
    }

    // === Statistiques pour les badges ===

    suspend fun getUserStats(): UserStats {
        val workouts = completedWorkoutsWithExercises.first()

        // Nombre total de séances
        val totalWorkouts = workouts.size

        // Calcul du streak actuel (séances sans pause > 3 jours)
        val sortedWorkouts = workouts.sortedByDescending { it.workout.date }
        var currentStreak = 0
        var maxStreak = 0
        var tempStreak = 0
        var previousDate: Long? = null

        for (workout in sortedWorkouts) {
            if (previousDate == null) {
                tempStreak = 1
            } else {
                val daysDiff = (previousDate - workout.workout.date) / (24 * 60 * 60 * 1000)
                if (daysDiff <= 3) {
                    tempStreak++
                } else {
                    if (currentStreak == 0) currentStreak = tempStreak
                    maxStreak = maxOf(maxStreak, tempStreak)
                    tempStreak = 1
                }
            }
            previousDate = workout.workout.date
        }
        if (currentStreak == 0) currentStreak = tempStreak
        maxStreak = maxOf(maxStreak, tempStreak)

        // Poids max par exercice
        val maxWeightByExercise = mutableMapOf<String, Float>()
        var totalVolume = 0f
        var maxVolumeInWorkout = 0f
        val uniqueExerciseNames = mutableSetOf<String>()
        var hasEarlyMorningWorkout = false
        var hasLateNightWorkout = false
        var hasFullBodyWorkout = false

        for (workout in workouts) {
            var workoutVolume = 0f
            val muscleGroupsInWorkout = mutableSetOf<SettingsManager.Companion.MuscleGroup>()

            // Vérifier l'heure de la séance
            val cal = Calendar.getInstance().apply { timeInMillis = workout.workout.date }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            if (hour < 7) hasEarlyMorningWorkout = true
            if (hour >= 22) hasLateNightWorkout = true

            for (exerciseWithSets in workout.exercises) {
                val exerciseName = exerciseWithSets.exercise.name
                uniqueExerciseNames.add(exerciseName)

                // Groupe musculaire
                val muscleGroup = SettingsManager.getMuscleGroup(exerciseName)
                if (muscleGroup != SettingsManager.Companion.MuscleGroup.OTHER) {
                    muscleGroupsInWorkout.add(muscleGroup)
                }

                for (set in exerciseWithSets.sets.filter { it.isCompleted }) {
                    // Poids max
                    val currentMax = maxWeightByExercise[exerciseName] ?: 0f
                    if (set.weight > currentMax) {
                        maxWeightByExercise[exerciseName] = set.weight
                    }

                    // Volume
                    val effectiveReps = calculateEffectiveReps(set.reps, set.miorep)
                    val setVolume = set.weight * effectiveReps
                    workoutVolume += setVolume
                    totalVolume += setVolume
                }
            }

            maxVolumeInWorkout = maxOf(maxVolumeInWorkout, workoutVolume)

            // Vérifier full body (au moins 5 groupes musculaires différents)
            if (muscleGroupsInWorkout.size >= 5) {
                hasFullBodyWorkout = true
            }
        }

        // Nombre de PRs (simplifié : nombre d'exercices où on a progressé)
        // Pour l'instant on compte le nombre d'exercices différents comme proxy
        val totalPRs = maxWeightByExercise.count { it.value > 0 }

        return UserStats(
            totalWorkouts = totalWorkouts,
            currentStreak = currentStreak,
            maxStreak = maxStreak,
            totalPRs = totalPRs,
            maxWeightByExercise = maxWeightByExercise,
            maxVolumeInWorkout = maxVolumeInWorkout,
            totalVolume = totalVolume,
            uniqueExercises = uniqueExerciseNames.size,
            hasEarlyMorningWorkout = hasEarlyMorningWorkout,
            hasLateNightWorkout = hasLateNightWorkout,
            hasFullBodyWorkout = hasFullBodyWorkout
        )
    }

    /** Stats du mois précédent (pour le récapitulatif mensuel) */
    suspend fun getLastMonthStats(): MonthlyStats {
        val now = Calendar.getInstance()
        val endCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val startCal = (endCal.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val monthStart = startCal.timeInMillis
        val monthEnd = endCal.timeInMillis
        val monthName = SimpleDateFormat("MMMM yyyy", java.util.Locale.FRANCE).format(startCal.time)

        val workouts = completedWorkoutsWithExercises.first()
            .filter { it.workout.date in monthStart until monthEnd }
        val totalVolumeKg = workouts.sumOf { w ->
            w.exercises.sumOf { ex ->
                ex.sets.filter { it.isCompleted }.sumOf { s ->
                    (s.weight * calculateEffectiveReps(s.reps, s.miorep)).toDouble()
                }
            }
        }.toFloat()
        return MonthlyStats(
            monthName = monthName,
            sessionsCount = workouts.size,
            totalMinutes = workouts.sumOf { it.workout.durationMinutes },
            totalVolumeKg = totalVolumeKg,
            uniqueExercises = workouts.flatMap { it.exercises }.map { it.exercise.name }.distinct().size
        )
    }

    // === Supprimer toutes les mensurations ===

    suspend fun deleteAllMeasurements() {
        measurementDao.deleteAll()
    }

    // ────────────────────────────────────────────────
    // Groupe A — PR, suggestions de charge, stats hebdo
    // ────────────────────────────────────────────────

    /** Ré-insère directement un ExerciseSet (pour l'undo) */
    suspend fun insertSetDirectly(set: ExerciseSet): Long = exerciseSetDao.insert(set)

    /** Ré-insère directement un Exercise (pour l'undo) */
    suspend fun insertExerciseDirectly(exercise: Exercise): Long = exerciseDao.insert(exercise)

    /** Poids max historique pour un exercice (séances complétées uniquement) */
    suspend fun getMaxWeightForExercise(exerciseName: String): Float {
        return completedWorkoutsWithExercises.first()
            .flatMap { it.exercises }
            .filter { it.exercise.name.equals(exerciseName, ignoreCase = true) }
            .flatMap { it.sets }
            .filter { it.isCompleted && it.weight > 0f }
            .maxOfOrNull { it.weight } ?: 0f
    }

    /** Meilleur 1RM estimé historique pour un exercice */
    suspend fun getBest1RMForExercise(exerciseName: String): Float {
        return completedWorkoutsWithExercises.first()
            .flatMap { it.exercises }
            .filter { it.exercise.name.equals(exerciseName, ignoreCase = true) }
            .flatMap { it.sets }
            .filter { it.isCompleted && it.weight > 0f }
            .maxOfOrNull { calculate1RM(it.weight, it.reps, it.miorep) } ?: 0f
    }

    /**
     * Suggestion de charge pour la prochaine séance d'un exercice.
     * +1,25 kg si TOUS les sets de la dernière séance étaient complétés,
     * sinon même poids.
     */
    suspend fun getLoadSuggestion(exerciseName: String): LoadSuggestion? {
        val lastSets = exerciseSetDao.getAllSetsFromLastWorkoutByExerciseName(exerciseName)
        if (lastSets.isEmpty()) return null
        val weightedSets = lastSets.filter { it.weight > 0f }
        if (weightedSets.isEmpty()) return null
        val avgWeight = weightedSets.map { it.weight }.average().toFloat()
        val allCompleted = lastSets.all { it.isCompleted }
        return if (allCompleted) {
            LoadSuggestion(suggestedWeight = avgWeight + 1.25f, reason = "+1,25 kg", isProgression = true)
        } else {
            LoadSuggestion(suggestedWeight = avgWeight, reason = "même poids", isProgression = false)
        }
    }

    /** Stats de la semaine en cours (lundi → dimanche) */
    suspend fun getWeeklyStats(): WeeklyStats {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            val dow = get(Calendar.DAY_OF_WEEK)
            add(Calendar.DAY_OF_YEAR, if (dow == Calendar.SUNDAY) -6 else -(dow - Calendar.MONDAY))
        }
        val weekStart = cal.timeInMillis
        val workouts = completedWorkoutsWithExercises.first().filter { it.workout.date >= weekStart }
        val totalVolumeKg = workouts.sumOf { w ->
            w.exercises.sumOf { ex ->
                ex.sets.filter { it.isCompleted }.sumOf { s ->
                    (s.weight * calculateEffectiveReps(s.reps, s.miorep)).toDouble()
                }
            }
        }.toFloat()
        return WeeklyStats(
            sessionsCount = workouts.size,
            totalMinutes = workouts.sumOf { it.workout.durationMinutes },
            totalVolumeKg = totalVolumeKg
        )
    }
}

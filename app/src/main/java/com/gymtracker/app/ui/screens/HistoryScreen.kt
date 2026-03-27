package com.gymtracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymtracker.app.data.model.Workout
import com.gymtracker.app.data.model.WorkoutWithExercises
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel
import com.gymtracker.app.util.matchesSearch
import java.text.SimpleDateFormat
import java.util.*

enum class HistoryViewMode {
    LIST, CALENDAR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val workouts by viewModel.allWorkouts.collectAsState(initial = emptyList())
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }
    var workoutToEdit by remember { mutableStateOf<WorkoutWithExercises?>(null) }
    var viewMode by remember { mutableStateOf(HistoryViewMode.LIST) }
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    // Extraire les noms de séances uniques pour le filtre
    val workoutNames = remember(workouts) {
        workouts.map { it.workout.name }.filter { it.isNotBlank() }.distinct().sorted()
    }

    // Filtrer les workouts (filtre + recherche)
    val filteredWorkouts = remember(workouts, selectedFilter, searchQuery) {
        var result = if (selectedFilter == null) workouts
                     else workouts.filter { it.workout.name == selectedFilter }
        if (searchQuery.isNotBlank()) {
            result = result.filter { wwe ->
                matchesSearch(wwe.workout.name, searchQuery) ||
                matchesSearch(wwe.workout.notes, searchQuery) ||
                wwe.exercises.any { matchesSearch(it.exercise.name, searchQuery) }
            }
        }
        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Exercice, séance, note…") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(
                            "Historique",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground
                ),
                navigationIcon = {
                    if (searchActive) {
                        IconButton(onClick = {
                            searchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Fermer la recherche", tint = OnSurfaceVariant)
                        }
                    }
                },
                actions = {
                    // Recherche
                    if (!searchActive) {
                        IconButton(onClick = { searchActive = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Rechercher",
                                tint = OnSurfaceVariant
                            )
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Effacer", tint = OnSurfaceVariant)
                        }
                    }

                    if (!searchActive) {
                        // Filtre par type de séance
                        if (workoutNames.isNotEmpty()) {
                            Box {
                                IconButton(onClick = { showFilterMenu = true }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "Filtrer",
                                        tint = if (selectedFilter != null) Primary else OnSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showFilterMenu,
                                    onDismissRequest = { showFilterMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Toutes les séances") },
                                        onClick = {
                                            selectedFilter = null
                                            showFilterMenu = false
                                        },
                                        leadingIcon = {
                                            if (selectedFilter == null) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Primary)
                                            }
                                        }
                                    )
                                    Divider()
                                    workoutNames.forEach { name ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                selectedFilter = name
                                                showFilterMenu = false
                                            },
                                            leadingIcon = {
                                                if (selectedFilter == name) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Primary)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Toggle vue liste/calendrier
                        IconButton(onClick = {
                            viewMode = if (viewMode == HistoryViewMode.LIST)
                                HistoryViewMode.CALENDAR else HistoryViewMode.LIST
                        }) {
                            Icon(
                                if (viewMode == HistoryViewMode.LIST) Icons.Default.CalendarMonth else Icons.Default.List,
                                contentDescription = "Changer de vue",
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        containerColor = Background
    ) { paddingValues ->

        if (workouts.isEmpty()) {
            // Pas d'historique
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Aucune séance enregistrée",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurfaceVariant
                    )

                    Text(
                        text = "Vos séances terminées apparaîtront ici",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else if (searchQuery.isNotBlank() && filteredWorkouts.isEmpty()) {
            // Recherche sans résultats
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucun résultat pour \"$searchQuery\"",
                        style = MaterialTheme.typography.titleMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            when (viewMode) {
                HistoryViewMode.LIST -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        // Indicateurs actifs (filtre ou recherche)
                        if (selectedFilter != null || searchQuery.isNotBlank()) {
                            item {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (searchQuery.isNotBlank()) {
                                        FilterChip(
                                            selected = true,
                                            onClick = { searchQuery = ""; searchActive = false },
                                            label = { Text("\"$searchQuery\" • ${filteredWorkouts.size} résultat(s)") },
                                            trailingIcon = {
                                                Icon(Icons.Default.Close, contentDescription = "Effacer", modifier = Modifier.size(16.dp))
                                            }
                                        )
                                    }
                                    if (selectedFilter != null) {
                                        FilterChip(
                                            selected = true,
                                            onClick = { selectedFilter = null },
                                            label = { Text(selectedFilter!!) },
                                            trailingIcon = {
                                                Icon(Icons.Default.Close, contentDescription = "Retirer le filtre", modifier = Modifier.size(16.dp))
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        items(
                            items = filteredWorkouts,
                            key = { it.workout.id }
                        ) { workoutWithExercises ->
                            WorkoutHistoryCard(
                                workoutWithExercises = workoutWithExercises,
                                onDelete = { workoutToDelete = workoutWithExercises.workout },
                                onEdit = { workoutToEdit = workoutWithExercises }
                            )
                        }
                    }
                }

                HistoryViewMode.CALENDAR -> {
                    CalendarView(
                        workouts = filteredWorkouts,
                        onWorkoutClick = { /* TODO: afficher détails */ },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    )
                }
            }
        }
    }

    // Dialog de confirmation de suppression
    workoutToDelete?.let { workout ->
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            title = { Text("Supprimer cette séance ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorkout(workout)
                        workoutToDelete = null
                    }
                ) {
                    Text("Supprimer", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog d'édition de séance
    workoutToEdit?.let { workoutWithExercises ->
        EditWorkoutDialog(
            workoutWithExercises = workoutWithExercises,
            onDismiss = { workoutToEdit = null },
            onSave = { updatedWorkout, updatedSets ->
                viewModel.updateWorkoutHistory(updatedWorkout, updatedSets)
                workoutToEdit = null
            }
        )
    }
}

@Composable
fun CalendarView(
    workouts: List<WorkoutWithExercises>,
    onWorkoutClick: (WorkoutWithExercises) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    val dateFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.FRANCE) }

    // Créer un map des workouts par jour
    val workoutsByDay = remember(workouts, currentMonth) {
        val cal = Calendar.getInstance()
        workouts.filter { workout ->
            cal.timeInMillis = workout.workout.date
            cal.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
        }.groupBy { workout ->
            cal.timeInMillis = workout.workout.date
            cal.get(Calendar.DAY_OF_MONTH)
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Header du mois
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply {
                    add(Calendar.MONTH, -1)
                }
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mois précédent")
            }

            Text(
                text = dateFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = {
                currentMonth = (currentMonth.clone() as Calendar).apply {
                    add(Calendar.MONTH, 1)
                }
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Mois suivant")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Jours de la semaine
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grille des jours
        val firstDayOfMonth = (currentMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        // Ajuster pour que Lundi = 0
        val startOffset = (firstDayOfMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7

        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - startOffset + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayOfMonth in 1..daysInMonth) {
                            val dayWorkouts = workoutsByDay[dayOfMonth]
                            val hasWorkout = dayWorkouts != null

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (hasWorkout) Primary.copy(alpha = 0.2f) else Color.Transparent
                                    )
                                    .clickable(enabled = hasWorkout) {
                                        dayWorkouts?.firstOrNull()?.let { onWorkoutClick(it) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayOfMonth",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (hasWorkout) Primary else OnSurface
                                    )
                                    if (hasWorkout) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Primary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Liste des séances du mois
        val monthWorkouts = workouts.filter { workout ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = workout.workout.date
            cal.get(Calendar.YEAR) == currentMonth.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
        }.sortedByDescending { it.workout.date }

        if (monthWorkouts.isNotEmpty()) {
            Text(
                text = "${monthWorkouts.size} séance(s) ce mois",
                style = MaterialTheme.typography.titleSmall,
                color = Primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(monthWorkouts, key = { it.workout.id }) { workout ->
                    CalendarWorkoutItem(workout)
                }
            }
        } else {
            Text(
                text = "Aucune séance ce mois",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CalendarWorkoutItem(workoutWithExercises: WorkoutWithExercises) {
    val workout = workoutWithExercises.workout
    val dateFormat = remember { SimpleDateFormat("EEE d", Locale.FRANCE) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = workout.name.ifBlank { "Séance libre" },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = dateFormat.format(Date(workout.date)),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }

        Text(
            text = "${workoutWithExercises.exercises.size} exo",
            style = MaterialTheme.typography.bodySmall,
            color = Primary
        )
    }
}

@Composable
fun WorkoutHistoryCard(
    workoutWithExercises: WorkoutWithExercises,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val workout = workoutWithExercises.workout
    val exercises = workoutWithExercises.exercises
    
    val dateFormat = remember { SimpleDateFormat("EEEE d MMMM", Locale.FRANCE) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.FRANCE) }
    
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateFormat.format(Date(workout.date)).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    
                    Text(
                        text = "à ${timeFormat.format(Date(workout.date))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Indicateur de note
                    if (workout.notes.isNotBlank()) {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = "A une note",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (workout.isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Terminée",
                            tint = Completed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Modifier",
                            tint = Primary
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = OnSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats résumées
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(
                    icon = Icons.Default.FitnessCenter,
                    value = "${exercises.size}",
                    label = "Exercices"
                )
                
                val totalSets = exercises.sumOf { it.sets.size }
                StatChip(
                    icon = Icons.Default.Repeat,
                    value = "$totalSets",
                    label = "Séries"
                )
                
                if (workout.durationMinutes > 0) {
                    StatChip(
                        icon = Icons.Default.Timer,
                        value = "${workout.durationMinutes}",
                        label = "Minutes"
                    )
                }
            }
            
            // Liste des exercices (si expanded)
            if (expanded && exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Divider(color = SurfaceVariant)
                
                Spacer(modifier = Modifier.height(12.dp))

                // Afficher les notes si présentes
                if (workout.notes.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariant.copy(alpha = 0.5f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Notes,
                            contentDescription = "Note",
                            tint = Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = workout.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                exercises.forEach { exerciseWithSets ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = exerciseWithSets.exercise.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        val sets = exerciseWithSets.sets
                        val maxWeight = sets.maxOfOrNull { it.weight } ?: 0f
                        val setsCount = sets.size
                        
                        Text(
                            text = "$setsCount séries • ${maxWeight}kg max",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }
            
            // Indicateur expand
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Réduire" else "Développer",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                tint = OnSurfaceVariant
            )
        }
    }
}

@Composable
fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant
        )
    }
}

/**
 * Dialog pour modifier une séance de l'historique
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutDialog(
    workoutWithExercises: WorkoutWithExercises,
    onDismiss: () -> Unit,
    onSave: (Workout, List<com.gymtracker.app.data.model.ExerciseSet>) -> Unit
) {
    val workout = workoutWithExercises.workout
    var editedNotes by remember { mutableStateOf(workout.notes) }

    // Créer une copie mutable des sets pour l'édition
    val editedSets = remember {
        mutableStateMapOf<Long, com.gymtracker.app.data.model.ExerciseSet>().apply {
            workoutWithExercises.exercises.forEach { exerciseWithSets ->
                exerciseWithSets.sets.forEach { set ->
                    this[set.id] = set
                }
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Modifier la séance")
                Text(
                    text = dateFormat.format(Date(workout.date)).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Notes
                item {
                    OutlinedTextField(
                        value = editedNotes,
                        onValueChange = { editedNotes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                // Exercices
                workoutWithExercises.exercises.forEach { exerciseWithSets ->
                    item {
                        Text(
                            text = exerciseWithSets.exercise.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }

                    // Séries de cet exercice
                    exerciseWithSets.sets.sortedBy { it.setNumber }.forEach { set ->
                        item {
                            val currentSet = editedSets[set.id] ?: set
                            EditableSetRow(
                                set = currentSet,
                                onSetUpdated = { updatedSet ->
                                    editedSets[set.id] = updatedSet
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedWorkout = workout.copy(notes = editedNotes)
                    onSave(updatedWorkout, editedSets.values.toList())
                }
            ) {
                Text("Enregistrer", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

/**
 * Ligne de série éditable pour le dialog d'édition
 */
@Composable
fun EditableSetRow(
    set: com.gymtracker.app.data.model.ExerciseSet,
    onSetUpdated: (com.gymtracker.app.data.model.ExerciseSet) -> Unit
) {
    var weightText by remember(set.id) { mutableStateOf(if (set.weight == 0f) "" else set.weight.toString()) }
    var repsText by remember(set.id) { mutableStateOf(if (set.reps == 0) "" else set.reps.toString()) }
    var miorepText by remember(set.id) { mutableStateOf(if (set.miorep == 0) "" else set.miorep.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceVariant)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numéro de série
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.setNumber}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }

        // Poids
        OutlinedTextField(
            value = weightText,
            onValueChange = {
                weightText = it
                val newWeight = it.replace(",", ".").toFloatOrNull() ?: 0f
                onSetUpdated(set.copy(weight = newWeight))
            },
            label = { Text("kg") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Reps
        OutlinedTextField(
            value = repsText,
            onValueChange = {
                repsText = it
                val newReps = it.toIntOrNull() ?: 0
                onSetUpdated(set.copy(reps = newReps))
            },
            label = { Text("Reps") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )

        // Miorep
        OutlinedTextField(
            value = miorepText,
            onValueChange = {
                miorepText = it
                val newMiorep = it.toIntOrNull() ?: 0
                onSetUpdated(set.copy(miorep = newMiorep))
            },
            label = { Text("Mio") },
            modifier = Modifier.weight(0.7f),
            singleLine = true
        )
    }
}

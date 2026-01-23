package com.gymtracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymtracker.app.data.model.ExerciseWithSets
import com.gymtracker.app.ui.components.*
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeWorkoutId by viewModel.activeWorkoutId.collectAsState()
    val workoutStartTime by viewModel.workoutStartTime.collectAsState()
    val exercises by viewModel.activeWorkoutExercises.collectAsState(initial = emptyList())
    val exerciseNames by viewModel.exerciseNames.collectAsState(initial = emptyList())
    
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Séance en cours",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (activeWorkoutId != null) {
                            WorkoutTimer(startTime = workoutStartTime)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground
                ),
                actions = {
                    if (activeWorkoutId != null) {
                        IconButton(onClick = { viewModel.exportToJson(context) }) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Exporter",
                                tint = OnSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (activeWorkoutId != null) {
                ExtendedFloatingActionButton(
                    onClick = { showFinishDialog = true },
                    containerColor = Completed,
                    contentColor = OnPrimary
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Terminer")
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        
        if (activeWorkoutId == null) {
            // Pas de séance active - afficher le bouton pour commencer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Primary
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Prêt pour l'entraînement ?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Commencez une nouvelle séance\npour enregistrer vos exercices",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = { viewModel.startWorkout() },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Commencer la séance",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        } else {
            // Séance active - afficher les exercices
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Liste des exercices
                items(
                    items = exercises,
                    key = { it.exercise.id }
                ) { exerciseWithSets ->
                    ExerciseCard(
                        exerciseWithSets = exerciseWithSets,
                        onAddSet = { viewModel.addSet(exerciseWithSets.exercise.id) },
                        onDeleteExercise = { viewModel.deleteExercise(exerciseWithSets.exercise) },
                        onSetUpdated = { viewModel.updateSet(it) },
                        onSetCompleted = { setId, completed -> 
                            viewModel.toggleSetCompletion(setId, completed) 
                        },
                        onDeleteSet = { viewModel.deleteSet(it) },
                        onIncrementReps = { viewModel.incrementReps(it) },
                        onDecrementReps = { viewModel.decrementReps(it) },
                        onIncrementWeight = { viewModel.incrementWeight(it) },
                        onDecrementWeight = { viewModel.decrementWeight(it) }
                    )
                }
                
                // Bouton ajouter exercice
                item {
                    AddExerciseButton(
                        onClick = { showAddExerciseDialog = true }
                    )
                }
                
                // Espace pour le FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
    
    // Dialog ajouter exercice
    if (showAddExerciseDialog) {
        AddExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name ->
                viewModel.addExercise(name)
                showAddExerciseDialog = false
            },
            existingExerciseNames = exerciseNames
        )
    }
    
    // Dialog confirmer fin de séance
    if (showFinishDialog) {
        val completedSets = exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
        val totalSets = exercises.sumOf { it.sets.size }
        
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Terminer la séance ?") },
            text = {
                Column {
                    Text("Résumé de votre séance :")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• ${exercises.size} exercice(s)")
                    Text("• $completedSets/$totalSets série(s) complétée(s)")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        viewModel.finishWorkout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Completed)
                ) {
                    Text("Terminer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Continuer")
                }
            }
        )
    }
}

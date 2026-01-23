package com.gymtracker.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.gymtracker.app.data.SettingsManager
import com.gymtracker.app.data.model.TemplateWithExercises
import com.gymtracker.app.ui.components.*
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeWorkoutId by viewModel.activeWorkoutId.collectAsState()
    val workoutStartTime by viewModel.workoutStartTime.collectAsState()
    val exercises by viewModel.activeWorkoutExercises.collectAsState(initial = emptyList())
    val exerciseNames by viewModel.exerciseNames.collectAsState(initial = emptyList())
    val templates by viewModel.allTemplates.collectAsState(initial = emptyList())

    // Texte et image d'accueil personnalisés
    val welcomeText by settingsManager.welcomeText.collectAsState(initial = SettingsManager.DEFAULT_WELCOME_TEXT)
    val welcomeImageUri by settingsManager.welcomeImageUri.collectAsState(initial = null)

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showTemplateSelectionDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var workoutNotes by remember { mutableStateOf("") }

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
                        // Bouton pour ajouter une note
                        IconButton(onClick = { showNotesDialog = true }) {
                            Icon(
                                Icons.Default.NoteAdd,
                                contentDescription = "Ajouter une note",
                                tint = if (workoutNotes.isNotBlank()) Primary else OnSurfaceVariant
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
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    // Image d'accueil personnalisée ou icône par défaut
                    if (welcomeImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(Uri.parse(welcomeImageUri))
                                    .crossfade(true)
                                    .build()
                            ),
                            contentDescription = "Image d'accueil",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Primary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = welcomeText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Sélectionne une séance ou\ncommence une séance libre",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Bouton pour sélectionner un template
                    if (templates.isNotEmpty()) {
                        Button(
                            onClick = { showTemplateSelectionDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Choisir une séance",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bouton séance libre (secondaire)
                        OutlinedButton(
                            onClick = { viewModel.startWorkout() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Séance libre",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        // Pas de templates - bouton séance libre uniquement
                        Button(
                            onClick = { viewModel.startWorkout() },
                            modifier = Modifier
                                .fillMaxWidth()
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

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Crée des séances toutes faites dans \"Séances\"\npour gagner du temps",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
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
            onConfirm = { name, restTimeSeconds ->
                viewModel.addExercise(name, restTimeSeconds)
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
                    if (workoutNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("📝 Note ajoutée", color = Primary)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        viewModel.finishWorkout(workoutNotes)
                        workoutNotes = "" // Reset notes
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

    // Dialog pour ajouter une note
    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Note de séance") },
            text = {
                OutlinedTextField(
                    value = workoutNotes,
                    onValueChange = { workoutNotes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("Ex: Douleur au dos, fatigue, bonne forme...") },
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(onClick = { showNotesDialog = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    workoutNotes = ""
                    showNotesDialog = false
                }) {
                    Text("Effacer")
                }
            }
        )
    }

    // Dialog sélection de template
    if (showTemplateSelectionDialog) {
        TemplateSelectionDialog(
            templates = templates,
            onDismiss = { showTemplateSelectionDialog = false },
            onTemplateSelected = { templateId ->
                viewModel.startWorkoutFromTemplate(templateId)
                showTemplateSelectionDialog = false
            }
        )
    }
}

@Composable
private fun TemplateSelectionDialog(
    templates: List<TemplateWithExercises>,
    onDismiss: () -> Unit,
    onTemplateSelected: (Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choisir un template") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates, key = { it.template.id }) { templateWithExercises ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTemplateSelected(templateWithExercises.template.id) },
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = templateWithExercises.template.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${templateWithExercises.exercises.size} exercice(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary
                            )

                            if (templateWithExercises.exercises.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = templateWithExercises.exercises.joinToString(", ") { it.name },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

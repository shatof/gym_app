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
    var showCancelDialog by remember { mutableStateOf(false) }
    var showTemplateSelectionDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var workoutNotes by remember { mutableStateOf("") }

    // État pour le timer de repos - stocke l'ID de l'exercice et les secondes
    var restTimerState by remember { mutableStateOf<Pair<Long, Int>?>(null) } // Pair(exerciseId, seconds)

    // Couleurs pour les groupes de superset
    val supersetColors = listOf(
        androidx.compose.ui.graphics.Color(0xFFE91E63), // Pink
        androidx.compose.ui.graphics.Color(0xFF2196F3), // Blue
        androidx.compose.ui.graphics.Color(0xFFFF9800), // Orange
        androidx.compose.ui.graphics.Color(0xFF9C27B0), // Purple
        androidx.compose.ui.graphics.Color(0xFF00BCD4)  // Cyan
    )

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
            // Grouper les exercices par superset
            val groupedExercises = remember(exercises) {
                val result = mutableListOf<List<com.gymtracker.app.data.model.ExerciseWithSets>>()
                val processed = mutableSetOf<Long>()

                exercises.forEach { exercise ->
                    if (exercise.exercise.id !in processed) {
                        val supersetGroupId = exercise.exercise.supersetGroupId
                        if (supersetGroupId != null) {
                            // Trouver tous les exercices du même superset
                            val supersetExercises = exercises.filter {
                                it.exercise.supersetGroupId == supersetGroupId
                            }
                            result.add(supersetExercises)
                            supersetExercises.forEach { processed.add(it.exercise.id) }
                        } else {
                            result.add(listOf(exercise))
                            processed.add(exercise.exercise.id)
                        }
                    }
                }
                result
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Liste des exercices groupés par superset
                groupedExercises.forEach { exerciseGroup ->
                    item(key = exerciseGroup.map { it.exercise.id }.joinToString(",")) {
                        if (exerciseGroup.size > 1) {
                            // Superset - afficher côte à côte
                            val supersetGroupId = exerciseGroup.first().exercise.supersetGroupId
                            val supersetColor = supersetGroupId?.let { groupId ->
                                supersetColors.getOrNull((groupId - 1) % supersetColors.size)
                            }

                            Column {
                                // Label superset
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Link,
                                        contentDescription = null,
                                        tint = supersetColor ?: Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SUPERSET",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = supersetColor ?: Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // Exercices côte à côte
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    exerciseGroup.forEach { exerciseWithSets ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            ExerciseCard(
                                                exerciseWithSets = exerciseWithSets,
                                                onAddSet = { viewModel.addSet(exerciseWithSets.exercise.id) },
                                                onDeleteExercise = { viewModel.deleteExercise(exerciseWithSets.exercise) },
                                                onSetUpdated = { viewModel.updateSet(it) },
                                                onSetCompleted = { setId, completed ->
                                                    viewModel.toggleSetCompletion(setId, completed)
                                                },
                                                onDeleteSet = { viewModel.deleteSet(it) },
                                                onIncrementWeight = { viewModel.incrementWeight(it) },
                                                onDecrementWeight = { viewModel.decrementWeight(it) },
                                                onRestTimerStart = { seconds ->
                                                    restTimerState = Pair(exerciseWithSets.exercise.id, seconds)
                                                },
                                                supersetColor = supersetColor,
                                                isCompactMode = true
                                            )
                                        }
                                    }
                                }

                                // Timer de repos pour le superset (s'affiche pour n'importe quel exercice du groupe)
                                val activeTimerExercise = exerciseGroup.find { it.exercise.id == restTimerState?.first }
                                if (activeTimerExercise != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    RestTimer(
                                        totalSeconds = restTimerState!!.second,
                                        onDismiss = { restTimerState = null }
                                    )
                                }
                            }
                        } else {
                            // Exercice simple
                            val exerciseWithSets = exerciseGroup.first()
                            val supersetColor = exerciseWithSets.exercise.supersetGroupId?.let { groupId ->
                                supersetColors.getOrNull((groupId - 1) % supersetColors.size)
                            }

                            Column {
                                ExerciseCard(
                                    exerciseWithSets = exerciseWithSets,
                                    onAddSet = { viewModel.addSet(exerciseWithSets.exercise.id) },
                                    onDeleteExercise = { viewModel.deleteExercise(exerciseWithSets.exercise) },
                                    onSetUpdated = { viewModel.updateSet(it) },
                                    onSetCompleted = { setId, completed ->
                                        viewModel.toggleSetCompletion(setId, completed)
                                    },
                                    onDeleteSet = { viewModel.deleteSet(it) },
                                    onIncrementWeight = { viewModel.incrementWeight(it) },
                                    onDecrementWeight = { viewModel.decrementWeight(it) },
                                    onRestTimerStart = { seconds ->
                                        restTimerState = Pair(exerciseWithSets.exercise.id, seconds)
                                    },
                                    supersetColor = supersetColor
                                )

                                // Timer de repos affiché juste en dessous de l'exercice concerné
                                if (restTimerState?.first == exerciseWithSets.exercise.id) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    RestTimer(
                                        totalSeconds = restTimerState!!.second,
                                        onDismiss = { restTimerState = null }
                                    )
                                }
                            }
                        }
                    }
                }

                // Bouton ajouter exercice
                item {
                    AddExerciseButton(
                        onClick = { showAddExerciseDialog = true }
                    )
                }
                
                // Bouton annuler la séance
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Cancel,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Annuler la séance")
                    }
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

    // Dialog confirmer annulation de séance
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    "Annuler la séance ?",
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text("Cette action supprimera définitivement la séance en cours et toutes les données saisies. Cette action est irréversible.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelWorkout()
                        showCancelDialog = false
                        workoutNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Annuler la séance")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Continuer")
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

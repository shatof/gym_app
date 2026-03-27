package com.gymtracker.app.ui.screens

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.gymtracker.app.data.SettingsManager
import com.gymtracker.app.data.model.PrEvent
import com.gymtracker.app.data.model.PrType
import com.gymtracker.app.data.model.TemplateWithExercises
import com.gymtracker.app.data.model.WeeklyStats
import com.gymtracker.app.ui.components.*
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay

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
    val loadSuggestions by viewModel.loadSuggestions.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()

    // Texte et image d'accueil personnalisés
    val welcomeText by settingsManager.welcomeText.collectAsState(initial = SettingsManager.DEFAULT_WELCOME_TEXT)
    val welcomeImageUri by settingsManager.welcomeImageUri.collectAsState(initial = null)

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showTemplateSelectionDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var workoutNotes by remember { mutableStateOf("") }

    // Timer de repos
    var restTimerState by remember { mutableStateOf<Pair<Long, Int>?>(null) }

    // ── Groupe A : PR celebration ──────────────────────────────────────
    var currentPREvent by remember { mutableStateOf<PrEvent?>(null) }
    LaunchedEffect(Unit) {
        viewModel.prEvent.collect { event ->
            currentPREvent = event
        }
    }
    LaunchedEffect(currentPREvent) {
        if (currentPREvent != null) {
            delay(4500)
            currentPREvent = null
        }
    }
    // ──────────────────────────────────────────────────────────────────

    // Couleurs pour les groupes de superset
    val supersetColors = listOf(
        androidx.compose.ui.graphics.Color(0xFFE91E63),
        androidx.compose.ui.graphics.Color(0xFF2196F3),
        androidx.compose.ui.graphics.Color(0xFFFF9800),
        androidx.compose.ui.graphics.Color(0xFF9C27B0),
        androidx.compose.ui.graphics.Color(0xFF00BCD4)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Séance en cours", style = MaterialTheme.typography.titleLarge)
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

        Box(modifier = Modifier.fillMaxSize()) {

            if (activeWorkoutId == null) {
                // ── Mode Aujourd'hui (écran d'accueil enrichi) ───────────────────
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
                        // Image ou icône
                        if (welcomeImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(Uri.parse(welcomeImageUri))
                                        .crossfade(true)
                                        .build()
                                ),
                                contentDescription = "Image d'accueil",
                                modifier = Modifier.size(120.dp).clip(CircleShape),
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

                        if (templates.isNotEmpty()) {
                            Button(
                                onClick = { showTemplateSelectionDialog = true },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choisir une séance", style = MaterialTheme.typography.titleMedium)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { viewModel.startWorkout() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Séance libre", style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.startWorkout() },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Commencer la séance", style = MaterialTheme.typography.titleMedium)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Crée des séances toutes faites dans \"Séances\"\npour gagner du temps",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        // ── Objectifs de la semaine ──────────────────────────
                        weeklyStats?.let { stats ->
                            Spacer(modifier = Modifier.height(24.dp))
                            WeeklyGoalsCard(stats = stats)
                        }
                    }
                }

            } else {
                // ── Séance active ─────────────────────────────────────────────
                val groupedExercises by remember {
                    derivedStateOf {
                        val result = mutableListOf<List<com.gymtracker.app.data.model.ExerciseWithSets>>()
                        val processed = mutableSetOf<Long>()
                        exercises.forEach { exercise ->
                            if (exercise.exercise.id !in processed) {
                                val supersetGroupId = exercise.exercise.supersetGroupId
                                if (supersetGroupId != null) {
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
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    groupedExercises.forEach { exerciseGroup ->
                        item(key = exerciseGroup.map { it.exercise.id }.joinToString(",")) {
                            if (exerciseGroup.size > 1) {
                                val supersetGroupId = exerciseGroup.first().exercise.supersetGroupId
                                val supersetColor = supersetGroupId?.let { groupId ->
                                    supersetColors.getOrNull((groupId - 1) % supersetColors.size)
                                }

                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Link, contentDescription = null,
                                            tint = supersetColor ?: Primary, modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "SUPERSET",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = supersetColor ?: Primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        exerciseGroup.forEach { exerciseWithSets ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                ExerciseCard(
                                                    exerciseWithSets = exerciseWithSets,
                                                    onAddSet = { viewModel.addSet(exerciseWithSets.exercise.id) },
                                                    onDeleteExercise = { viewModel.deleteExercise(exerciseWithSets) },
                                                    onSetUpdated = { viewModel.updateSet(it) },
                                                    onSetCompleted = { setId, completed ->
                                                        viewModel.toggleSetCompletion(setId, completed)
                                                        if (completed) {
                                                            exerciseWithSets.sets.find { it.id == setId }?.let { set ->
                                                                viewModel.checkAndEmitPR(exerciseWithSets.exercise.name, set.weight, set.reps, set.miorep)
                                                            }
                                                        }
                                                    },
                                                    onDeleteSet = { viewModel.deleteSet(it) },
                                                    onIncrementWeight = { viewModel.incrementWeight(it) },
                                                    onDecrementWeight = { viewModel.decrementWeight(it) },
                                                    onRestTimerStart = { seconds ->
                                                        restTimerState = Pair(exerciseWithSets.exercise.id, seconds)
                                                    },
                                                    onRestTimerStop = {
                                                        restTimerState = null
                                                        com.gymtracker.app.service.RestTimerService.stopTimer(context)
                                                    },
                                                    supersetColor = supersetColor,
                                                    isCompactMode = true
                                                )
                                            }
                                        }
                                    }

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
                                val exerciseWithSets = exerciseGroup.first()
                                val supersetColor = exerciseWithSets.exercise.supersetGroupId?.let { groupId ->
                                    supersetColors.getOrNull((groupId - 1) % supersetColors.size)
                                }

                                Column {
                                    ExerciseCard(
                                        exerciseWithSets = exerciseWithSets,
                                        onAddSet = { viewModel.addSet(exerciseWithSets.exercise.id) },
                                        onDeleteExercise = { viewModel.deleteExercise(exerciseWithSets) },
                                        onSetUpdated = { viewModel.updateSet(it) },
                                        onSetCompleted = { setId, completed ->
                                            viewModel.toggleSetCompletion(setId, completed)
                                            if (completed) {
                                                exerciseWithSets.sets.find { it.id == setId }?.let { set ->
                                                    viewModel.checkAndEmitPR(exerciseWithSets.exercise.name, set.weight, set.reps, set.miorep)
                                                }
                                            }
                                        },
                                        onDeleteSet = { viewModel.deleteSet(it) },
                                        onIncrementWeight = { viewModel.incrementWeight(it) },
                                        onDecrementWeight = { viewModel.decrementWeight(it) },
                                        onRestTimerStart = { seconds ->
                                            restTimerState = Pair(exerciseWithSets.exercise.id, seconds)
                                        },
                                        onRestTimerStop = {
                                            restTimerState = null
                                            com.gymtracker.app.service.RestTimerService.stopTimer(context)
                                        },
                                        supersetColor = supersetColor,
                                        loadSuggestion = loadSuggestions[exerciseWithSets.exercise.id]
                                    )

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

                    item {
                        AddExerciseButton(onClick = { showAddExerciseDialog = true })
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Annuler la séance")
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // ── Bannière PR (overlay, uniquement pendant une séance) ─────────
            AnimatedVisibility(
                visible = currentPREvent != null && activeWorkoutId != null,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        start = 16.dp, end = 16.dp
                    )
            ) {
                currentPREvent?.let { event ->
                    PRCelebrationBanner(event = event, onDismiss = { currentPREvent = null })
                }
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────

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
                        viewModel.finishWorkout(workoutNotes, context)
                        workoutNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Completed)
                ) { Text("Terminer") }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) { Text("Continuer") }
            }
        )
    }

    if (showNotesDialog) {
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            title = { Text("Note de séance") },
            text = {
                OutlinedTextField(
                    value = workoutNotes,
                    onValueChange = { workoutNotes = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    placeholder = { Text("Ex: Douleur au dos, fatigue, bonne forme...") },
                    maxLines = 5
                )
            },
            confirmButton = {
                Button(onClick = { showNotesDialog = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { workoutNotes = ""; showNotesDialog = false }) { Text("Effacer") }
            }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Annuler la séance ?", color = MaterialTheme.colorScheme.error) },
            text = { Text("Cette action supprimera définitivement la séance en cours et toutes les données saisies. Cette action est irréversible.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelWorkout()
                        showCancelDialog = false
                        workoutNotes = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Annuler la séance") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Continuer") }
            }
        )
    }

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

// ── Bannière de célébration PR ────────────────────────────────────────────

@Composable
private fun PRCelebrationBanner(event: PrEvent, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Completed),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🏆", fontSize = 28.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Nouveau record !",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                val detail = if (event.type == PrType.WEIGHT) {
                    val prev = formatPRValue(event.previousValue)
                    val next = formatPRValue(event.newValue)
                    "${event.exerciseName} — $next kg (précédent : $prev kg)"
                } else {
                    val prev = formatPRValue(event.previousValue)
                    val next = formatPRValue(event.newValue)
                    "${event.exerciseName} — 1RM : $next kg (précédent : $prev)"
                }
                Text(detail, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.9f))
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun formatPRValue(value: Float): String =
    if (value == value.toInt().toFloat()) value.toInt().toString()
    else String.format("%.1f", value).replace(",", ".")

// ── Objectifs de la semaine (Mode Aujourd'hui) ───────────────────────────

@Composable
private fun WeeklyGoalsCard(stats: WeeklyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cette semaine", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Séances
            WeeklyGoalRow(
                emoji = "🏋️",
                label = "Séances",
                current = stats.sessionsCount,
                goal = stats.sessionsGoal,
                unit = ""
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Temps
            WeeklyGoalRow(
                emoji = "⏱️",
                label = "Temps",
                current = stats.totalMinutes,
                goal = stats.minutesGoal,
                unit = " min"
            )

            if (stats.totalVolumeKg > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                val volumeDisplay = if (stats.totalVolumeKg >= 1000f)
                    "${String.format("%.1f", stats.totalVolumeKg / 1000f)} t"
                else
                    "${stats.totalVolumeKg.toInt()} kg"
                Text(
                    "Volume total : $volumeDisplay",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyGoalRow(emoji: String, label: String, current: Int, goal: Int, unit: String) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, modifier = Modifier.width(22.dp), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                Text(
                    "$current/$goal$unit",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (progress >= 1f) Completed else OnSurface
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (progress >= 1f) Completed else Primary,
                trackColor = SurfaceVariant
            )
        }
    }
}

// ── Sélection de template ─────────────────────────────────────────────────

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
            androidx.compose.foundation.lazy.LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates, key = { it.template.id }) { templateWithExercises ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTemplateSelected(templateWithExercises.template.id) },
                        colors = CardDefaults.cardColors(containerColor = Surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
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
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

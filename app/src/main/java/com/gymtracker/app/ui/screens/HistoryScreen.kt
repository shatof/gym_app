package com.gymtracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymtracker.app.data.model.Workout
import com.gymtracker.app.data.model.WorkoutWithExercises
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val workouts by viewModel.allWorkouts.collectAsState(initial = emptyList())
    var workoutToDelete by remember { mutableStateOf<Workout?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Historique",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground
                ),
                actions = {
                    IconButton(onClick = { viewModel.exportToJson(context) }) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Exporter tout",
                            tint = OnSurfaceVariant
                        )
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
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(
                    items = workouts,
                    key = { it.workout.id }
                ) { workoutWithExercises ->
                    WorkoutHistoryCard(
                        workoutWithExercises = workoutWithExercises,
                        onDelete = { workoutToDelete = workoutWithExercises.workout }
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
}

@Composable
fun WorkoutHistoryCard(
    workoutWithExercises: WorkoutWithExercises,
    onDelete: () -> Unit,
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
                    if (workout.isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Terminée",
                            tint = Completed,
                            modifier = Modifier.size(20.dp)
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

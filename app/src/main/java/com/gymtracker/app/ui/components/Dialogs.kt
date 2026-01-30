package com.gymtracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.gymtracker.app.ui.theme.*

/**
 * Dialog pour ajouter un nouvel exercice
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)
@Composable
fun AddExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, restTimeSeconds: Int) -> Unit,
    existingExerciseNames: List<String>,
    commonExercises: List<String> = com.gymtracker.app.data.SettingsManager.DEFAULT_EXERCISES
) {
    var exerciseName by remember { mutableStateOf("") }
    var restTimeSeconds by remember { mutableStateOf("180") }
    var showSuggestions by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Suggestions triées par priorité : exercices par défaut > existants > tous
    val filteredSuggestions = remember(exerciseName, existingExerciseNames, commonExercises) {
        if (exerciseName.length >= 2) {
            val allExercises = com.gymtracker.app.data.SettingsManager.ALL_EXERCISES

            // D'abord les exercices par défaut qui matchent
            val fromDefault = commonExercises.filter {
                it.contains(exerciseName, ignoreCase = true) && it != exerciseName
            }
            // Ensuite les exercices existants
            val fromExisting = existingExerciseNames.filter {
                it.contains(exerciseName, ignoreCase = true) && it != exerciseName && !fromDefault.contains(it)
            }
            // Enfin tous les autres
            val fromAll = allExercises.filter {
                it.contains(exerciseName, ignoreCase = true) && it != exerciseName && !fromDefault.contains(it) && !fromExisting.contains(it)
            }

            (fromDefault + fromExisting + fromAll).take(3)
        } else {
            emptyList()
        }
    }
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Nouvel exercice",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { 
                        exerciseName = it
                        showSuggestions = it.length >= 2
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = { Text("Nom de l'exercice") },
                    placeholder = { Text("ex: Développé couché") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        cursorColor = Primary
                    )
                )
                
                // Suggestions d'exercices (max 3)
                if (showSuggestions && filteredSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariant)
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        exerciseName = suggestion
                                        showSuggestions = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Temps de repos
                OutlinedTextField(
                    value = restTimeSeconds,
                    onValueChange = { newValue ->
                        restTimeSeconds = newValue.filter { it.isDigit() }.take(3)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Temps de repos (secondes)") },
                    placeholder = { Text("180") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (exerciseName.isNotBlank()) {
                                keyboardController?.hide()
                                onConfirm(exerciseName.trim(), restTimeSeconds.toIntOrNull() ?: 180)
                            }
                        }
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        cursorColor = Primary
                    )
                )

                // Exercices courants
                if (exerciseName.isEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Exercices courants",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        commonExercises.forEach { exercise ->
                            SuggestionChip(
                                onClick = {
                                    exerciseName = exercise
                                },
                                label = { Text(exercise, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (exerciseName.isNotBlank()) {
                                onConfirm(exerciseName.trim(), restTimeSeconds.toIntOrNull() ?: 180)
                            }
                        },
                        enabled = exerciseName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}

/**
 * Chronomètre de séance
 */
@Composable
fun WorkoutTimer(
    startTime: Long?,
    modifier: Modifier = Modifier
) {
    var elapsedTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(startTime) {
        if (startTime != null) {
            while (true) {
                elapsedTime = System.currentTimeMillis() - startTime
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    if (startTime != null) {
        val hours = (elapsedTime / 3600000).toInt()
        val minutes = ((elapsedTime % 3600000) / 60000).toInt()
        val seconds = ((elapsedTime % 60000) / 1000).toInt()
        
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (hours > 0) {
                    "%d:%02d:%02d".format(hours, minutes, seconds)
                } else {
                    "%02d:%02d".format(minutes, seconds)
                },
                style = MaterialTheme.typography.titleMedium,
                color = Primary
            )
        }
    }
}

/**
 * Timer de repos entre séries
 */
@Composable
fun RestTimer(
    totalSeconds: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var remainingSeconds by remember { mutableStateOf(totalSeconds) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            remainingSeconds--
        }
        isFinished = true
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val progress = remainingSeconds.toFloat() / totalSeconds.toFloat()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isFinished) Completed.copy(alpha = 0.2f) else Surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isFinished) Icons.Default.CheckCircle else Icons.Default.Timer,
                        contentDescription = null,
                        tint = if (isFinished) Completed else Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFinished) "Repos terminé !" else "Repos",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isFinished) Completed else OnSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Fermer",
                        tint = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timer display
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.displayMedium,
                color = if (isFinished) Completed else Primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Barre de progression
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isFinished) Completed else Primary,
                trackColor = SurfaceVariant
            )

            if (isFinished) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Completed)
                ) {
                    Text("C'est parti !")
                }
            }
        }
    }
}


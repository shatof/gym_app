package com.gymtracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.data.model.ExerciseSet
import com.gymtracker.app.data.model.ExerciseWithSets
import com.gymtracker.app.ui.theme.*

/**
 * Card pour afficher un exercice avec ses séries
 */
@Composable
fun ExerciseCard(
    exerciseWithSets: ExerciseWithSets,
    onAddSet: () -> Unit,
    onDeleteExercise: () -> Unit,
    onSetUpdated: (ExerciseSet) -> Unit,
    onSetCompleted: (Long, Boolean) -> Unit,
    onDeleteSet: (ExerciseSet) -> Unit,
    onIncrementReps: (ExerciseSet) -> Unit,
    onDecrementReps: (ExerciseSet) -> Unit,
    onIncrementWeight: (ExerciseSet) -> Unit,
    onDecrementWeight: (ExerciseSet) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header avec nom de l'exercice
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exerciseWithSets.exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = OnSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // En-tête des colonnes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Série",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.width(40.dp)
                )
                Text(
                    "Poids",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Reps",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Text(
                    "Mio",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Liste des séries
            exerciseWithSets.sets.sortedBy { it.setNumber }.forEach { set ->
                SetRow(
                    set = set,
                    onSetUpdated = onSetUpdated,
                    onSetCompleted = { onSetCompleted(set.id, it) },
                    onDelete = { onDeleteSet(set) },
                    onIncrementReps = { onIncrementReps(set) },
                    onDecrementReps = { onDecrementReps(set) },
                    onIncrementWeight = { onIncrementWeight(set) },
                    onDecrementWeight = { onDecrementWeight(set) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Bouton ajouter série
            TextButton(
                onClick = onAddSet,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Ajouter une série")
            }
        }
    }
    
    // Dialog de confirmation suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'exercice ?") },
            text = { Text("Cette action supprimera ${exerciseWithSets.exercise.name} et toutes ses séries.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteExercise()
                    }
                ) {
                    Text("Supprimer", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/**
 * Ligne représentant une série
 */
@Composable
fun SetRow(
    set: ExerciseSet,
    onSetUpdated: (ExerciseSet) -> Unit,
    onSetCompleted: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onIncrementReps: () -> Unit,
    onDecrementReps: () -> Unit,
    onIncrementWeight: () -> Unit,
    onDecrementWeight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (set.isCompleted) Completed.copy(alpha = 0.2f) else SurfaceVariant
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numéro de série
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (set.isCompleted) Completed else Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.setNumber}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Poids avec boutons +/-
        NumberInputWithButtons(
            value = set.weight,
            onValueChange = { onSetUpdated(set.copy(weight = it)) },
            onIncrement = onIncrementWeight,
            onDecrement = onDecrementWeight,
            suffix = "kg",
            modifier = Modifier.weight(1f)
        )
        
        // Répétitions avec boutons +/-
        NumberInputWithButtons(
            value = set.reps.toFloat(),
            onValueChange = { onSetUpdated(set.copy(reps = it.toInt())) },
            onIncrement = onIncrementReps,
            onDecrement = onDecrementReps,
            isInteger = true,
            modifier = Modifier.weight(1f)
        )
        
        // Miorep (optionnel)
        MiorepInput(
            value = set.miorep,
            onValueChange = { onSetUpdated(set.copy(miorep = it)) },
            modifier = Modifier.width(50.dp)
        )
        
        // Checkbox de validation
        Checkbox(
            checked = set.isCompleted,
            onCheckedChange = onSetCompleted,
            colors = CheckboxDefaults.colors(
                checkedColor = Completed,
                uncheckedColor = OnSurfaceVariant
            )
        )
    }
}

/**
 * Input numérique avec boutons +/-
 */
@Composable
fun NumberInputWithButtons(
    value: Float,
    onValueChange: (Float) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    suffix: String = "",
    isInteger: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouton -
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Diminuer",
                tint = OnSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
        
        // Valeur
        Text(
            text = if (isInteger) "${value.toInt()}" else "%.1f".format(value) + if (suffix.isNotEmpty()) " $suffix" else "",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(if (suffix.isNotEmpty()) 60.dp else 40.dp)
        )
        
        // Bouton +
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Augmenter",
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Input pour Miorep
 */
@Composable
fun MiorepInput(
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(value) { mutableStateOf(value?.toString() ?: "") }
    
    BasicTextField(
        value = text,
        onValueChange = { newText ->
            text = newText.filter { it.isDigit() }.take(2)
            onValueChange(text.toIntOrNull())
        },
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Background)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = OnSurface,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        cursorBrush = SolidColor(Primary)
    )
}

/**
 * Bouton pour ajouter un exercice
 */
@Composable
fun AddExerciseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Primary
        )
    ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Ajouter un exercice", style = MaterialTheme.typography.titleMedium)
    }
}

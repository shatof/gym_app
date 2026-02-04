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
    onIncrementWeight: (ExerciseSet) -> Unit,
    onDecrementWeight: (ExerciseSet) -> Unit,
    onRestTimerStart: (Int) -> Unit = {}, // Callback pour démarrer le timer de repos
    onRestTimerStop: () -> Unit = {}, // Callback pour arrêter le timer de repos
    supersetColor: Color? = null, // Couleur du superset si applicable
    isCompactMode: Boolean = false, // Mode compact pour affichage superset côte à côte
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val supersetGroupId = exerciseWithSets.exercise.supersetGroupId

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp),
        border = if (supersetGroupId != null && supersetColor != null) {
            androidx.compose.foundation.BorderStroke(2.dp, supersetColor)
        } else null
    ) {
        Column {
            // Indicateur de superset (masqué en mode compact car affiché au niveau parent)
            if (supersetGroupId != null && supersetColor != null && !isCompactMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(supersetColor.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = supersetColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Superset $supersetGroupId",
                        style = MaterialTheme.typography.labelSmall,
                        color = supersetColor
                    )
                }
            }

            Column(
                modifier = Modifier.padding(if (isCompactMode) 8.dp else 16.dp)
            ) {
                // Header avec nom de l'exercice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = exerciseWithSets.exercise.name,
                        style = if (isCompactMode) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        maxLines = if (isCompactMode) 2 else Int.MAX_VALUE
                    )

                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = if (isCompactMode) Modifier.size(32.dp) else Modifier
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = OnSurfaceVariant,
                            modifier = if (isCompactMode) Modifier.size(18.dp) else Modifier
                        )
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactMode) 8.dp else 12.dp))

                // En-tête des colonnes (simplifié en mode compact)
                if (!isCompactMode) {
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
                }

                // Liste des séries
                exerciseWithSets.sets.sortedBy { it.setNumber }.forEach { set ->
                    if (isCompactMode) {
                        CompactSetRow(
                            set = set,
                            onSetUpdated = onSetUpdated,
                            onSetCompleted = { completed ->
                                onSetCompleted(set.id, completed)
                                if (completed) {
                                    onRestTimerStart(exerciseWithSets.exercise.restTimeSeconds)
                                } else {
                                    onRestTimerStop()
                                }
                            },
                            onDelete = { onDeleteSet(set) }
                        )
                    } else {
                        SetRow(
                            set = set,
                            onSetUpdated = onSetUpdated,
                            onSetCompleted = { completed ->
                                onSetCompleted(set.id, completed)
                                // Démarrer le timer de repos si le set vient d'être complété
                                if (completed) {
                                    onRestTimerStart(exerciseWithSets.exercise.restTimeSeconds)
                                } else {
                                    // Arrêter le timer si on décoche
                                    onRestTimerStop()
                                }
                            },
                            onDelete = { onDeleteSet(set) },
                            onIncrementWeight = { onIncrementWeight(set) },
                            onDecrementWeight = { onDecrementWeight(set) }
                        )
                    }
                    Spacer(modifier = Modifier.height(if (isCompactMode) 4.dp else 8.dp))
                }

                // Bouton ajouter série
                TextButton(
                    onClick = onAddSet,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = if (isCompactMode) Modifier.size(16.dp) else Modifier)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isCompactMode) "+" else "Ajouter une série", style = if (isCompactMode) MaterialTheme.typography.bodySmall else LocalTextStyle.current)
                }
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
    onIncrementWeight: () -> Unit,
    onDecrementWeight: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val backgroundColor = if (set.isCompleted) Completed.copy(alpha = 0.2f) else SurfaceVariant
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numéro de série avec long press pour supprimer
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (set.isCompleted) Completed else Primary)
                .clickable { showDeleteConfirm = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.setNumber}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Poids avec boutons +/-
        WeightInputWithButtons(
            value = set.weight,
            onValueChange = { onSetUpdated(set.copy(weight = it)) },
            onIncrement = onIncrementWeight,
            onDecrement = onDecrementWeight,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        // Répétitions - simple champ de saisie
        RepsInput(
            value = set.reps,
            onValueChange = { onSetUpdated(set.copy(reps = it)) },
            modifier = Modifier.width(60.dp)
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

    // Dialog de confirmation de suppression de série
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la série ?") },
            text = { Text("Voulez-vous supprimer la série ${set.setNumber} ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Supprimer", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/**
 * Ligne compacte pour les séries dans les supersets
 */
@Composable
fun CompactSetRow(
    set: ExerciseSet,
    onSetUpdated: (ExerciseSet) -> Unit,
    onSetCompleted: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val backgroundColor = if (set.isCompleted) Completed.copy(alpha = 0.2f) else SurfaceVariant

    var weightText by remember { mutableStateOf(if (set.weight == 0f) "" else "%.1f".format(set.weight).replace(",", ".")) }
    var repsText by remember { mutableStateOf(if (set.reps == 0) "" else set.reps.toString()) }

    // Synchroniser avec les valeurs externes
    val expectedWeightText = if (set.weight == 0f) "" else "%.1f".format(set.weight).replace(",", ".")
    if (weightText != expectedWeightText && (weightText.toFloatOrNull() ?: -1f) != set.weight) {
        weightText = expectedWeightText
    }
    val expectedRepsText = if (set.reps == 0) "" else set.reps.toString()
    if (repsText != expectedRepsText && (repsText.toIntOrNull() ?: -1) != set.reps) {
        repsText = expectedRepsText
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Numéro de série (cliquable pour supprimer)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (set.isCompleted) Completed else Primary)
                .clickable { showDeleteConfirm = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.setNumber}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }

        // Poids
        BasicTextField(
            value = weightText,
            onValueChange = { newText ->
                val filtered = newText.filter { it.isDigit() || it == '.' || it == ',' }.replace(",", ".").take(5)
                weightText = filtered
                onSetUpdated(set.copy(weight = filtered.toFloatOrNull() ?: 0f))
            },
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Background)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = OnSurface,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            cursorBrush = SolidColor(Primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (weightText.isEmpty()) {
                        Text("kg", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                    innerTextField()
                }
            }
        )

        // Reps
        BasicTextField(
            value = repsText,
            onValueChange = { newText ->
                val filtered = newText.filter { it.isDigit() }.take(3)
                repsText = filtered
                onSetUpdated(set.copy(reps = filtered.toIntOrNull() ?: 0))
            },
            modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Background)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = OnSurface,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(Primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.Center) {
                    if (repsText.isEmpty()) {
                        Text("reps", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    }
                    innerTextField()
                }
            }
        )

        // Checkbox
        Checkbox(
            checked = set.isCompleted,
            onCheckedChange = onSetCompleted,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Completed,
                uncheckedColor = OnSurfaceVariant
            )
        )
    }

    // Dialog de confirmation de suppression de série
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer la série ?") },
            text = { Text("Voulez-vous supprimer la série ${set.setNumber} ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Supprimer", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

/**
 * Input pour le poids avec boutons +/- (incréments de 1.25kg)
 */
@Composable
fun WeightInputWithButtons(
    value: Float,
    onValueChange: (Float) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Formater avec 1 décimale pour l'affichage (16.25 → 16.3)
    fun formatWeight(weight: Float): String {
        return if (weight == 0f) {
            ""
        } else if (weight == weight.toInt().toFloat()) {
            // Nombre entier
            weight.toInt().toString()
        } else {
            // Nombre décimal - arrondir à 1 décimale pour l'affichage
            String.format("%.1f", weight).replace(",", ".")
        }
    }

    // Utiliser key pour forcer la recomposition uniquement quand la valeur change significativement
    var text by remember { mutableStateOf(formatWeight(value)) }

    // Synchroniser le texte avec la valeur externe seulement si différent
    val formattedValue = formatWeight(value)
    if (text != formattedValue && (text.replace(",", ".").toFloatOrNull() ?: -1f) != value) {
        text = formattedValue
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bouton moins
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Diminuer",
                modifier = Modifier.size(16.dp),
                tint = Primary
            )
        }

        BasicTextField(
            value = text,
            onValueChange = { newText ->
                // Accepter les chiffres, un point ou une virgule
                val filtered = newText.filter { it.isDigit() || it == '.' || it == ',' }
                    .replace(",", ".")
                // Limiter à un seul point décimal
                val parts = filtered.split(".")
                val sanitized = if (parts.size > 2) {
                    parts[0] + "." + parts.drop(1).joinToString("")
                } else if (parts.size == 2 && parts[1].length > 2) {
                    // Limiter à 2 décimales
                    parts[0] + "." + parts[1].take(2)
                } else {
                    filtered
                }
                text = sanitized.take(7) // Limiter la longueur totale

                // Mettre à jour la valeur si c'est un nombre valide ou vide
                val floatValue = sanitized.toFloatOrNull() ?: 0f
                onValueChange(floatValue)
            },
            modifier = Modifier
                .width(55.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Background)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = OnSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            cursorBrush = SolidColor(Primary),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        )

        // Bouton plus
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Augmenter",
                modifier = Modifier.size(16.dp),
                tint = Primary
            )
        }
    }
}

/**
 * Input simple pour les répétitions
 */
@Composable
fun RepsInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(if (value == 0) "" else value.toString()) }

    // Synchroniser le texte avec la valeur externe seulement si différent
    val expectedText = if (value == 0) "" else value.toString()
    if (text != expectedText && (text.toIntOrNull() ?: -1) != value) {
        text = expectedText
    }

    BasicTextField(
        value = text,
        onValueChange = { newText ->
            val filtered = newText.filter { it.isDigit() }.take(3)
            text = filtered
            onValueChange(filtered.toIntOrNull() ?: 0)
        },
        modifier = modifier
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Background)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = OnSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        cursorBrush = SolidColor(Primary),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (text.isEmpty()) {
                    Text(
                        text = "reps",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                innerTextField()
            }
        }
    )
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
    var text by remember { mutableStateOf(value?.toString() ?: "") }

    // Synchroniser le texte avec la valeur externe seulement si différent
    val expectedText = value?.toString() ?: ""
    if (text != expectedText && text.toIntOrNull() != value) {
        text = expectedText
    }

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

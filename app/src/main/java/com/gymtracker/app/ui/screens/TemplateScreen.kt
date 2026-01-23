package com.gymtracker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymtracker.app.data.model.SessionTemplate
import com.gymtracker.app.data.model.TemplateExercise
import com.gymtracker.app.data.model.TemplateWithExercises
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.TemplateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateScreen(
    viewModel: TemplateViewModel,
    modifier: Modifier = Modifier
) {
    val templates by viewModel.allTemplatesWithExercises.collectAsState(initial = emptyList())
    val editingTemplateId by viewModel.editingTemplateId.collectAsState()
    val editingExercises by viewModel.editingTemplateExercises.collectAsState(initial = emptyList())
    val exerciseNames by viewModel.exerciseNames.collectAsState(initial = emptyList())

    var showCreateTemplateDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var templateToDelete by remember { mutableStateOf<SessionTemplate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editingTemplateId != null) "Édition de la séance" else "Mes Séances",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (editingTemplateId != null) {
                        IconButton(onClick = { viewModel.selectTemplateForEditing(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground
                )
            )
        },
        floatingActionButton = {
            if (editingTemplateId == null) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateTemplateDialog = true },
                    containerColor = Primary,
                    contentColor = OnPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nouvelle Séance")
                }
            } else {
                ExtendedFloatingActionButton(
                    onClick = { showAddExerciseDialog = true },
                    containerColor = Primary,
                    contentColor = OnPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter Exercice")
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->

        if (editingTemplateId == null) {
            // Liste des templates
            if (templates.isEmpty()) {
                EmptyTemplatesView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(templates, key = { it.template.id }) { templateWithExercises ->
                        TemplateCard(
                            templateWithExercises = templateWithExercises,
                            onClick = { viewModel.selectTemplateForEditing(templateWithExercises.template.id) },
                            onDelete = { templateToDelete = templateWithExercises.template }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        } else {
            // Édition d'un template
            val currentTemplate = templates.find { it.template.id == editingTemplateId }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Nom du template
                item {
                    currentTemplate?.let { template ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = template.template.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (template.template.description.isNotEmpty()) {
                                    Text(
                                        text = template.template.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${template.exercises.size} exercice(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Liste des exercices
                if (editingExercises.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aucun exercice\nAppuyez sur + pour en ajouter",
                                    textAlign = TextAlign.Center,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(editingExercises, key = { it.id }) { exercise ->
                        TemplateExerciseCard(
                            exercise = exercise,
                            onDelete = { viewModel.deleteTemplateExercise(exercise) },
                            onUpdateSetsCount = { newCount ->
                                viewModel.updateTemplateExercise(exercise.copy(defaultSetsCount = newCount))
                            },
                            onUpdateRestTime = { newRestTime ->
                                viewModel.updateTemplateExercise(exercise.copy(restTimeSeconds = newRestTime))
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Dialog création de template
    if (showCreateTemplateDialog) {
        CreateTemplateDialog(
            onDismiss = { showCreateTemplateDialog = false },
            onConfirm = { name, description ->
                viewModel.createTemplate(name, description)
                showCreateTemplateDialog = false
            }
        )
    }

    // Dialog ajout d'exercice
    if (showAddExerciseDialog) {
        AddTemplateExerciseDialog(
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name, setsCount, restTimeSeconds ->
                viewModel.addExerciseToTemplate(name, setsCount, restTimeSeconds)
                showAddExerciseDialog = false
            },
            existingExerciseNames = exerciseNames
        )
    }

    // Dialog confirmation suppression
    templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text("Supprimer la séance ?") },
            text = { Text("La séance \"${template.name}\" sera définitivement supprimée.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTemplate(template)
                        templateToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun EmptyTemplatesView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Aucune séance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Créez des séances types\npour gagner du temps",
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateCard(
    templateWithExercises: TemplateWithExercises,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = templateWithExercises.template.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (templateWithExercises.template.description.isNotEmpty()) {
                    Text(
                        text = templateWithExercises.template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${templateWithExercises.exercises.size} exercice(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary
                )

                if (templateWithExercises.exercises.isNotEmpty()) {
                    Text(
                        text = templateWithExercises.exercises.take(3).joinToString(", ") { it.name } +
                                if (templateWithExercises.exercises.size > 3) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Row {
                IconButton(onClick = onClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Éditer",
                        tint = Primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateExerciseCard(
    exercise: TemplateExercise,
    onDelete: () -> Unit,
    onUpdateSetsCount: (Int) -> Unit,
    onUpdateRestTime: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Séries:",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )

                    IconButton(
                        onClick = { if (exercise.defaultSetsCount > 1) onUpdateSetsCount(exercise.defaultSetsCount - 1) },
                        modifier = Modifier.size(32.dp),
                        enabled = exercise.defaultSetsCount > 1
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Moins",
                            modifier = Modifier.size(16.dp),
                            tint = if (exercise.defaultSetsCount > 1) OnSurfaceVariant else OnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    }

                    Text(
                        text = "${exercise.defaultSetsCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { onUpdateSetsCount(exercise.defaultSetsCount + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                    }
                }

                // Temps de repos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OnSurfaceVariant
                    )
                    Text(
                        text = "Repos:",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )

                    IconButton(
                        onClick = { if (exercise.restTimeSeconds > 15) onUpdateRestTime(exercise.restTimeSeconds - 15) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Moins", modifier = Modifier.size(16.dp))
                    }

                    Text(
                        text = "${exercise.restTimeSeconds}s",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = { onUpdateRestTime(exercise.restTimeSeconds + 15) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(16.dp))
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTemplateDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle Séance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la séance") },
                    placeholder = { Text("Ex: Push, Pull, Legs...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optionnel)") },
                    placeholder = { Text("Ex: Séance pectoraux, épaules, triceps") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name.trim(), description.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Créer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTemplateExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, setsCount: Int, restTimeSeconds: Int) -> Unit,
    existingExerciseNames: List<String>
) {
    var name by remember { mutableStateOf("") }
    var setsCount by remember { mutableStateOf("3") }
    var restTimeSeconds by remember { mutableStateOf("180") }
    var expanded by remember { mutableStateOf(false) }

    // Prioriser : exercices par défaut > exercices existants > tous les exercices
    val filteredNames = remember(name, existingExerciseNames) {
        if (name.isBlank()) {
            emptyList()
        } else {
            val defaultExercises = com.gymtracker.app.data.SettingsManager.DEFAULT_EXERCISES
            val allExercises = com.gymtracker.app.data.SettingsManager.ALL_EXERCISES

            // D'abord les exercices par défaut qui matchent
            val fromDefault = defaultExercises.filter {
                it.contains(name, ignoreCase = true) && it != name
            }
            // Ensuite les exercices existants
            val fromExisting = existingExerciseNames.filter {
                it.contains(name, ignoreCase = true) && it != name && !fromDefault.contains(it)
            }
            // Enfin tous les autres
            val fromAll = allExercises.filter {
                it.contains(name, ignoreCase = true) && it != name && !fromDefault.contains(it) && !fromExisting.contains(it)
            }

            (fromDefault + fromExisting + fromAll).take(3)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un exercice") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded && filteredNames.isNotEmpty(),
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            expanded = true
                        },
                        label = { Text("Nom de l'exercice") },
                        placeholder = { Text("Ex: Développé couché") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded && filteredNames.isNotEmpty(),
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredNames.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    name = suggestion
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = setsCount,
                    onValueChange = {
                        if (it.isEmpty() || it.toIntOrNull() != null) {
                            setsCount = it
                        }
                    },
                    label = { Text("Nombre de séries") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = restTimeSeconds,
                    onValueChange = {
                        restTimeSeconds = it.filter { c -> c.isDigit() }.take(3)
                    },
                    label = { Text("Temps de repos (secondes)") },
                    placeholder = { Text("180") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Timer, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(name.trim(), setsCount.toIntOrNull() ?: 3, restTimeSeconds.toIntOrNull() ?: 180)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

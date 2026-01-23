package com.gymtracker.app.ui.screens

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.gymtracker.app.data.SettingsManager
import com.gymtracker.app.data.ThemeColor
import com.gymtracker.app.data.model.ExportData
import com.gymtracker.app.data.repository.GymRepository
import com.gymtracker.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    repository: GymRepository,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isDarkTheme by settingsManager.isDarkTheme.collectAsState(initial = true)
    val themeColor by settingsManager.themeColor.collectAsState(initial = ThemeColor.GREEN)
    val commonExercises by settingsManager.commonExercises.collectAsState(initial = SettingsManager.DEFAULT_EXERCISES)
    val welcomeText by settingsManager.welcomeText.collectAsState(initial = SettingsManager.DEFAULT_WELCOME_TEXT)
    val welcomeImageUri by settingsManager.welcomeImageUri.collectAsState(initial = null)

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToRemove by remember { mutableStateOf<String?>(null) }
    var showDeleteDataDialog by remember { mutableStateOf(false) }
    var showImportResultDialog by remember { mutableStateOf<String?>(null) }
    var showWelcomeTextDialog by remember { mutableStateOf(false) }

    // Launcher pour sélectionner le fichier d'import
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val result = importData(context, repository, it)
                showImportResultDialog = result
            }
        }
    }

    // Launcher pour sélectionner l'image d'accueil
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Prendre les permissions persistantes pour l'URI
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignorer si les permissions persistantes ne sont pas disponibles
            }
            scope.launch {
                settingsManager.setWelcomeImageUri(it.toString())
                onShowSnackbar("Image d'accueil mise à jour")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Paramètres") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Section Export/Sauvegarde
            item {
                SettingsSection(title = "Sauvegarde") {
                    SettingsItem(
                        icon = Icons.Default.Upload,
                        title = "Exporter les données",
                        subtitle = "Historique, templates et performances",
                        onClick = {
                            scope.launch {
                                exportData(context, repository, onShowSnackbar)
                            }
                        }
                    )

                    Divider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                    SettingsItem(
                        icon = Icons.Default.Download,
                        title = "Importer les données",
                        subtitle = "Restaurer depuis un fichier JSON",
                        onClick = {
                            importLauncher.launch("application/json")
                        }
                    )
                }
            }

            // Section Apparence
            item {
                SettingsSection(title = "Apparence") {
                    // Thème sombre/clair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "Thème sombre",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (isDarkTheme) "Activé" else "Désactivé",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { newValue ->
                                scope.launch {
                                    settingsManager.setDarkTheme(newValue)
                                    updateThemeMode(newValue)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Primary,
                                checkedTrackColor = Primary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    Divider(color = SurfaceVariant)

                    // Couleur du thème
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                "Couleur principale",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(ThemeColor.entries.toList()) { color ->
                                ColorOption(
                                    color = color,
                                    isSelected = themeColor == color,
                                    onClick = {
                                        scope.launch {
                                            settingsManager.setThemeColor(color)
                                            updatePrimaryColor(color)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section Personnalisation
            item {
                SettingsSection(title = "Personnalisation") {
                    // Texte d'accueil
                    SettingsItem(
                        icon = Icons.Default.TextFields,
                        title = "Texte d'accueil",
                        subtitle = welcomeText,
                        onClick = { showWelcomeTextDialog = true }
                    )

                    Divider(color = SurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))

                    // Image d'accueil
                    SettingsItem(
                        icon = Icons.Default.Image,
                        title = "Image d'accueil",
                        subtitle = if (welcomeImageUri != null) "Image personnalisée" else "Icône par défaut",
                        onClick = { imageLauncher.launch("image/*") }
                    )

                    if (welcomeImageUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                scope.launch {
                                    settingsManager.setWelcomeImageUri(null)
                                    onShowSnackbar("Image d'accueil supprimée")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Supprimer l'image", color = Error)
                        }
                    }
                }
            }

            // Section Exercices par défaut
            item {
                SettingsSection(title = "Exercices par défaut") {
                    Text(
                        "Ces exercices sont proposés lors de l'ajout d'un nouvel exercice",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    commonExercises.forEach { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                exercise,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = { exerciseToRemove = exercise }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Supprimer",
                                    tint = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showAddExerciseDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ajouter un exercice")
                    }
                }
            }

            // Section Zone dangereuse
            item {
                SettingsSection(title = "Zone dangereuse") {
                    SettingsItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Supprimer toutes les données",
                        subtitle = "Cette action est irréversible",
                        onClick = { showDeleteDataDialog = true }
                    )
                }
            }

            // Info version
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "gros biscoto mdr v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Dialog ajouter exercice avec autocomplétion
    if (showAddExerciseDialog) {
        var newExercise by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        val filteredExercises = SettingsManager.ALL_EXERCISES.filter {
            it.contains(newExercise, ignoreCase = true) && !commonExercises.contains(it)
        }.take(6)

        AlertDialog(
            onDismissRequest = { showAddExerciseDialog = false },
            title = { Text("Ajouter un exercice") },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expanded && filteredExercises.isNotEmpty() && newExercise.isNotEmpty(),
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = newExercise,
                            onValueChange = {
                                newExercise = it
                                expanded = true
                            },
                            label = { Text("Nom de l'exercice") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded && filteredExercises.isNotEmpty() && newExercise.isNotEmpty(),
                            onDismissRequest = { expanded = false }
                        ) {
                            filteredExercises.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        newExercise = suggestion
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newExercise.isNotBlank()) {
                            scope.launch {
                                settingsManager.addCommonExercise(newExercise.trim())
                            }
                            showAddExerciseDialog = false
                        }
                    },
                    enabled = newExercise.isNotBlank()
                ) {
                    Text("Ajouter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExerciseDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog confirmation suppression exercice
    exerciseToRemove?.let { exercise ->
        AlertDialog(
            onDismissRequest = { exerciseToRemove = null },
            title = { Text("Supprimer l'exercice ?") },
            text = { Text("\"$exercise\" ne sera plus proposé par défaut.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            settingsManager.removeCommonExercise(exercise)
                        }
                        exerciseToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToRemove = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog confirmation suppression de toutes les données
    if (showDeleteDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDataDialog = false },
            title = { Text("Supprimer toutes les données ?") },
            text = {
                Text(
                    "Cette action supprimera définitivement :\n" +
                    "• Tout l'historique des séances\n" +
                    "• Tous les templates\n" +
                    "• Toutes les performances\n\n" +
                    "Cette action est IRRÉVERSIBLE."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            repository.deleteAllData()
                            onShowSnackbar("Toutes les données ont été supprimées")
                        }
                        showDeleteDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Supprimer tout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDataDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Dialog résultat import
    showImportResultDialog?.let { result ->
        AlertDialog(
            onDismissRequest = { showImportResultDialog = null },
            title = { Text("Import terminé") },
            text = { Text(result) },
            confirmButton = {
                Button(onClick = { showImportResultDialog = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Dialog modification texte d'accueil
    if (showWelcomeTextDialog) {
        var newWelcomeText by remember { mutableStateOf(welcomeText) }

        AlertDialog(
            onDismissRequest = { showWelcomeTextDialog = false },
            title = { Text("Modifier le texte d'accueil") },
            text = {
                OutlinedTextField(
                    value = newWelcomeText,
                    onValueChange = { newWelcomeText = it },
                    label = { Text("Texte d'accueil") },
                    placeholder = { Text(SettingsManager.DEFAULT_WELCOME_TEXT) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            settingsManager.setWelcomeText(newWelcomeText.trim())
                            onShowSnackbar("Texte d'accueil mis à jour")
                        }
                        showWelcomeTextDialog = false
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWelcomeTextDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = OnSurfaceVariant
        )
    }
}

@Composable
private fun ColorOption(
    color: ThemeColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorValue = when (color) {
        ThemeColor.GREEN -> ThemeColors.GreenPrimary
        ThemeColor.BLUE -> ThemeColors.BluePrimary
        ThemeColor.PURPLE -> ThemeColors.PurplePrimary
        ThemeColor.PINK -> ThemeColors.PinkPrimary
        ThemeColor.ORANGE -> ThemeColors.OrangePrimary
        ThemeColor.RED -> ThemeColors.RedPrimary
        ThemeColor.TEAL -> ThemeColors.TealPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colorValue)
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, OnSurface, CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Sélectionné",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            color.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) Primary else OnSurfaceVariant
        )
    }
}

private suspend fun exportData(
    context: Context,
    repository: GymRepository,
    onShowSnackbar: (String) -> Unit
) {
    try {
        val exportData = repository.exportAllData()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(exportData)

        val dateFormat = SimpleDateFormat("dd-MM-yy-HH'h'mm", Locale.getDefault())
        val fileName = "${dateFormat.format(Date())}.json"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                onShowSnackbar("Export enregistré dans Téléchargements : $fileName")
            } ?: run {
                onShowSnackbar("Erreur : impossible de créer le fichier")
            }
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(json.toByteArray()) }
            onShowSnackbar("Export enregistré dans Téléchargements : $fileName")
        }
    } catch (e: Exception) {
        onShowSnackbar("Erreur lors de l'export : ${e.message}")
    }
}

private suspend fun importData(
    context: Context,
    repository: GymRepository,
    uri: Uri
): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val json = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
        inputStream?.close()

        if (json.isBlank()) {
            return "Erreur : fichier vide"
        }

        val gson = Gson()
        val exportData = gson.fromJson(json, ExportData::class.java)

        val importedCount = repository.importData(exportData)

        "Import réussi !\n• ${importedCount.first} séance(s) importée(s)\n• ${importedCount.second} exercice(s) importé(s)"
    } catch (e: Exception) {
        "Erreur lors de l'import : ${e.message}"
    }
}


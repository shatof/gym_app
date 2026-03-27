package com.gymtracker.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymtracker.app.data.model.*
import com.gymtracker.app.data.SettingsManager
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.MuscleGroupStats
import com.gymtracker.app.ui.viewmodel.ProgressViewModel
import com.gymtracker.app.ui.viewmodel.ProfileViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    profileViewModel: ProfileViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()

    val exerciseNames by viewModel.exerciseNames.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val progressData by viewModel.progressData.collectAsState(initial = null)
    val muscleGroupStats by viewModel.muscleGroupStats.collectAsState(initial = emptyList())
    val weeklyStats by viewModel.weeklyStats.collectAsState()

    // Onglets internes : Stats et Profil
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("📊 Stats", "👤 Profil")

    // Charger les mappings personnalisés au démarrage
    LaunchedEffect(Unit) {
        settingsManager.loadCustomMuscleGroupMappings()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            if (selectedTab == 0) "Progression" else "Profil & Badges",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Background,
                        titleContentColor = OnBackground
                    )
                )
                // Onglets Stats / Profil
                if (profileViewModel != null) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Background,
                        contentColor = Primary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                selectedContentColor = Primary,
                                unselectedContentColor = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        
        if (selectedTab == 0) {
            // Onglet Stats
            StatsContent(
                exerciseNames = exerciseNames,
                selectedExercise = selectedExercise,
                progressData = progressData,
                muscleGroupStats = muscleGroupStats,
                weeklyStats = weeklyStats,
                viewModel = viewModel,
                settingsManager = settingsManager,
                paddingValues = paddingValues,
                scope = scope
            )
        } else if (profileViewModel != null) {
            // Onglet Profil
            ProfileContent(
                viewModel = profileViewModel,
                paddingValues = paddingValues
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(
    exerciseNames: List<String>,
    selectedExercise: String?,
    progressData: ExerciseProgress?,
    muscleGroupStats: List<MuscleGroupStats>,
    weeklyStats: com.gymtracker.app.data.model.WeeklyStats?,
    viewModel: ProgressViewModel,
    settingsManager: SettingsManager,
    paddingValues: PaddingValues,
    scope: kotlinx.coroutines.CoroutineScope
) {
        if (exerciseNames.isEmpty()) {
            // Pas de données
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
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Pas encore de données",
                        style = MaterialTheme.typography.titleLarge,
                        color = OnSurfaceVariant
                    )
                    
                    Text(
                        text = "Terminez des séances pour voir\nvotre progression",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // État pour la section rétractable des stats par groupe musculaire
            var showMuscleGroupStats by remember { mutableStateOf(false) }
            var selectedMuscleGroup by remember { mutableStateOf<SettingsManager.Companion.MuscleGroup?>(null) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // ── Objectifs de la semaine ──────────────────────────────────
                weeklyStats?.let { stats ->
                    item {
                        WeeklyGoalsSection(stats = stats)
                    }
                }

                // Bouton pour afficher/masquer les statistiques par groupe musculaire
                if (muscleGroupStats.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showMuscleGroupStats = !showMuscleGroupStats },
                            colors = CardDefaults.cardColors(containerColor = Surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = Primary
                                    )
                                    Column {
                                        Text(
                                            text = "Séries par groupe musculaire",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${muscleGroupStats.sumOf { it.totalSets }} séries au total",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                                Icon(
                                    if (showMuscleGroupStats) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showMuscleGroupStats) "Masquer" else "Afficher",
                                    tint = OnSurfaceVariant
                                )
                            }
                        }
                    }

                    // Contenu rétractable
                    if (showMuscleGroupStats) {
                        item {
                            MuscleGroupStatsCard(
                                stats = muscleGroupStats,
                                onMuscleGroupClick = { group ->
                                    selectedMuscleGroup = if (selectedMuscleGroup == group) null else group
                                },
                                selectedGroup = selectedMuscleGroup,
                                exerciseNames = exerciseNames,
                                onExerciseClick = { exerciseName ->
                                    viewModel.selectExercise(exerciseName)
                                    showMuscleGroupStats = false
                                    selectedMuscleGroup = null
                                },
                                onChangeExerciseMuscleGroup = { exerciseName, newGroup ->
                                    scope.launch {
                                        settingsManager.setExerciseMuscleGroup(exerciseName, newGroup)
                                    }
                                }
                            )
                        }
                    }
                }

                // Sélecteur d'exercice avec recherche
                item {
                    var searchQuery by remember { mutableStateOf("") }
                    var showSuggestions by remember { mutableStateOf(false) }

                    Text(
                        text = "Sélectionnez un exercice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Champ de recherche
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            showSuggestions = it.length >= 2
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Rechercher un exercice...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    showSuggestions = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            cursorColor = Primary
                        )
                    )

                    // Suggestions de recherche
                    if (showSuggestions && searchQuery.length >= 2) {
                        val filteredExercises = exerciseNames.filter {
                            it.lowercase().contains(searchQuery.lowercase())
                        }.take(5)

                        if (filteredExercises.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Surface)
                            ) {
                                Column {
                                    filteredExercises.forEach { name ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.selectExercise(name)
                                                    searchQuery = name
                                                    showSuggestions = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                tint = Primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(name)
                                        }
                                        if (name != filteredExercises.last()) {
                                            Divider()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Liste horizontale des exercices (chips)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(exerciseNames) { name ->
                            FilterChip(
                                selected = selectedExercise == name,
                                onClick = { viewModel.selectExercise(name) },
                                label = { Text(name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = OnPrimary
                                )
                            )
                        }
                    }
                }
                
                // Graphiques et stats
                progressData?.let { progress ->
                    if (progress.dataPoints.isNotEmpty()) {
                        // Stats récapitulatives
                        item {
                            ProgressSummaryCard(progress = progress)
                        }
                        
                        // Graphique du poids max
                        item {
                            ProgressChartCard(
                                title = "Poids maximum",
                                dataPoints = progress.dataPoints,
                                getValue = { it.maxWeight },
                                unit = "kg",
                                color = ChartLine1
                            )
                        }
                        
                        // Graphique du volume total
                        item {
                            ProgressChartCard(
                                title = "Volume total",
                                dataPoints = progress.dataPoints,
                                getValue = { it.totalVolume },
                                unit = "kg",
                                color = ChartLine2
                            )
                        }
                        
                        // Graphique du 1RM estimé
                        item {
                            ProgressChartCard(
                                title = "1RM estimé",
                                dataPoints = progress.dataPoints,
                                getValue = { it.bestSet.estimated1RM },
                                unit = "kg",
                                color = ChartLine3
                            )
                        }
                        
                        // Historique détaillé
                        item {
                            Text(
                                text = "Historique détaillé",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        items(progress.dataPoints.reversed()) { dataPoint ->
                            DataPointCard(dataPoint = dataPoint)
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Surface)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Pas de données pour cet exercice",
                                        color = OnSurfaceVariant
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressSummaryCard(
    progress: ExerciseProgress,
    modifier: Modifier = Modifier
) {
    val dataPoints = progress.dataPoints
    val latestData = dataPoints.lastOrNull()
    val firstData = dataPoints.firstOrNull()
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = progress.exerciseName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Poids max actuel
                StatColumn(
                    label = "Poids max",
                    value = "${latestData?.maxWeight ?: 0}",
                    unit = "kg",
                    icon = Icons.Default.FitnessCenter
                )
                
                // 1RM estimé
                StatColumn(
                    label = "1RM estimé",
                    value = "%.1f".format(latestData?.bestSet?.estimated1RM ?: 0f),
                    unit = "kg",
                    icon = Icons.Default.EmojiEvents
                )
                
                // Progression
                if (firstData != null && latestData != null && dataPoints.size > 1) {
                    val progression = latestData.maxWeight - firstData.maxWeight
                    StatColumn(
                        label = "Progression",
                        value = "${if (progression >= 0) "+" else ""}${"%.1f".format(progression)}",
                        unit = "kg",
                        icon = if (progression >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        valueColor = if (progression >= 0) Completed else Error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${dataPoints.size} séances enregistrées",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = OnSurface,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = " $unit",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OnSurfaceVariant
        )
    }
}

@Composable
fun ProgressChartCard(
    title: String,
    dataPoints: List<ProgressDataPoint>,
    getValue: (ProgressDataPoint) -> Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    
    LaunchedEffect(dataPoints) {
        val entries = dataPoints.mapIndexed { index, point ->
            entryOf(index.toFloat(), getValue(point))
        }
        chartEntryModelProducer.setEntries(entries)
    }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
    
    val axisValueFormatter = remember(dataPoints) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            dataPoints.getOrNull(value.toInt())?.let { 
                dateFormat.format(Date(it.date)) 
            } ?: ""
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (dataPoints.size >= 2) {
                Chart(
                    chart = lineChart(
                        lines = listOf(
                            lineSpec(
                                lineColor = color,
                                lineBackgroundShader = DynamicShaders.fromBrush(
                                    Brush.verticalGradient(
                                        listOf(
                                            color.copy(alpha = 0.4f),
                                            color.copy(alpha = 0f)
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    chartModelProducer = chartEntryModelProducer,
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = axisValueFormatter
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Minimum 2 séances requises pour le graphique",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DataPointCard(
    dataPoint: ProgressDataPoint,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dateFormat.format(Date(dataPoint.date)),
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${dataPoint.bestSet.weight} kg",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val repsText = if (dataPoint.bestSet.miorep != null && dataPoint.bestSet.miorep > 0) {
                        "${dataPoint.bestSet.reps} reps + ${dataPoint.bestSet.miorep} mio"
                    } else {
                        "${dataPoint.bestSet.reps} reps"
                    }
                    Text(
                        text = repsText,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "1RM: ${"%.1f".format(dataPoint.bestSet.estimated1RM)} kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary
                    )
                    Text(
                        text = "Vol: ${"%.0f".format(dataPoint.totalVolume)} kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Card affichant les statistiques par groupe musculaire avec détails cliquables
 */
@Composable
fun MuscleGroupStatsCard(
    stats: List<MuscleGroupStats>,
    onMuscleGroupClick: (SettingsManager.Companion.MuscleGroup) -> Unit,
    selectedGroup: SettingsManager.Companion.MuscleGroup?,
    exerciseNames: List<String>,
    onExerciseClick: (String) -> Unit,
    onChangeExerciseMuscleGroup: ((String, SettingsManager.Companion.MuscleGroup) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var exerciseToReclassify by remember { mutableStateOf<String?>(null) }

    val muscleGroupColors = mapOf(
        SettingsManager.Companion.MuscleGroup.CHEST to Color(0xFFE91E63),
        SettingsManager.Companion.MuscleGroup.BACK to Color(0xFF2196F3),
        SettingsManager.Companion.MuscleGroup.SHOULDERS to Color(0xFFFF9800),
        SettingsManager.Companion.MuscleGroup.BICEPS to Color(0xFF9C27B0),
        SettingsManager.Companion.MuscleGroup.TRICEPS to Color(0xFF00BCD4),
        SettingsManager.Companion.MuscleGroup.LEGS to Color(0xFF4CAF50),
        SettingsManager.Companion.MuscleGroup.ABS to Color(0xFFFFEB3B),
        SettingsManager.Companion.MuscleGroup.OTHER to Color(0xFF9E9E9E)
    )

    val maxSets = stats.maxOfOrNull { it.totalSets } ?: 1

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stats.forEach { stat ->
                val color = muscleGroupColors[stat.muscleGroup] ?: Primary
                val progress = stat.totalSets.toFloat() / maxSets.toFloat()
                val isSelected = selectedGroup == stat.muscleGroup

                // Exercices de ce groupe musculaire
                val exercisesInGroup = exerciseNames.filter { name ->
                    SettingsManager.getMuscleGroup(name) == stat.muscleGroup
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable { onMuscleGroupClick(stat.muscleGroup) }
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(color)
                            )
                            Text(
                                text = stat.muscleGroup.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stat.totalSets} séries",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                            Text(
                                text = "(~${"%.1f".format(stat.averageSetsPerWorkout)}/séance)",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant
                            )
                            Icon(
                                if (isSelected) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = OnSurfaceVariant
                            )
                        }
                    }

                    // Barre de progression
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(color)
                        )
                    }

                    // Détails des exercices si sélectionné
                    if (isSelected && exercisesInGroup.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Exercices (${exercisesInGroup.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        exercisesInGroup.forEach { exerciseName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { onExerciseClick(exerciseName) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = color
                                    )
                                    Text(
                                        text = exerciseName,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Bouton pour changer le groupe musculaire
                                    if (onChangeExerciseMuscleGroup != null) {
                                        IconButton(
                                            onClick = { exerciseToReclassify = exerciseName },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Changer le groupe musculaire",
                                                modifier = Modifier.size(14.dp),
                                                tint = OnSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = "Voir progression",
                                        modifier = Modifier.size(14.dp),
                                        tint = Primary
                                    )
                                }
                            }
                        }

                        if (exercisesInGroup.isEmpty()) {
                            Text(
                                text = "Aucun exercice enregistré",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            if (stats.isNotEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Total
                val totalSets = stats.sumOf { it.totalSets }
                val totalWorkouts = stats.maxOfOrNull { it.totalWorkouts } ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalSets séries sur $totalWorkouts séance(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Primary
                    )
                }
            }
        }
    }

    // Dialog pour reclassifier un exercice
    exerciseToReclassify?.let { exerciseName ->
        AlertDialog(
            onDismissRequest = { exerciseToReclassify = null },
            title = { Text("Classer \"$exerciseName\"") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Choisissez le groupe musculaire :",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsManager.Companion.MuscleGroup.entries.forEach { group ->
                        val groupColor = muscleGroupColors[group] ?: Primary
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onChangeExerciseMuscleGroup?.invoke(exerciseName, group)
                                    exerciseToReclassify = null
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(groupColor)
                            )
                            Text(
                                text = group.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { exerciseToReclassify = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}

// === CONTENU DE L'ONGLET PROFIL ===

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    viewModel: ProfileViewModel,
    paddingValues: PaddingValues
) {
    val measurements by viewModel.allMeasurements.collectAsState()
    val latestMeasurement by viewModel.latestMeasurement.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val unlockedCount by viewModel.unlockedBadgesCount.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    val measurementToEdit by viewModel.measurementToEdit.collectAsState()
    val lastMonthStats by viewModel.lastMonthStats.collectAsState()

    var showMeasurementDialog by remember { mutableStateOf(false) }
    var showAllMeasurements by remember { mutableStateOf(false) }
    var selectedBadgeCategory by remember { mutableStateOf<BadgeCategory?>(null) }

    // Recharger les stats
    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // === SECTION STATISTIQUES RAPIDES ===
        item {
            ProfileStatsOverviewCard(userStats = userStats, unlockedBadges = unlockedCount, totalBadges = badges.size)
        }

        // === NIVEAU ATHLÈTE ===
        item {
            AthleteLevelCard(userStats = userStats)
        }

        // === RÉCAPITULATIF MENSUEL ===
        val monthStats = lastMonthStats
        if (monthStats != null && monthStats.sessionsCount > 0) {
            item {
                MonthlyRecapCard(stats = monthStats)
            }
        }

        // === DÉFIS PERSONNELS ===
        item {
            PersonalChallengesCard(userStats = userStats)
        }

        // === SECTION BADGES ===
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🏆 Badges",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "$unlockedCount/${badges.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Filtres par catégorie
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedBadgeCategory == null,
                                onClick = { selectedBadgeCategory = null },
                                label = { Text("Tous") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = OnPrimary
                                )
                            )
                        }
                        items(BadgeCategory.entries.toList()) { category ->
                            FilterChip(
                                selected = selectedBadgeCategory == category,
                                onClick = { selectedBadgeCategory = category },
                                label = { Text(category.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = OnPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grille de badges
                    val filteredBadges = if (selectedBadgeCategory != null) {
                        badges.filter { it.category == selectedBadgeCategory }
                    } else {
                        badges
                    }

                    val sortedBadges = filteredBadges.sortedByDescending { it.isUnlocked }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sortedBadges.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { badge ->
                                    ProfileBadgeItem(
                                        badge = badge,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // === SECTION MENSURATIONS ===
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📏 Mensurations",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            IconButton(onClick = {
                                viewModel.setMeasurementToEdit(null)
                                showMeasurementDialog = true
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Ajouter", tint = Primary)
                            }
                            if (measurements.isNotEmpty()) {
                                IconButton(onClick = { showAllMeasurements = !showAllMeasurements }) {
                                    Icon(
                                        if (showAllMeasurements) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    if (latestMeasurement != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ProfileLatestMeasurementDisplay(
                            measurement = latestMeasurement!!,
                            onEdit = {
                                viewModel.setMeasurementToEdit(latestMeasurement)
                                showMeasurementDialog = true
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📐", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Aucune mensuration",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Historique des mensurations
        if (showAllMeasurements && measurements.size > 1) {
            item {
                Text(
                    "Historique des mensurations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(measurements.drop(1)) { measurement ->
                ProfileMeasurementHistoryItem(
                    measurement = measurement,
                    onEdit = {
                        viewModel.setMeasurementToEdit(measurement)
                        showMeasurementDialog = true
                    },
                    onDelete = { viewModel.deleteMeasurement(measurement) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog pour ajouter/modifier une mensuration
    if (showMeasurementDialog) {
        ProfileMeasurementDialog(
            measurement = measurementToEdit,
            onDismiss = { showMeasurementDialog = false },
            onSave = { measurement ->
                viewModel.saveMeasurement(measurement)
                showMeasurementDialog = false
            }
        )
    }
}

@Composable
private fun ProfileStatsOverviewCard(
    userStats: UserStats,
    unlockedBadges: Int,
    totalBadges: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📊 Vue d'ensemble",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(value = "${userStats.totalWorkouts}", label = "Séances", icon = "💪")
                ProfileStatItem(value = "${userStats.currentStreak}", label = "Série", icon = "🔥")
                ProfileStatItem(value = "$unlockedBadges/$totalBadges", label = "Badges", icon = "🏆")
                ProfileStatItem(value = "${userStats.uniqueExercises}", label = "Exercices", icon = "🎯")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progression badges", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text(
                        "${((unlockedBadges.toFloat() / totalBadges) * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = unlockedBadges.toFloat() / totalBadges,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Primary,
                    trackColor = SurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileStatItem(value: String, label: String, icon: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 24.sp)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
    }
}

@Composable
private fun ProfileBadgeItem(badge: Badge, modifier: Modifier = Modifier) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.clickable { showDetails = !showDetails }.animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.isUnlocked) Primary.copy(alpha = 0.15f) else SurfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (badge.isUnlocked) Primary.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f))
                    .then(if (badge.isUnlocked) Modifier.border(2.dp, Primary, CircleShape) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Text(badge.icon, fontSize = 24.sp, modifier = Modifier.alpha(if (badge.isUnlocked) 1f else 0.4f))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                badge.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = if (badge.isUnlocked) OnSurface else OnSurfaceVariant,
                maxLines = 2
            )

            if (showDetails) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(badge.description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = OnSurfaceVariant)

                if (!badge.isUnlocked && badge.progress > 0f) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = badge.progress,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                        color = Primary,
                        trackColor = SurfaceVariant
                    )
                    Text("${(badge.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }

                if (badge.isUnlocked && badge.unlockedDate != null) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text("Débloqué le ${dateFormat.format(Date(badge.unlockedDate))}", style = MaterialTheme.typography.labelSmall, color = Primary)
                }
            }
        }
    }
}

@Composable
private fun ProfileLatestMeasurementDisplay(measurement: Measurement, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dernière mesure : ${dateFormat.format(Date(measurement.date))}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val measurementItems = listOfNotNull(
            measurement.weight?.let { ProfileMeasurementDisplayItem("Poids", "%.1f kg".format(it), "⚖️") },
            measurement.bodyFat?.let { ProfileMeasurementDisplayItem("Masse grasse", "%.1f %%".format(it), "📊") },
            measurement.chest?.let { ProfileMeasurementDisplayItem("Poitrine", "%.1f cm".format(it), "🫁") },
            measurement.waist?.let { ProfileMeasurementDisplayItem("Taille", "%.1f cm".format(it), "📏") },
            measurement.hips?.let { ProfileMeasurementDisplayItem("Hanches", "%.1f cm".format(it), "🍑") },
            measurement.shoulders?.let { ProfileMeasurementDisplayItem("Épaules", "%.1f cm".format(it), "🎯") },
            measurement.armLeft?.let { ProfileMeasurementDisplayItem("Bras G", "%.1f cm".format(it), "💪") },
            measurement.armRight?.let { ProfileMeasurementDisplayItem("Bras D", "%.1f cm".format(it), "💪") }
        )

        if (measurementItems.isNotEmpty()) {
            measurementItems.chunked(3).forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { item ->
                        ProfileMeasurementChip(item = item, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (measurement.notes.isNotBlank()) {
            Text("📝 ${measurement.notes}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
    }
}

private data class ProfileMeasurementDisplayItem(val label: String, val value: String, val icon: String)

@Composable
private fun ProfileMeasurementChip(item: ProfileMeasurementDisplayItem, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = SurfaceVariant, shape = RoundedCornerShape(8.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(item.icon, fontSize = 16.sp)
            Text(item.value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(item.label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
        }
    }
}

@Composable
private fun ProfileMeasurementHistoryItem(measurement: Measurement, onEdit: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(dateFormat.format(Date(measurement.date)), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    measurement.weight?.let { Text("⚖️ %.1f kg".format(it), style = MaterialTheme.typography.bodySmall) }
                    measurement.chest?.let { Text("🫁 %.1f cm".format(it), style = MaterialTheme.typography.bodySmall) }
                    measurement.waist?.let { Text("📏 %.1f cm".format(it), style = MaterialTheme.typography.bodySmall) }
                }
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Primary) }
                IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Error) }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Supprimer cette mesure ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = Error)) {
                    Text("Supprimer")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Annuler") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileMeasurementDialog(measurement: Measurement?, onDismiss: () -> Unit, onSave: (Measurement) -> Unit) {
    val isEditing = measurement != null

    var weight by remember { mutableStateOf(measurement?.weight?.toString() ?: "") }
    var bodyFat by remember { mutableStateOf(measurement?.bodyFat?.toString() ?: "") }
    var chest by remember { mutableStateOf(measurement?.chest?.toString() ?: "") }
    var waist by remember { mutableStateOf(measurement?.waist?.toString() ?: "") }
    var hips by remember { mutableStateOf(measurement?.hips?.toString() ?: "") }
    var shoulders by remember { mutableStateOf(measurement?.shoulders?.toString() ?: "") }
    var armLeft by remember { mutableStateOf(measurement?.armLeft?.toString() ?: "") }
    var armRight by remember { mutableStateOf(measurement?.armRight?.toString() ?: "") }
    var thighLeft by remember { mutableStateOf(measurement?.thighLeft?.toString() ?: "") }
    var thighRight by remember { mutableStateOf(measurement?.thighRight?.toString() ?: "") }
    var notes by remember { mutableStateOf(measurement?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Modifier les mensurations" else "Nouvelles mensurations") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("Remplissez uniquement les champs souhaités", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileMeasurementTextField(value = weight, onValueChange = { weight = it }, label = "Poids (kg)", modifier = Modifier.weight(1f))
                        ProfileMeasurementTextField(value = bodyFat, onValueChange = { bodyFat = it }, label = "Masse grasse (%)", modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileMeasurementTextField(value = chest, onValueChange = { chest = it }, label = "Poitrine (cm)", modifier = Modifier.weight(1f))
                        ProfileMeasurementTextField(value = shoulders, onValueChange = { shoulders = it }, label = "Épaules (cm)", modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileMeasurementTextField(value = waist, onValueChange = { waist = it }, label = "Taille (cm)", modifier = Modifier.weight(1f))
                        ProfileMeasurementTextField(value = hips, onValueChange = { hips = it }, label = "Hanches (cm)", modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileMeasurementTextField(value = armLeft, onValueChange = { armLeft = it }, label = "Bras G (cm)", modifier = Modifier.weight(1f))
                        ProfileMeasurementTextField(value = armRight, onValueChange = { armRight = it }, label = "Bras D (cm)", modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ProfileMeasurementTextField(value = thighLeft, onValueChange = { thighLeft = it }, label = "Cuisse G (cm)", modifier = Modifier.weight(1f))
                        ProfileMeasurementTextField(value = thighRight, onValueChange = { thighRight = it }, label = "Cuisse D (cm)", modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optionnel)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val newMeasurement = Measurement(
                    id = measurement?.id ?: 0,
                    date = measurement?.date ?: System.currentTimeMillis(),
                    weight = weight.toFloatOrNull(),
                    bodyFat = bodyFat.toFloatOrNull(),
                    chest = chest.toFloatOrNull(),
                    waist = waist.toFloatOrNull(),
                    hips = hips.toFloatOrNull(),
                    shoulders = shoulders.toFloatOrNull(),
                    armLeft = armLeft.toFloatOrNull(),
                    armRight = armRight.toFloatOrNull(),
                    thighLeft = thighLeft.toFloatOrNull(),
                    thighRight = thighRight.toFloatOrNull(),
                    notes = notes
                )
                onSave(newMeasurement)
            }) { Text(if (isEditing) "Enregistrer" else "Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun ProfileMeasurementTextField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

// ── Défis personnels ──────────────────────────────────────────────────────

private data class Challenge(val icon: String, val title: String, val description: String, val progress: Float, val completed: Boolean)

private fun buildChallenges(stats: UserStats): List<Challenge> {
    val challenges = mutableListOf<Challenge>()

    // Défi séances
    val nextWorkoutMilestone = listOf(5, 10, 25, 50, 100, 200).firstOrNull { it > stats.totalWorkouts } ?: (stats.totalWorkouts + 50)
    val prevWorkoutMilestone = listOf(0, 5, 10, 25, 50, 100).lastOrNull { it <= stats.totalWorkouts } ?: 0
    challenges.add(
        Challenge(
            icon = "💪",
            title = "Atteindre $nextWorkoutMilestone séances",
            description = "${stats.totalWorkouts}/$nextWorkoutMilestone séances terminées",
            progress = (stats.totalWorkouts - prevWorkoutMilestone).toFloat() / (nextWorkoutMilestone - prevWorkoutMilestone),
            completed = false
        )
    )

    // Défi streak
    val nextStreakMilestone = listOf(3, 5, 10, 20, 50).firstOrNull { it > stats.currentStreak } ?: (stats.currentStreak + 10)
    challenges.add(
        Challenge(
            icon = "🔥",
            title = "Série de $nextStreakMilestone séances",
            description = "${stats.currentStreak}/$nextStreakMilestone séances consécutives",
            progress = stats.currentStreak.toFloat() / nextStreakMilestone,
            completed = false
        )
    )

    // Défi exercices différents
    val nextExerciseMilestone = listOf(5, 10, 15, 20, 30).firstOrNull { it > stats.uniqueExercises } ?: (stats.uniqueExercises + 10)
    challenges.add(
        Challenge(
            icon = "🎯",
            title = "$nextExerciseMilestone exercices différents",
            description = "${stats.uniqueExercises}/$nextExerciseMilestone exercices pratiqués",
            progress = stats.uniqueExercises.toFloat() / nextExerciseMilestone,
            completed = false
        )
    )

    return challenges
}

@Composable
private fun PersonalChallengesCard(userStats: UserStats) {
    val challenges = remember(userStats) { buildChallenges(userStats) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "🎮 Défis en cours",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            challenges.forEach { challenge ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(challenge.icon, fontSize = 20.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(challenge.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("${(challenge.progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Primary)
                        }
                        Text(challenge.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(3.dp))
                        LinearProgressIndicator(
                            progress = challenge.progress.coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = Primary,
                            trackColor = SurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ── Niveau athlète ───────────────────────────────────────────────────────

private enum class AthleteLevel(val label: String, val icon: String, val minWorkouts: Int) {
    DEBUTANT("Débutant", "🌱", 0),
    AMATEUR("Amateur", "⭐", 10),
    INTERMEDIAIRE("Intermédiaire", "💪", 30),
    AVANCE("Avancé", "🔥", 75),
    EXPERT("Expert", "⚡", 150),
    ELITE("Élite", "👑", 300)
}

@Composable
private fun AthleteLevelCard(userStats: UserStats) {
    val levels = AthleteLevel.entries
    val currentLevel = levels.lastOrNull { userStats.totalWorkouts >= it.minWorkouts } ?: AthleteLevel.DEBUTANT
    val nextLevel = levels.getOrNull(currentLevel.ordinal + 1)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Niveau athlète",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(currentLevel.icon, fontSize = 20.sp)
                        Text(
                            currentLevel.label,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
                Text("${userStats.totalWorkouts} séances", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }

            if (nextLevel != null) {
                val progress = (userStats.totalWorkouts - currentLevel.minWorkouts).toFloat() /
                        (nextLevel.minWorkouts - currentLevel.minWorkouts)
                val remaining = nextLevel.minWorkouts - userStats.totalWorkouts

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("→ ${nextLevel.icon} ${nextLevel.label}", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    Text("encore $remaining séances", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Primary,
                    trackColor = SurfaceVariant
                )
            }

            // Streak avec protection
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥", fontSize = 20.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Série actuelle : ${userStats.currentStreak} séances",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (userStats.currentStreak >= 5) {
                        Text(
                            "Protection active — 1 pause autorisée",
                            style = MaterialTheme.typography.bodySmall,
                            color = Completed
                        )
                    } else {
                        Text(
                            "Record : ${userStats.maxStreak} séances",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                if (userStats.currentStreak >= 5) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Completed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── Récapitulatif mensuel ─────────────────────────────────────────────────

@Composable
private fun MonthlyRecapCard(stats: MonthlyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📅 Récap — ${stats.monthName.replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatItem(value = "${stats.sessionsCount}", label = "Séances", icon = "💪")
                ProfileStatItem(value = "${stats.totalMinutes}", label = "Minutes", icon = "⏱️")
                ProfileStatItem(value = "${(stats.totalVolumeKg / 1000).toInt()}t", label = "Volume", icon = "📦")
                ProfileStatItem(value = "${stats.uniqueExercises}", label = "Exercices", icon = "🎯")
            }
        }
    }
}

// ── Objectifs de la semaine (Stats tab) ──────────────────────────────────

@Composable
private fun WeeklyGoalsSection(stats: com.gymtracker.app.data.model.WeeklyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Objectifs de la semaine",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            WeeklyProgressRow(
                label = "Séances",
                current = stats.sessionsCount,
                goal = stats.sessionsGoal,
                unit = ""
            )

            Spacer(modifier = Modifier.height(10.dp))

            WeeklyProgressRow(
                label = "Temps d'entraînement",
                current = stats.totalMinutes,
                goal = stats.minutesGoal,
                unit = " min"
            )

            if (stats.totalVolumeKg > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                val volumeDisplay = if (stats.totalVolumeKg >= 1000f)
                    "${"%.1f".format(stats.totalVolumeKg / 1000f)} t"
                else
                    "${stats.totalVolumeKg.toInt()} kg"
                Text(
                    "Volume soulevé : $volumeDisplay",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressRow(label: String, current: Int, goal: Int, unit: String) {
    val progress = (current.toFloat() / goal).coerceIn(0f, 1f)
    Column {
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
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (progress >= 1f) Completed else Primary,
            trackColor = SurfaceVariant
        )
    }
}

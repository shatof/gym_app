package com.gymtracker.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gymtracker.app.data.model.ExerciseProgress
import com.gymtracker.app.data.model.ProgressDataPoint
import com.gymtracker.app.ui.theme.*
import com.gymtracker.app.ui.viewmodel.ProgressViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    modifier: Modifier = Modifier
) {
    val exerciseNames by viewModel.exerciseNames.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    val progressData by viewModel.progressData.collectAsState(initial = null)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Progression",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = OnBackground
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Sélecteur d'exercice
                item {
                    Text(
                        text = "Sélectionnez un exercice",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
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

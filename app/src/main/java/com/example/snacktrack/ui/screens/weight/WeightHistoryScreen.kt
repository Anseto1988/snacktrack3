package com.example.snacktrack.ui.screens.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.snacktrack.data.model.WeightEntry
import com.example.snacktrack.data.repository.WeightRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightHistoryScreen(
    dogId: String,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val weightRepository = remember { WeightRepository(context) }
    
    var weightEntries by remember { mutableStateOf<List<WeightEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(dogId) {
        scope.launch {
            weightRepository.getWeightHistory(dogId).collect { entries ->
                weightEntries = entries.sortedBy { it.timestamp }
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gewichtsverlauf") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Gewicht hinzufügen")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Einfaches Chart
                if (weightEntries.isNotEmpty()) {
                    SimpleWeightChart(
                        entries = weightEntries,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Gewichtsliste
                LazyColumn {
                    items(weightEntries.reversed()) { entry ->
                        WeightEntryCard(entry = entry)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    if (showAddDialog) {
        AddWeightDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { weight ->
                scope.launch {
                    val newEntry = WeightEntry(
                        dogId = dogId,
                        weight = weight,
                        timestamp = LocalDateTime.now()
                    )
                    weightRepository.addWeightEntry(newEntry)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun SimpleWeightChart(
    entries: List<WeightEntry>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (entries.size >= 2) {
                val maxWeight = entries.maxOf { it.weight }
                val minWeight = entries.minOf { it.weight }
                val weightRange = maxWeight - minWeight
                
                val points = entries.mapIndexed { index, entry ->
                    val x = (index.toFloat() / (entries.size - 1)) * size.width
                    val y = size.height - ((entry.weight - minWeight) / weightRange * size.height).toFloat()
                    Offset(x, y)
                }
                
                // Zeichne Linien zwischen den Punkten
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = Color.Blue,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = 3.dp.toPx()
                    )
                }
                
                // Zeichne Punkte
                points.forEach { point ->
                    drawCircle(
                        color = Color.Blue,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightEntryCard(entry: WeightEntry) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${entry.weight} kg",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = entry.timestamp.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            entry.note?.let { note ->
                Text(
                    text = note,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var weight by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gewicht hinzufügen") },
        text = {
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Gewicht (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    weight.toDoubleOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
} 
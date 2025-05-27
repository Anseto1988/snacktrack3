package com.example.snacktrack.ui.screens.dog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.ui.viewmodel.DogViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogDetailScreen(
    dogId: String,
    navController: NavController,
    dogViewModel: DogViewModel
) {
    val dogs by dogViewModel.dogs.collectAsState()
    val dog = dogs.find { it.id == dogId }
    
    if (dog == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dog.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate("add_edit_dog/${dog.id}")
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hundebild
            if (!dog.imageId.isNullOrEmpty()) {
                AsyncImage(
                    model = "https://your-appwrite-url/storage/buckets/dog_images/files/${dog.imageId}/view",
                    contentDescription = "Foto von ${dog.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            // Grundinformationen
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Grundinformationen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    DetailRow("Name", dog.name)
                    DetailRow("Rasse", dog.breed)
                    DetailRow("Geschlecht", dog.sex.displayName)
                    dog.birthDate?.let {
                        DetailRow("Geburtsdatum", it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }
                }
            }
            
            // Gewicht und Aktivität
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Gewicht & Aktivität",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    DetailRow("Aktuelles Gewicht", "${dog.weight} kg")
                    dog.targetWeight?.let {
                        DetailRow("Zielgewicht", "$it kg")
                    }
                    DetailRow("Aktivitätslevel", dog.activityLevel.displayName)
                    DetailRow("Täglicher Kalorienbedarf", "${dog.calculateDailyCalorieNeed()} kcal")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium
        )
        Text(text = value)
    }
} 
package com.example.snacktrack.ui.screens.dog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.snacktrack.data.model.ActivityLevel
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.model.Sex
import com.example.snacktrack.data.repository.DogRepository
import com.example.snacktrack.data.service.AppwriteService
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDogScreen(
    dogId: String?,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dogRepository = remember { DogRepository(context) }
    
    var dog by remember { mutableStateOf<Dog?>(null) }
    var isLoading by remember { mutableStateOf(dogId != null) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Form-Felder
    var name by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var birthDateString by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var selectedSexIndex by remember { mutableStateOf(0) }
    var selectedActivityLevelIndex by remember { mutableStateOf(2) } // Normal
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageId by remember { mutableStateOf<String?>(null) }
    
    // Dropdowns
    var sexExpanded by remember { mutableStateOf(false) }
    var activityLevelExpanded by remember { mutableStateOf(false) }
    
    val sexOptions = listOf("Männlich", "Weiblich", "Unbekannt")
    val activityLevelOptions = ActivityLevel.values().map { it.displayName }
    
    // Laden eines existierenden Hundes
    LaunchedEffect(dogId) {
        if (dogId != null) {
            try {
                dogRepository.getDogs().collect { dogs ->
                    val existingDog = dogs.find { it.id == dogId }
                    if (existingDog != null) {
                        dog = existingDog
                        name = existingDog.name
                        breed = existingDog.breed
                        birthDateString = existingDog.birthDate?.format(DateTimeFormatter.ISO_DATE) ?: ""
                        weight = existingDog.weight.toString()
                        targetWeight = existingDog.targetWeight?.toString() ?: ""
                        selectedSexIndex = when (existingDog.sex) {
                            Sex.MALE -> 0
                            Sex.FEMALE -> 1
                            Sex.UNKNOWN -> 2
                        }
                        selectedActivityLevelIndex = ActivityLevel.values().indexOf(existingDog.activityLevel)
                        imageId = existingDog.imageId
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Fehler beim Laden des Hundes: ${e.message}"
                isLoading = false
            }
        }
    }
    
    // Bild-Auswahl-Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageUri = it }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (dogId == null) "Hund hinzufügen" else "Hund bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Profilbild
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Hundeprofilbild",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (imageId != null) {
                            AsyncImage(
                                model = "https://parse.nordburglarp.de/v1/storage/buckets/${AppwriteService.BUCKET_DOG_IMAGES}/files/${imageId}/view?project=6829fc47b73f5bc2be1f",
                                contentDescription = "Hundeprofilbild",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Bild hinzufügen",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Rasse
                OutlinedTextField(
                    value = breed,
                    onValueChange = { breed = it },
                    label = { Text("Rasse") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Geburtsdatum
                OutlinedTextField(
                    value = birthDateString,
                    onValueChange = { birthDateString = it },
                    label = { Text("Geburtsdatum (JJJJ-MM-TT)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Geschlecht
                ExposedDropdownMenuBox(
                    expanded = sexExpanded,
                    onExpandedChange = { sexExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = sexOptions[selectedSexIndex],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded) },
                        label = { Text("Geschlecht") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = sexExpanded,
                        onDismissRequest = { sexExpanded = false }
                    ) {
                        sexOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedSexIndex = index
                                    sexExpanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Gewicht
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Aktuelles Gewicht (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    OutlinedTextField(
                        value = targetWeight,
                        onValueChange = { targetWeight = it },
                        label = { Text("Zielgewicht (kg)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Aktivitätslevel
                ExposedDropdownMenuBox(
                    expanded = activityLevelExpanded,
                    onExpandedChange = { activityLevelExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = activityLevelOptions[selectedActivityLevelIndex],
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityLevelExpanded) },
                        label = { Text("Aktivitätslevel") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = activityLevelExpanded,
                        onDismissRequest = { activityLevelExpanded = false }
                    ) {
                        activityLevelOptions.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedActivityLevelIndex = index
                                    activityLevelExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Fehleranzeige
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Speichern-Button
                Button(
                    onClick = {
                        // Validierung
                        if (name.isBlank()) {
                            errorMessage = "Bitte gib einen Namen ein"
                            return@Button
                        }
                        
                        val weightValue = weight.toDoubleOrNull()
                        if (weightValue == null || weightValue <= 0) {
                            errorMessage = "Bitte gib ein gültiges Gewicht ein"
                            return@Button
                        }
                        
                        val targetWeightValue = targetWeight.toDoubleOrNull()
                        if (targetWeight.isNotBlank() && (targetWeightValue == null || targetWeightValue <= 0)) {
                            errorMessage = "Bitte gib ein gültiges Zielgewicht ein"
                            return@Button
                        }
                        
                        val birthDate = try {
                            if (birthDateString.isNotBlank()) {
                                LocalDate.parse(birthDateString)
                            } else {
                                null
                            }
                        } catch (e: Exception) {
                            errorMessage = "Bitte gib ein gültiges Geburtsdatum im Format JJJJ-MM-TT ein"
                            return@Button
                        }
                        
                        isSaving = true
                        errorMessage = null
                        
                        scope.launch {
                            try {
                                // Bild hochladen, falls neu ausgewählt
                                var newImageId = imageId
                                
                                if (imageUri != null) {
                                    val imageFile = File(context.cacheDir, "dog_image.jpg")
                                    context.contentResolver.openInputStream(imageUri!!)?.use { input ->
                                        imageFile.outputStream().use { output ->
                                            input.copyTo(output)
                                        }
                                    }
                                    
                                    dogRepository.uploadDogImage(imageFile)
                                        .onSuccess { id ->
                                            newImageId = id
                                        }
                                        .onFailure { e ->
                                            errorMessage = "Fehler beim Hochladen des Bildes: ${e.message}"
                                            isSaving = false
                                            return@launch
                                        }
                                }
                                
                                // Hund speichern
                                val sex = when (selectedSexIndex) {
                                    0 -> Sex.MALE
                                    1 -> Sex.FEMALE
                                    else -> Sex.UNKNOWN
                                }
                                
                                val activityLevel = ActivityLevel.values()[selectedActivityLevelIndex]
                                
                                val updatedDog = Dog(
                                    id = dogId ?: "",
                                    name = name,
                                    breed = breed,
                                    birthDate = birthDate,
                                    sex = sex,
                                    weight = weightValue,
                                    targetWeight = targetWeightValue,
                                    activityLevel = activityLevel,
                                    imageId = newImageId
                                )
                                
                                dogRepository.saveDog(updatedDog)
                                    .onSuccess {
                                        isSaving = false
                                        onSaveSuccess()
                                    }
                                    .onFailure { e ->
                                        isSaving = false
                                        errorMessage = "Fehler beim Speichern: ${e.message}"
                                    }
                            } catch (e: Exception) {
                                isSaving = false
                                errorMessage = "Fehler: ${e.message}"
                            }
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Speichern")
                    }
                }
            }
        }
    }
} 
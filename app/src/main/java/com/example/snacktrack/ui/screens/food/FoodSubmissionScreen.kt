package com.example.snacktrack.ui.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.snacktrack.data.model.FoodSubmission
import com.example.snacktrack.data.model.SubmissionStatus
import com.example.snacktrack.data.repository.FoodRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSubmissionScreen(
    dogId: String, // Wird aktuell nicht direkt für die Submission verwendet, aber für die Navigation nützlich
    ean: String,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val foodRepository = remember { FoodRepository(context) }

    var brand by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var crudeFiber by remember { mutableStateOf("") }
    var rawAsh by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    // Weitere Felder wie imageUrl, additives können hier hinzugefügt werden

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neues Futter vorschlagen") },
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
            Text("EAN: $ean", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text("Marke") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = product,
                onValueChange = { product = it },
                label = { Text("Produktname") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Nährwertangaben (pro 100g)
            Text("Nährwerte pro 100g:", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = protein,
                onValueChange = { protein = it },
                label = { Text("Protein (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = fat,
                onValueChange = { fat = it },
                label = { Text("Fett (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = crudeFiber,
                onValueChange = { crudeFiber = it },
                label = { Text("Rohfaser (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = rawAsh,
                onValueChange = { rawAsh = it },
                label = { Text("Rohasche (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            )
            OutlinedTextField(
                value = moisture,
                onValueChange = { moisture = it },
                label = { Text("Feuchtigkeit (g)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
            // TODO: Felder für Zusatzstoffe und Bild-URL hinzufügen

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Validierung
                    if (brand.isBlank() || product.isBlank() || protein.isBlank() || fat.isBlank() || crudeFiber.isBlank() || rawAsh.isBlank() || moisture.isBlank()) {
                        errorMessage = "Bitte alle Pflichtfelder ausfüllen."
                        return@Button
                    }
                    val proteinValue = protein.toDoubleOrNull()
                    val fatValue = fat.toDoubleOrNull()
                    val crudeFiberValue = crudeFiber.toDoubleOrNull()
                    val rawAshValue = rawAsh.toDoubleOrNull()
                    val moistureValue = moisture.toDoubleOrNull()

                    if (proteinValue == null || fatValue == null || crudeFiberValue == null || rawAshValue == null || moistureValue == null) {
                        errorMessage = "Ungültige Nährwertangaben."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    val foodSubmission = FoodSubmission(
                        ean = ean,
                        brand = brand,
                        product = product,
                        protein = proteinValue,
                        fat = fatValue,
                        crudeFiber = crudeFiberValue,
                        rawAsh = rawAshValue,
                        moisture = moistureValue,
                        // additives = ..., // Hier Map erstellen
                        // imageUrl = ...,
                        status = SubmissionStatus.PENDING,
                        submittedAt = LocalDateTime.now()
                        // userId wird im Repository geholt
                    )

                    scope.launch {
                        foodRepository.submitFoodEntry(foodSubmission)
                            .onSuccess {
                                isLoading = false
                                showSuccessDialog = true
                            }
                            .onFailure { e ->
                                isLoading = false
                                errorMessage = "Fehler beim Senden: ${e.message}"
                            }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Vorschlag senden")
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onSaveSuccess() // Zurück navigieren
            },
            title = { Text("Vorschlag gesendet") },
            text = { Text("Vielen Dank! Dein Futtervorschlag wurde erfolgreich übermittelt und wird bald geprüft.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onSaveSuccess() // Zurück navigieren
                }) {
                    Text("OK")
                }
            }
        )
    }
}
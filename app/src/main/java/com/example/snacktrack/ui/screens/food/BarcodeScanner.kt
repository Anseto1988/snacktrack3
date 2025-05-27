package com.example.snacktrack.ui.screens.food

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.snacktrack.data.repository.FoodRepository
import com.example.snacktrack.data.model.FoodSubmission
import com.example.snacktrack.util.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScanner(
    dogId: String,
    onFoodFound: (String) -> Unit,
    onFoodNotFound: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val foodRepository = remember { FoodRepository(context) }
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var isFlashEnabled by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var foundEan by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Barcode scannen") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasCameraPermission) {
                FloatingActionButton(
                    onClick = { isFlashEnabled = !isFlashEnabled }
                ) {
                    Icon(
                        imageVector = if (isFlashEnabled) Icons.Default.FlashOff else Icons.Default.FlashOn,
                        contentDescription = if (isFlashEnabled) "Blitz ausschalten" else "Blitz einschalten"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showPermissionDialog) {
                AlertDialog(
                    onDismissRequest = { showPermissionDialog = false },
                    title = { Text("Kamerazugriff benötigt") },
                    text = { Text("Diese App benötigt Zugriff auf die Kamera, um Barcodes zu scannen.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPermissionDialog = false
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        ) {
                            Text("Erneut versuchen")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showPermissionDialog = false
                                onBackClick()
                            }
                        ) {
                            Text("Abbrechen")
                        }
                    }
                )
            }
            
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (hasCameraPermission && isScanning) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        val previewView = PreviewView(context).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        }
                        
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            
                            val imageAnalysis = BarcodeScanner.createAnalyzer(context) { barcodes ->
                                for (barcode in barcodes) {
                                    BarcodeScanner.extractEAN(barcode)?.let { ean ->
                                        if (isScanning) {
                                            isScanning = false
                                            foundEan = ean
                                            
                                            scope.launch {
                                                isLoading = true
                                                
                                                foodRepository.getFoodByEAN(ean)
                                                    .onSuccess { food ->
                                                        isLoading = false
                                                        if (food != null) {
                                                            onFoodFound(food.id)
                                                        } else {
                                                            onFoodNotFound(ean)
                                                        }
                                                    }
                                                    .onFailure { e ->
                                                        isLoading = false
                                                        errorMessage = "Fehler beim Suchen des Futters: ${e.message}"
                                                    }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                            } catch (e: Exception) {
                                Log.e("BarcodeScanner", "Binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(context))
                        
                        previewView
                    }
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Halte die Kamera auf einen Barcode",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Die App wird automatisch scannen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
} 
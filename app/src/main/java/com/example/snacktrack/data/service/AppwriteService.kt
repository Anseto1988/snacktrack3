package com.example.snacktrack.data.service

import android.content.Context
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import io.appwrite.services.Realtime

/**
 * Service-Klasse für die Kommunikation mit Appwrite
 */
class AppwriteService private constructor(context: Context) {
    
    companion object {
        private const val ENDPOINT = "https://parse.nordburglarp.de/v1"
        private const val PROJECT_ID = "snackrack2"
        const val DATABASE_ID = "snacktrack-db"
        
        // Collection IDs
        const val COLLECTION_DOGS = "dogs"
        const val COLLECTION_WEIGHT_ENTRIES = "weightEntries"
        const val COLLECTION_FOOD_INTAKE = "foodIntake"
        const val COLLECTION_FOOD_DB = "foodDB"
        const val COLLECTION_FOOD_SUBMISSIONS = "foodSubmissions"
        
        // Bucket IDs
        const val BUCKET_DOG_IMAGES = "dog_images"
        
        @Volatile
        private var instance: AppwriteService? = null
        
        fun getInstance(context: Context): AppwriteService {
            return instance ?: synchronized(this) {
                instance ?: AppwriteService(context).also { instance = it }
            }
        }
    }
    
    // Appwrite Client
    val client = Client(context)
        .setEndpoint(ENDPOINT)
        .setProject(PROJECT_ID)
        .setSelfSigned(true) // Nur für Entwicklung, in Produktion entfernen
    
    // Appwrite Services
    val account = Account(client)
    val databases = Databases(client)
    val storage = Storage(client)
    val realtime = Realtime(client)
    
    /**
     * Prüft, ob ein Nutzer eingeloggt ist
     */
    suspend fun isLoggedIn(): Boolean {
        return try {
            account.get()
            true
        } catch (e: Exception) {
            false
        }
    }
} 
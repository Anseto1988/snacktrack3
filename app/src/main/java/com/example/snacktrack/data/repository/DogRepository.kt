package com.example.snacktrack.data.repository

import android.content.Context
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.FileList
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.example.snacktrack.data.model.Dog
import com.example.snacktrack.data.service.AppwriteService
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DogRepository(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Holt alle Hunde des aktuell eingeloggten Nutzers
     */
    fun getDogs(): Flow<List<Dog>> = flow {
        try {
            val user = appwriteService.account.get()
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOGS,
                queries = listOf(Query.equal("ownerId", user.id))
            )
            
            val dogs = response.documents.map { doc ->
                Dog(
                    id = doc.id,
                    ownerId = doc.data["ownerId"].toString(),
                    name = doc.data["name"].toString(),
                    birthDate = (doc.data["birthDate"] as? String)?.let {
                        LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
                    },
                    breed = doc.data["breed"]?.toString() ?: "",
                    sex = doc.data["sex"]?.toString()?.let { enumValueOf<com.example.snacktrack.data.model.Sex>(it) }
                        ?: com.example.snacktrack.data.model.Sex.UNKNOWN,
                    weight = (doc.data["weight"] as? Number)?.toDouble() ?: 0.0,
                    targetWeight = (doc.data["targetWeight"] as? Number)?.toDouble(),
                    activityLevel = doc.data["activityLevel"]?.toString()?.let {
                        enumValueOf<com.example.snacktrack.data.model.ActivityLevel>(it)
                    } ?: com.example.snacktrack.data.model.ActivityLevel.NORMAL,
                    imageId = doc.data["imageId"]?.toString()
                )
            }
            emit(dogs)
        } catch (e: Exception) {
            emit(emptyList<Dog>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Erstellt oder aktualisiert einen Hund
     */
    suspend fun saveDog(dog: Dog): Result<Dog> = withContext(Dispatchers.IO) {
        try {
            val user = appwriteService.account.get()
            
            val data = mapOf(
                "ownerId" to user.id,
                "name" to dog.name,
                "birthDate" to dog.birthDate?.format(DateTimeFormatter.ISO_DATE),
                "breed" to dog.breed,
                "sex" to dog.sex.name,
                "weight" to dog.weight,
                "targetWeight" to dog.targetWeight,
                "activityLevel" to dog.activityLevel.name,
                "imageId" to dog.imageId,
                "dailyCalorieNeed" to dog.calculateDailyCalorieNeed()
            )
            
            val response = if (dog.id.isNotEmpty()) {
                // Update
                databases.updateDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_DOGS,
                    documentId = dog.id,
                    data = data
                )
            } else {
                // Create
                databases.createDocument(
                    databaseId = AppwriteService.DATABASE_ID,
                    collectionId = AppwriteService.COLLECTION_DOGS,
                    documentId = ID.unique(),
                    data = data
                )
            }
            
            Result.success(
                Dog(
                    id = response.id,
                    ownerId = response.data["ownerId"].toString(),
                    name = response.data["name"].toString(),
                    birthDate = (response.data["birthDate"] as? String)?.let {
                        LocalDate.parse(it, DateTimeFormatter.ISO_DATE)
                    },
                    breed = response.data["breed"]?.toString() ?: "",
                    sex = response.data["sex"]?.toString()?.let { enumValueOf<com.example.snacktrack.data.model.Sex>(it) }
                        ?: com.example.snacktrack.data.model.Sex.UNKNOWN,
                    weight = (response.data["weight"] as? Number)?.toDouble() ?: 0.0,
                    targetWeight = (response.data["targetWeight"] as? Number)?.toDouble(),
                    activityLevel = response.data["activityLevel"]?.toString()?.let {
                        enumValueOf<com.example.snacktrack.data.model.ActivityLevel>(it)
                    } ?: com.example.snacktrack.data.model.ActivityLevel.NORMAL,
                    imageId = response.data["imageId"]?.toString()
                )
            )
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Lädt ein Bild für einen Hund hoch
     */
    suspend fun uploadDogImage(file: File): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = appwriteService.storage.createFile(
                bucketId = AppwriteService.BUCKET_DOG_IMAGES,
                fileId = ID.unique(),
                file = InputFile.fromFile(file)
            )
            Result.success(response.id)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Löscht einen Hund
     */
    suspend fun deleteDog(dogId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_DOGS,
                documentId = dogId
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
} 
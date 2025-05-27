package com.example.snacktrack.data.repository

import android.content.Context
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import com.example.snacktrack.data.model.FoodIntake
import com.example.snacktrack.data.service.AppwriteService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FoodIntakeRepository(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Holt alle Futtereinträge für einen Hund an einem bestimmten Tag
     */
    fun getFoodIntakesForDog(dogId: String, date: LocalDate): Flow<List<FoodIntake>> = flow {
        try {
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1)
            
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.greaterThanEqual("timestamp", startOfDay.format(DateTimeFormatter.ISO_DATE_TIME)),
                    Query.lessThanEqual("timestamp", endOfDay.format(DateTimeFormatter.ISO_DATE_TIME)),
                    Query.orderDesc("timestamp")
                )
            )
            
            val intakes = response.documents.map { doc ->
                FoodIntake(
                    id = doc.id,
                    dogId = doc.data["dogId"].toString(),
                    foodId = doc.data["foodId"]?.toString(),
                    foodName = doc.data["foodName"].toString(),
                    amountGram = (doc.data["amountGram"] as? Number)?.toDouble() ?: 0.0,
                    calories = (doc.data["calories"] as? Number)?.toInt() ?: 0,
                    timestamp = (doc.data["timestamp"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now(),
                    note = doc.data["note"]?.toString(),
                    protein = (doc.data["protein"] as? Number)?.toDouble(),
                    fat = (doc.data["fat"] as? Number)?.toDouble(),
                    carbs = (doc.data["carbs"] as? Number)?.toDouble()
                )
            }
            emit(intakes)
        } catch (e: Exception) {
            emit(emptyList<FoodIntake>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Fügt einen neuen Futtereintrag hinzu
     */
    suspend fun addFoodIntake(intake: FoodIntake): Result<FoodIntake> = withContext(Dispatchers.IO) {
        try {
            val data = mapOf(
                "dogId" to intake.dogId,
                "foodId" to intake.foodId,
                "foodName" to intake.foodName,
                "amountGram" to intake.amountGram,
                "calories" to intake.calories,
                "timestamp" to intake.timestamp.format(DateTimeFormatter.ISO_DATE_TIME),
                "note" to intake.note,
                "protein" to intake.protein,
                "fat" to intake.fat,
                "carbs" to intake.carbs
            )
            
            val response = databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                documentId = ID.unique(),
                data = data
            )
            
            Result.success(
                FoodIntake(
                    id = response.id,
                    dogId = response.data["dogId"].toString(),
                    foodId = response.data["foodId"]?.toString(),
                    foodName = response.data["foodName"].toString(),
                    amountGram = (response.data["amountGram"] as? Number)?.toDouble() ?: 0.0,
                    calories = (response.data["calories"] as? Number)?.toInt() ?: 0,
                    timestamp = (response.data["timestamp"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now(),
                    note = response.data["note"]?.toString(),
                    protein = (response.data["protein"] as? Number)?.toDouble(),
                    fat = (response.data["fat"] as? Number)?.toDouble(),
                    carbs = (response.data["carbs"] as? Number)?.toDouble()
                )
            )
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Löscht einen Futtereintrag
     */
    suspend fun deleteFoodIntake(intakeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.deleteDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                documentId = intakeId
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Berechnet die Gesamtkalorien für einen Hund an einem bestimmten Tag
     */
    suspend fun getTotalCaloriesForDay(dogId: String, date: LocalDate): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val startOfDay = date.atStartOfDay()
            val endOfDay = date.plusDays(1).atStartOfDay().minusSeconds(1)
            
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_INTAKE,
                queries = listOf(
                    Query.equal("dogId", dogId),
                    Query.greaterThanEqual("timestamp", startOfDay.format(DateTimeFormatter.ISO_DATE_TIME)),
                    Query.lessThanEqual("timestamp", endOfDay.format(DateTimeFormatter.ISO_DATE_TIME))
                )
            )
            
            val totalCalories = response.documents.sumOf { doc ->
                (doc.data["calories"] as? Number)?.toInt() ?: 0
            }
            
            Result.success(totalCalories)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
} 
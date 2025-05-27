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
import com.example.snacktrack.data.model.Food
import com.example.snacktrack.data.model.FoodSubmission
import com.example.snacktrack.data.model.SubmissionStatus
import com.example.snacktrack.data.service.AppwriteService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FoodRepository(private val context: Context) {
    
    private val appwriteService = AppwriteService.getInstance(context)
    private val databases = appwriteService.databases
    
    /**
     * Sucht nach Lebensmitteln basierend auf einem Suchbegriff
     */
    fun searchFoods(query: String): Flow<List<Food>> = flow {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                queries = listOf(
                    Query.search("product", query),
                    Query.limit(50)
                )
            )
            
            val foods = response.documents.map { doc ->
                Food(
                    id = doc.id,
                    ean = doc.data["ean"]?.toString() ?: "",
                    brand = doc.data["brand"]?.toString() ?: "",
                    product = doc.data["product"]?.toString() ?: "",
                    protein = (doc.data["protein"] as? Number)?.toDouble() ?: 0.0,
                    fat = (doc.data["fat"] as? Number)?.toDouble() ?: 0.0,
                    crudeFiber = (doc.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                    rawAsh = (doc.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                    moisture = (doc.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                    additives = (doc.data["additives"] as? Map<*, *>)?.mapValues { it.value.toString() }
                        ?.mapKeys { it.key.toString() } ?: emptyMap(),
                    imageUrl = doc.data["imageUrl"]?.toString()
                )
            }
            emit(foods)
        } catch (e: Exception) {
            emit(emptyList<Food>())
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Holt ein Lebensmittel anhand der ID
     */
    suspend fun getFoodById(foodId: String): Result<Food> = withContext(Dispatchers.IO) {
        try {
            val response = databases.getDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                documentId = foodId
            )
            
            val food = Food(
                id = response.id,
                ean = response.data["ean"]?.toString() ?: "",
                brand = response.data["brand"]?.toString() ?: "",
                product = response.data["product"]?.toString() ?: "",
                protein = (response.data["protein"] as? Number)?.toDouble() ?: 0.0,
                fat = (response.data["fat"] as? Number)?.toDouble() ?: 0.0,
                crudeFiber = (response.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                rawAsh = (response.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                moisture = (response.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                additives = (response.data["additives"] as? Map<*, *>)?.mapValues { it.value.toString() }
                    ?.mapKeys { it.key.toString() } ?: emptyMap(),
                imageUrl = response.data["imageUrl"]?.toString()
            )
            
            Result.success(food)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Sucht nach einem Lebensmittel anhand des EAN/Barcodes
     */
    suspend fun getFoodByEAN(ean: String): Result<Food?> = withContext(Dispatchers.IO) {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB,
                queries = listOf(Query.equal("ean", ean))
            )
            
            if (response.documents.isNotEmpty()) {
                val doc = response.documents.first()
                val food = Food(
                    id = doc.id,
                    ean = doc.data["ean"]?.toString() ?: "",
                    brand = doc.data["brand"]?.toString() ?: "",
                    product = doc.data["product"]?.toString() ?: "",
                    protein = (doc.data["protein"] as? Number)?.toDouble() ?: 0.0,
                    fat = (doc.data["fat"] as? Number)?.toDouble() ?: 0.0,
                    crudeFiber = (doc.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                    rawAsh = (doc.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                    moisture = (doc.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                    additives = (doc.data["additives"] as? Map<*, *>)?.mapValues { it.value.toString() }
                        ?.mapKeys { it.key.toString() } ?: emptyMap(),
                    imageUrl = doc.data["imageUrl"]?.toString()
                )
                Result.success(food)
            } else {
                Result.success(null)
            }
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Reicht ein neues Lebensmittel zur Aufnahme in die Datenbank ein
     */
    suspend fun submitFoodSubmission(food: Food): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                documentId = ID.unique(),
                data = mapOf(
                    "ean" to food.ean,
                    "brand" to food.brand,
                    "product" to food.product,
                    "protein" to food.protein,
                    "fat" to food.fat,
                    "crudeFiber" to food.crudeFiber,
                    "rawAsh" to food.rawAsh,
                    "moisture" to food.moisture,
                    "additives" to food.additives,
                    "imageUrl" to food.imageUrl
                )
            )
            Result.success(Unit)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Erstellt einen neuen Vorschlag für die Futterdatenbank
     */
    suspend fun submitFoodEntry(submission: FoodSubmission): Result<FoodSubmission> = withContext(Dispatchers.IO) {
        try {
            val user = appwriteService.account.get()
            
            val data = mapOf(
                "userId" to user.id,
                "ean" to submission.ean,
                "brand" to submission.brand,
                "product" to submission.product,
                "protein" to submission.protein,
                "fat" to submission.fat,
                "crudeFiber" to submission.crudeFiber,
                "rawAsh" to submission.rawAsh,
                "moisture" to submission.moisture,
                "additives" to submission.additives,
                "imageUrl" to submission.imageUrl,
                "status" to submission.status.name,
                "submittedAt" to submission.submittedAt.format(DateTimeFormatter.ISO_DATE_TIME)
            )
            
            val response = databases.createDocument(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_SUBMISSIONS,
                documentId = ID.unique(),
                data = data
            )
            
            Result.success(
                FoodSubmission(
                    id = response.id,
                    userId = response.data["userId"].toString(),
                    ean = response.data["ean"].toString(),
                    brand = response.data["brand"].toString(),
                    product = response.data["product"].toString(),
                    protein = (response.data["protein"] as? Number)?.toDouble() ?: 0.0,
                    fat = (response.data["fat"] as? Number)?.toDouble() ?: 0.0,
                    crudeFiber = (response.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                    rawAsh = (response.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                    moisture = (response.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                    additives = (response.data["additives"] as? Map<*, *>)?.mapValues { it.value.toString() }
                        ?.mapKeys { it.key.toString() } ?: emptyMap(),
                    imageUrl = response.data["imageUrl"]?.toString(),
                    status = response.data["status"]?.toString()?.let { 
                        enumValueOf<SubmissionStatus>(it) 
                    } ?: SubmissionStatus.PENDING,
                    submittedAt = (response.data["submittedAt"] as? String)?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    } ?: LocalDateTime.now()
                )
            )
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
    
    /**
     * Sucht Futtermittel anhand eines Suchbegriffs
     */
    suspend fun searchFood(query: String): Result<List<Food>> = withContext(Dispatchers.IO) {
        try {
            // In einer realen App würde hier eine fortgeschrittene Suche implementiert werden
            // Für dieses MVP ist die Suche vereinfacht
            val response = databases.listDocuments(
                databaseId = AppwriteService.DATABASE_ID,
                collectionId = AppwriteService.COLLECTION_FOOD_DB
            )
            
            val foods = response.documents
                .filter { doc ->
                    val brand = doc.data["brand"]?.toString() ?: ""
                    val product = doc.data["product"]?.toString() ?: ""
                    brand.contains(query, ignoreCase = true) || 
                    product.contains(query, ignoreCase = true)
                }
                .map { doc ->
                    Food(
                        id = doc.id,
                        ean = doc.data["ean"]?.toString() ?: "",
                        brand = doc.data["brand"]?.toString() ?: "",
                        product = doc.data["product"]?.toString() ?: "",
                        protein = (doc.data["protein"] as? Number)?.toDouble() ?: 0.0,
                        fat = (doc.data["fat"] as? Number)?.toDouble() ?: 0.0,
                        crudeFiber = (doc.data["crudeFiber"] as? Number)?.toDouble() ?: 0.0,
                        rawAsh = (doc.data["rawAsh"] as? Number)?.toDouble() ?: 0.0,
                        moisture = (doc.data["moisture"] as? Number)?.toDouble() ?: 0.0,
                        additives = (doc.data["additives"] as? Map<*, *>)?.mapValues { it.value.toString() }
                            ?.mapKeys { it.key.toString() } ?: emptyMap(),
                        imageUrl = doc.data["imageUrl"]?.toString()
                    )
                }
            
            Result.success(foods)
        } catch (e: AppwriteException) {
            Result.failure(e)
        }
    }
} 
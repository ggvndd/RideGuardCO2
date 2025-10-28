package com.capstoneco2.rideguard.network

import com.capstoneco2.rideguard.data.models.AccidentReportRequest
import com.capstoneco2.rideguard.data.models.AccidentReportResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Repository class for handling network operations
 */
class NetworkRepository {
    
    companion object {
        // Test endpoint URL - replace with your actual API endpoint
        private const val BASE_URL = "https://jsonplaceholder.typicode.com/"
        
        @Volatile
        private var INSTANCE: NetworkRepository? = null
        
        fun getInstance(): NetworkRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkRepository().also { INSTANCE = it }
            }
        }
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    private val apiService = retrofit.create(RideGuardApiService::class.java)
    
    /**
     * Submit an accident report to the server
     */
    suspend fun submitAccidentReport(accidentReport: AccidentReportRequest): Result<AccidentReportResponse> {
        return try {
            val response = apiService.submitAccidentReport(accidentReport)
            handleApiResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Test API connectivity with sample data
     */
    suspend fun testApiConnection(phoneNumber: String = "+1-555-987-6543"): Result<Map<String, Any>> {
        return try {
            val testData = mapOf(
                "phone_number" to phoneNumber.ifBlank { "+1-555-987-6543" }
            )
            
            val response = apiService.testEndpoint(testData)
            handleApiResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a sample accident report for testing
     */
    fun createSampleAccidentReport(phoneNumber: String = "+1-555-123-4567"): AccidentReportRequest {
        return AccidentReportRequest(
            phoneNumber = phoneNumber.ifBlank { "+1-555-123-4567" }
        )
    }
    
    /**
     * Helper function to handle API responses consistently
     */
    private fun <T> handleApiResponse(response: Response<T>): Result<T> {
        return if (response.isSuccessful) {
            response.body()?.let { responseBody ->
                Result.success(responseBody)
            } ?: Result.failure(Exception("Empty response body"))
        } else {
            Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
        }
    }
}
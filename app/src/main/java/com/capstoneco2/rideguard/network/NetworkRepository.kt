package com.capstoneco2.rideguard.network

import com.capstoneco2.rideguard.data.models.AccidentReportRequest
import com.capstoneco2.rideguard.data.models.AccidentReportResponse
import com.capstoneco2.rideguard.data.models.LocationData
import com.capstoneco2.rideguard.data.models.VehicleInfo
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
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
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    Result.success(responseBody)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Test API connectivity with sample data
     */
    suspend fun testApiConnection(): Result<Map<String, Any>> {
        return try {
            val testData = mapOf(
                "message" to "Test connection from RideGuard app",
                "timestamp" to LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "app_version" to "1.0.0",
                "device_type" to "Android"
            )
            
            val response = apiService.testEndpoint(testData)
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    Result.success(responseBody)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                Result.failure(Exception("HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create a sample accident report for testing
     */
    fun createSampleAccidentReport(): AccidentReportRequest {
        return AccidentReportRequest(
            accidentId = UUID.randomUUID().toString(),
            location = LocationData(
                latitude = 37.7749,
                longitude = -122.4194,
                address = "123 Main Street, San Francisco, CA 94102"
            ),
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            severity = "MODERATE",
            vehicleInfo = VehicleInfo(
                make = "Toyota",
                model = "Camry",
                year = 2022,
                licensePlate = "ABC123"
            ),
            emergencyContacted = false
        )
    }
}
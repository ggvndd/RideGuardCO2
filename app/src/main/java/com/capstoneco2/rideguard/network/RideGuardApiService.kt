package com.capstoneco2.rideguard.network

import com.capstoneco2.rideguard.data.models.AccidentReportRequest
import com.capstoneco2.rideguard.data.models.AccidentReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API service interface for RideGuard endpoints
 */
interface RideGuardApiService {
    
    /**
     * Submit an accident report to the server
     * 
     * @param accidentReport The accident report data to submit
     * @return Response containing the server's response
     */
    @POST("api/accidents/report")
    suspend fun submitAccidentReport(
        @Body accidentReport: AccidentReportRequest
    ): Response<AccidentReportResponse>
    
    /**
     * Test endpoint for API connectivity
     * 
     * @param testData Any JSON data for testing
     * @return Response containing success/failure status
     */
    @POST("api/test")
    suspend fun testEndpoint(
        @Body testData: Map<String, Any>
    ): Response<Map<String, Any>>
}
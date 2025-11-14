package com.capstoneco2.rideguard.config

/**
 * API Configuration for RideGuard backend endpoints
 * 
 * Update these URLs to match your actual backend server
 */
object ApiConfig {
    
    // Base URLs for different environments
    private const val DEV_BASE_URL = "https://backend-rideguard.vercel.app"  // Your Vercel backend
    private const val STAGING_BASE_URL = "https://backend-rideguard.vercel.app"  // Your staging server
    private const val PROD_BASE_URL = "https://backend-rideguard.vercel.app"  // Your production server
    
    // Current environment - change this based on your deployment
    private const val CURRENT_ENV = "DEVELOPMENT"  // "DEVELOPMENT", "STAGING", or "PRODUCTION"
    
    /**
     * Get base URL based on current environment
     */
    val BASE_URL: String = when (CURRENT_ENV) {
        "DEVELOPMENT" -> DEV_BASE_URL
        "STAGING" -> STAGING_BASE_URL
        "PRODUCTION" -> PROD_BASE_URL
        else -> DEV_BASE_URL
    }
    
    /**
     * SMS API Endpoints
     */
    object SmsEndpoints {
        const val RECEIVE_SMS = "/api/coba"                // POST - Receive SMS data (your main endpoint)
        const val EMERGENCY_SMS = "/api/coba"              // POST - Emergency SMS alerts (same endpoint, different data)
        const val SMS_STATUS = "/api/status"                 // GET - Check SMS service status
        const val SMS_PING = "/api/ping"                     // GET - Health check
        
        // Full URLs
        val RECEIVE_SMS_URL = "$BASE_URL$RECEIVE_SMS"
        val EMERGENCY_SMS_URL = "$BASE_URL$EMERGENCY_SMS"
        val SMS_STATUS_URL = "$BASE_URL$SMS_STATUS"
        val SMS_PING_URL = "$BASE_URL$SMS_PING"
    }
    
    /**
     * API Headers
     */
    object Headers {
        const val CONTENT_TYPE = "application/json"
        const val USER_AGENT = "RideGuard-Android/1.0"
        const val API_KEY_HEADER = "X-API-Key"  // If you use API keys
    }
    
    /**
     * HTTP Configuration
     */
    object HttpConfig {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
        const val RETRY_ATTEMPTS = 3
    }
    
    /**
     * API Key (if your backend requires authentication)
     * In production, store this securely or retrieve from secure storage
     */
    const val API_KEY = "your-api-key-here"  // Replace with actual API key
    
    /**
     * Check if we're in development mode
     */
    val isDevelopment: Boolean = CURRENT_ENV == "DEVELOPMENT"
    
    /**
     * Check if we're in production mode
     */
    val isProduction: Boolean = CURRENT_ENV == "PRODUCTION"
}
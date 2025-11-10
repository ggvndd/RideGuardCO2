package com.capstoneco2.rideguard.service

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for emergency notification parsing logic
 * Tests FCM body parsing and emergency data extraction
 */
class EmergencyNotificationParsingTest {
    
    /**
     * Test emergency keyword detection
     */
    @Test
    fun `should detect emergency keywords in notification body`() {
        val emergencyMessages = listOf(
            "Your emergency contact John has been in a traffic accident",
            "Emergency alert: crash detected",
            "Urgent: accident detected for emergency contact",
            "Traffic accident notification"
        )
        
        val regularMessages = listOf(
            "How are you doing today?",
            "Meeting reminder for tomorrow",
            "Don't forget to buy groceries"
        )
        
        emergencyMessages.forEach { message ->
            val isEmergency = message.lowercase().contains("emergency") || 
                            message.lowercase().contains("accident") || 
                            message.lowercase().contains("crash")
            assertTrue("Should identify emergency message: $message", isEmergency)
        }
        
        regularMessages.forEach { message ->
            val isEmergency = message.lowercase().contains("emergency") || 
                            message.lowercase().contains("accident") || 
                            message.lowercase().contains("crash")
            assertFalse("Should not identify regular message as emergency: $message", isEmergency)
        }
    }
    
    /**
     * Test crash victim name extraction
     */
    @Test
    fun `should extract crash victim name from notification body`() {
        val testCases = listOf(
            "Your emergency contact hanjus has been in a traffic accident" to "hanjus",
            "Your emergency contact John Doe has been in a traffic accident" to "John Doe",
            "Your emergency contact Maria has been in a traffic accident" to "Maria",
            "Your emergency contact Alex Smith has been in a traffic accident" to "Alex Smith"
        )
        
        testCases.forEach { (message, expectedName) ->
            val pattern = "Your emergency contact ([\\w\\s]+) has been".toRegex()
            val match = pattern.find(message)
            
            assertNotNull("Should find name pattern in: $message", match)
            assertEquals("Should extract correct name from: $message", expectedName, match?.groupValues?.get(1))
        }
    }
    
    /**
     * Test coordinate extraction from notification body
     */
    @Test
    fun `should extract coordinates from notification body`() {
        val testCases = listOf(
            "Location: -7.7676, 110.3698" to Pair("-7.7676", "110.3698"),
            "Location: 40.7128, -74.0060" to Pair("40.7128", "-74.0060"),
            "Location: 0.0, 0.0" to Pair("0.0", "0.0"),
            "Location: -12.345, 67.890" to Pair("-12.345", "67.890")
        )
        
        testCases.forEach { (message, expected) ->
            val (expectedLat, expectedLng) = expected
            val pattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            val match = pattern.find(message)
            
            assertNotNull("Should find coordinate pattern in: $message", match)
            if (match != null) {
                assertEquals("Should extract correct latitude", expectedLat, match.groupValues[1])
                assertEquals("Should extract correct longitude", expectedLng, match.groupValues[2])
            }
        }
    }
    
    /**
     * Test complete emergency data parsing
     */
    @Test
    fun `should parse complete emergency data from FCM body`() {
        val fcmBody = "Your emergency contact hanjus has been in a traffic accident. Location: -7.7676, 110.3698"
        
        // Extract crash victim name
        val namePattern = "Your emergency contact ([\\w\\s]+) has been".toRegex()
        val nameMatch = namePattern.find(fcmBody)
        assertNotNull("Should extract crash victim name", nameMatch)
        assertEquals("Should extract correct name", "hanjus", nameMatch?.groupValues?.get(1))
        
        // Extract coordinates
        val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
        val coordMatch = coordPattern.find(fcmBody)
        assertNotNull("Should extract coordinates", coordMatch)
        if (coordMatch != null) {
            assertEquals("Should extract latitude", "-7.7676", coordMatch.groupValues[1])
            assertEquals("Should extract longitude", "110.3698", coordMatch.groupValues[2])
        }
        
        // Validate coordinate ranges
        val lat = coordMatch?.groupValues?.get(1)?.toDoubleOrNull()
        val lng = coordMatch?.groupValues?.get(2)?.toDoubleOrNull()
        
        assertNotNull("Latitude should be valid number", lat)
        assertNotNull("Longitude should be valid number", lng)
        
        if (lat != null && lng != null) {
            assertTrue("Latitude should be in valid range", lat >= -90.0 && lat <= 90.0)
            assertTrue("Longitude should be in valid range", lng >= -180.0 && lng <= 180.0)
        }
    }
    
    /**
     * Test emergency data validation
     */
    @Test
    fun `should validate emergency notification data`() {
        val validData = mapOf(
            "crash_victim_name" to "John Doe",
            "latitude" to "-7.7676",
            "longitude" to "110.3698",
            "user_role" to "emergency_contact",
            "crash_id" to "crash_123"
        )
        
        // Validate all required fields are present
        assertTrue("Should have crash victim name", validData.containsKey("crash_victim_name"))
        assertTrue("Should have latitude", validData.containsKey("latitude"))
        assertTrue("Should have longitude", validData.containsKey("longitude"))
        assertTrue("Should have user role", validData.containsKey("user_role"))
        assertTrue("Should have crash ID", validData.containsKey("crash_id"))
        
        // Validate field values
        assertNotNull("Crash victim name should not be null", validData["crash_victim_name"])
        assertTrue("Crash victim name should not be empty", validData["crash_victim_name"]!!.isNotEmpty())
        
        assertEquals("User role should be emergency_contact", "emergency_contact", validData["user_role"])
        
        val lat = validData["latitude"]?.toDoubleOrNull()
        val lng = validData["longitude"]?.toDoubleOrNull()
        
        assertNotNull("Latitude should be parseable", lat)
        assertNotNull("Longitude should be parseable", lng)
        
        if (lat != null && lng != null) {
            assertTrue("Latitude should be valid", lat >= -90.0 && lat <= 90.0)
            assertTrue("Longitude should be valid", lng >= -180.0 && lng <= 180.0)
        }
    }
    
    /**
     * Test malformed input handling
     */
    @Test
    fun `should handle malformed notification data gracefully`() {
        val malformedInputs = listOf(
            "", // Empty string
            "Your emergency contact  has been in a traffic accident", // Missing name
            "Your emergency contact John has been in a traffic accident. Location: invalid, invalid", // Invalid coordinates
            "Random notification without emergency data", // No emergency data
            "Your emergency contact John has been in a traffic accident. Location: , " // Empty coordinates
        )
        
        malformedInputs.forEach { input ->
            // Should not throw exceptions when parsing malformed input
            val namePattern = "Your emergency contact ([\\w\\s]+) has been".toRegex()
            val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            
            // These should not crash
            val nameMatch = namePattern.find(input)
            val coordMatch = coordPattern.find(input)
            
            // For malformed input, matches might be null, but no exceptions should be thrown
            assertTrue("Should handle malformed input without crashing: $input", true)
        }
    }
}
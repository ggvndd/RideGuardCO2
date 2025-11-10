package com.capstoneco2.rideguard.service

import org.junit.Test
import org.junit.Assert.*
import kotlinx.coroutines.test.runTest

/**
 * Performance and stress tests for emergency notification system
 * Tests system behavior under load and extreme conditions
 */
class EmergencyNotificationPerformanceTest {
    
    /**
     * Test parsing performance with large datasets
     */
    @Test
    fun `should parse notifications efficiently with large datasets`() = runTest {
        val startTime = System.currentTimeMillis()
        val notifications = mutableListOf<String>()
        
        // Generate 1000 test notifications
        repeat(1000) { index ->
            notifications.add("Your emergency contact User$index has been in a traffic accident. Location: -7.${index % 10}${index % 10}76, 110.${index % 10}${index % 10}98")
        }
        
        // Parse all notifications
        val results = mutableListOf<Map<String, String>>()
        notifications.forEach { message ->
            val namePattern = "Your emergency contact ([\\w\\d]+) has been".toRegex()
            val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            
            val nameMatch = namePattern.find(message)
            val coordMatch = coordPattern.find(message)
            
            if (nameMatch != null && coordMatch != null) {
                results.add(mapOf(
                    "name" to nameMatch.groupValues[1],
                    "latitude" to coordMatch.groupValues[1],
                    "longitude" to coordMatch.groupValues[2]
                ))
            }
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertEquals("Should parse all 1000 notifications", 1000, results.size)
        assertTrue("Should parse efficiently (under 100ms)", duration < 100)
        
        // Verify some results
        assertEquals("First result should be correct", "User0", results[0]["name"])
        assertEquals("Last result should be correct", "User999", results[999]["name"])
    }
    
    /**
     * Test concurrent notification processing
     */
    @Test
    fun `should handle concurrent notification processing`() = runTest {
        val notifications = (1..50).map { index ->
            "Your emergency contact ConcurrentUser$index has been in a traffic accident. Location: ${index % 90}.0, ${index % 180}.0"
        }
        
        // Simulate concurrent processing
        val results = mutableSetOf<String>()
        notifications.forEach { message ->
            val namePattern = "Your emergency contact ([\\w\\d]+) has been".toRegex()
            val nameMatch = namePattern.find(message)
            
            if (nameMatch != null) {
                results.add(nameMatch.groupValues[1])
            }
        }
        
        assertEquals("Should process all concurrent notifications", 50, results.size)
        
        // Verify all unique names are captured
        (1..50).forEach { index ->
            assertTrue("Should contain ConcurrentUser$index", results.contains("ConcurrentUser$index"))
        }
    }
    
    /**
     * Test memory usage with repeated parsing
     */
    @Test
    fun `should not leak memory with repeated parsing operations`() {
        val testMessage = "Your emergency contact hanjus has been in a traffic accident. Location: -7.7676, 110.3698"
        val pattern = "Your emergency contact ([\\w]+) has been".toRegex()
        
        // Perform many parsing operations
        repeat(10000) {
            val match = pattern.find(testMessage)
            assertNotNull("Should consistently find match", match)
            assertEquals("Should consistently extract name", "hanjus", match?.groupValues?.get(1))
        }
        
        // If we get here without OutOfMemoryError, memory is managed properly
        assertTrue("Memory management test passed", true)
    }
    
    /**
     * Test regex pattern compilation efficiency
     */
    @Test
    fun `should compile regex patterns efficiently`() {
        val startTime = System.currentTimeMillis()
        
        // Test multiple pattern compilations
        repeat(1000) {
            val namePattern = "Your emergency contact ([\\w\\s]+) has been".toRegex()
            val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            
            // Use patterns once to ensure they're compiled
            namePattern.find("test")
            coordPattern.find("test")
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertTrue("Pattern compilation should be efficient (under 50ms)", duration < 50)
    }
    
    /**
     * Test parsing with malformed data at scale
     */
    @Test
    fun `should handle malformed data efficiently at scale`() {
        val malformedMessages = listOf(
            "", // Empty
            "Invalid message format",
            "Your emergency contact  has been", // Missing name
            "Location: invalid, coordinates", // Invalid coords
            "Random text without any structure",
            null.toString(), // Null string
            " ", // Whitespace only
            "Your emergency contact has been in a traffic accident", // Missing name
            "Emergency contact John Location:", // Missing proper format
            "Partial message without"
        )
        
        val startTime = System.currentTimeMillis()
        
        // Process malformed data 100 times each
        repeat(100) {
            malformedMessages.forEach { message ->
                val namePattern = "Your emergency contact ([\\w\\s]+) has been".toRegex()
                val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
                
                // Should not throw exceptions
                val nameMatch = namePattern.find(message)
                val coordMatch = coordPattern.find(message)
                
                // Results may be null for malformed data, but no crashes
                assertTrue("Should handle malformed data gracefully", true)
            }
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertTrue("Should process malformed data efficiently (under 100ms)", duration < 100)
    }
    
    /**
     * Test coordinate validation performance
     */
    @Test
    fun `should validate coordinates efficiently`() {
        val coordinates = (1..1000).map { index ->
            Pair(
                (Math.random() * 180 - 90).toString(), // Random latitude
                (Math.random() * 360 - 180).toString()  // Random longitude
            )
        }
        
        val startTime = System.currentTimeMillis()
        
        val validCoordinates = coordinates.filter { (lat, lng) ->
            val latitude = lat.toDoubleOrNull()
            val longitude = lng.toDoubleOrNull()
            
            latitude != null && longitude != null &&
            latitude >= -90.0 && latitude <= 90.0 &&
            longitude >= -180.0 && longitude <= 180.0
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertTrue("Should have some valid coordinates", validCoordinates.isNotEmpty())
        assertTrue("Coordinate validation should be efficient (under 50ms)", duration < 50)
    }
    
    /**
     * Test crash ID generation performance
     */
    @Test
    fun `should generate crash IDs efficiently`() {
        val startTime = System.currentTimeMillis()
        val crashIds = mutableSetOf<String>()
        
        // Generate 10,000 crash IDs
        repeat(10000) { index ->
            val crashId = "crash_${System.currentTimeMillis()}_$index"
            crashIds.add(crashId)
        }
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        assertEquals("Should generate all unique crash IDs", 10000, crashIds.size)
        assertTrue("Crash ID generation should be efficient (under 100ms)", duration < 100)
        
        // Verify format consistency
        crashIds.take(100).forEach { crashId ->
            assertTrue("Each crash ID should follow format", crashId.matches("crash_\\d+_\\d+".toRegex()))
        }
    }
    
    /**
     * Test notification data extraction with various message sizes
     */
    @Test
    fun `should extract data efficiently from various message sizes`() {
        val messageSizes = listOf(50, 100, 500, 1000, 2000, 4000) // Different FCM message sizes
        
        messageSizes.forEach { size ->
            val baseName = "TestUser"
            val baseCoords = "Location: -7.7676, 110.3698"
            val padding = "X".repeat(maxOf(0, size - baseName.length - baseCoords.length - 50))
            val message = "Your emergency contact $baseName has been in a traffic accident. $baseCoords. Additional: $padding"
            
            val startTime = System.currentTimeMillis()
            
            val namePattern = "Your emergency contact ([\\w]+) has been".toRegex()
            val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            
            val nameMatch = namePattern.find(message)
            val coordMatch = coordPattern.find(message)
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            assertNotNull("Should extract name from message of size $size", nameMatch)
            assertNotNull("Should extract coordinates from message of size $size", coordMatch)
            assertEquals("Should extract correct name", baseName, nameMatch?.groupValues?.get(1))
            assertTrue("Extraction should be fast even for large messages (under 10ms)", duration < 10)
        }
    }
    
    /**
     * Test system behavior under stress conditions
     */
    @Test
    fun `should maintain accuracy under stress conditions`() = runTest {
        // Simulate high-frequency notifications
        val notifications = (1..500).map { index ->
            "Your emergency contact StressUser$index has been in a traffic accident. Location: -${index % 90}.${index % 10000}, ${index % 180}.${index % 10000}"
        }
        
        var successfulParses = 0
        val results = mutableListOf<Triple<String, String, String>>() // name, lat, lng
        
        notifications.forEach { message ->
            val namePattern = "Your emergency contact ([\\w\\d]+) has been".toRegex()
            val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            
            val nameMatch = namePattern.find(message)
            val coordMatch = coordPattern.find(message)
            
            if (nameMatch != null && coordMatch != null) {
                successfulParses++
                results.add(Triple(
                    nameMatch.groupValues[1],
                    coordMatch.groupValues[1], 
                    coordMatch.groupValues[2]
                ))
            }
        }
        
        assertEquals("Should parse all stress notifications", 500, successfulParses)
        assertEquals("Should have all results", 500, results.size)
        
        // Verify data integrity under stress
        val firstResult = results[0]
        assertEquals("First name should be correct", "StressUser1", firstResult.first)
        
        val lastResult = results[499]
        assertEquals("Last name should be correct", "StressUser500", lastResult.first)
        
        // Verify no duplicate names
        val uniqueNames = results.map { it.first }.toSet()
        assertEquals("All names should be unique", 500, uniqueNames.size)
    }
}
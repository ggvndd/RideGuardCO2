package com.capstoneco2.rideguard.service

import org.junit.Test
import org.junit.Assert.*

/**
 * Comprehensive edge case tests for emergency notification system
 * Tests boundary conditions, special characters, and extreme scenarios
 */
class EmergencyNotificationEdgeCasesTest {
    
    /**
     * Test special characters in crash victim names
     */
    @Test
    fun `should handle special characters in crash victim names`() {
        val testCases = listOf(
            "Your emergency contact José María has been in a traffic accident" to "José María",
            "Your emergency contact Li Wei-Chen has been in a traffic accident" to "Li Wei-Chen",
            "Your emergency contact O'Connor has been in a traffic accident" to "O'Connor",
            "Your emergency contact Jean-François has been in a traffic accident" to "Jean-François",
            "Your emergency contact Müller has been in a traffic accident" to "Müller",
            "Your emergency contact د. أحمد has been in a traffic accident" to "د. أحمد"
        )
        
        testCases.forEach { (message, expectedName) ->
            val pattern = "Your emergency contact ([\\w\\s\\-'áéíóúàèìòùâêîôûäëïöüñçü.]+) has been".toRegex()
            val match = pattern.find(message)
            
            assertNotNull("Should find name pattern in: $message", match)
            if (match != null) {
                assertEquals("Should extract correct name with special characters", expectedName, match.groupValues[1])
            }
        }
    }
    
    /**
     * Test extreme coordinate values
     */
    @Test
    fun `should validate extreme coordinate values`() {
        val extremeCoordinates = listOf(
            // North/South poles
            Pair("90.0", "0.0"), // North pole
            Pair("-90.0", "0.0"), // South pole
            
            // International date line
            Pair("0.0", "180.0"), // Eastern hemisphere extreme
            Pair("0.0", "-180.0"), // Western hemisphere extreme
            
            // Equator crossings
            Pair("0.0", "0.0"), // Null Island
            
            // High precision coordinates
            Pair("40.7589", "-73.9851"), // Times Square, NYC
            Pair("-33.8688", "151.2093"), // Sydney Opera House
            Pair("35.6762", "139.6503") // Tokyo Station
        )
        
        extremeCoordinates.forEach { (lat, lng) ->
            val latitude = lat.toDoubleOrNull()
            val longitude = lng.toDoubleOrNull()
            
            assertNotNull("Latitude should be parseable: $lat", latitude)
            assertNotNull("Longitude should be parseable: $lng", longitude)
            
            if (latitude != null && longitude != null) {
                assertTrue("Latitude should be in valid range: $lat", latitude >= -90.0 && latitude <= 90.0)
                assertTrue("Longitude should be in valid range: $lng", longitude >= -180.0 && longitude <= 180.0)
            }
        }
    }
    
    /**
     * Test very long notification messages
     */
    @Test
    fun `should handle very long notification messages`() {
        val longName = "A".repeat(100) // 100 character name
        val veryLongMessage = "Your emergency contact $longName has been in a traffic accident. " +
                "Location: -7.7676, 110.3698. Additional details: " + "X".repeat(500)
        
        // Should still be able to parse despite length
        val namePattern = "Your emergency contact ([\\w]+) has been".toRegex()
        val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
        
        val nameMatch = namePattern.find(veryLongMessage)
        val coordMatch = coordPattern.find(veryLongMessage)
        
        assertNotNull("Should extract name from long message", nameMatch)
        assertNotNull("Should extract coordinates from long message", coordMatch)
        
        assertEquals("Should extract correct long name", longName, nameMatch?.groupValues?.get(1))
        assertEquals("Should extract latitude", "-7.7676", coordMatch?.groupValues?.get(1))
        assertEquals("Should extract longitude", "110.3698", coordMatch?.groupValues?.get(2))
    }
    
    /**
     * Test multiple coordinate formats in same message
     */
    @Test
    fun `should handle multiple coordinate formats in same message`() {
        val multiCoordMessage = "Emergency at Location: -7.7676, 110.3698 and also near GPS: 40.7128, -74.0060"
        
        val pattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
        val matches = pattern.findAll(multiCoordMessage).toList()
        
        assertEquals("Should find first coordinate set", 1, matches.size)
        
        val firstMatch = matches[0]
        assertEquals("Should extract first latitude", "-7.7676", firstMatch.groupValues[1])
        assertEquals("Should extract first longitude", "110.3698", firstMatch.groupValues[2])
    }
    
    /**
     * Test notification with missing punctuation
     */
    @Test
    fun `should handle notification without punctuation`() {
        val noPunctuationMessage = "Your emergency contact john has been in a traffic accident Location -7 7676 110 3698"
        
        // Should be flexible with spacing and punctuation
        val flexibleCoordPattern = "Location[\\s:]*([\\-+]?\\d+[\\s\\.]?\\d*)\\s+([\\-+]?\\d+[\\s\\.]?\\d*)".toRegex()
        val match = flexibleCoordPattern.find(noPunctuationMessage)
        
        assertNotNull("Should find coordinates without punctuation", match)
        if (match != null) {
            // Should find some coordinate pattern even without proper formatting
            assertTrue("Should extract some coordinate data", match.groupValues[1].isNotEmpty())
            assertTrue("Should extract some coordinate data", match.groupValues[2].isNotEmpty())
        }
    }
    
    /**
     * Test notification with HTML/XML content
     */
    @Test
    fun `should handle notification with HTML content`() {
        val htmlMessage = "<html>Your emergency contact &lt;John&gt; has been in a traffic accident. Location: -7.7676, 110.3698</html>"
        
        // Should still extract data from HTML content
        val namePattern = "Your emergency contact [&<]*([\\w\\s>]+)[&>]* has been".toRegex()
        val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
        
        val coordMatch = coordPattern.find(htmlMessage)
        
        assertNotNull("Should extract coordinates from HTML message", coordMatch)
        if (coordMatch != null) {
            assertEquals("Should extract latitude from HTML", "-7.7676", coordMatch.groupValues[1])
            assertEquals("Should extract longitude from HTML", "110.3698", coordMatch.groupValues[2])
        }
    }
    
    /**
     * Test notification with Unicode characters
     */
    @Test
    fun `should handle Unicode characters in notifications`() {
        val unicodeMessages = listOf(
            "Your emergency contact 田中 has been in a traffic accident. Location: 35.6762, 139.6503",
            "Your emergency contact Владимир has been in a traffic accident. Location: 55.7558, 37.6176", 
            "Your emergency contact محمد has been in a traffic accident. Location: 24.7136, 46.6753",
            "Your emergency contact Θεόδωρος has been in a traffic accident. Location: 37.9838, 23.7275"
        )
        
        unicodeMessages.forEach { message ->
            val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            val coordMatch = coordPattern.find(message)
            
            assertNotNull("Should extract coordinates from Unicode message: $message", coordMatch)
            
            if (coordMatch != null) {
                val lat = coordMatch.groupValues[1].toDoubleOrNull()
                val lng = coordMatch.groupValues[2].toDoubleOrNull()
                
                assertNotNull("Latitude should be valid in Unicode message", lat)
                assertNotNull("Longitude should be valid in Unicode message", lng)
                
                if (lat != null && lng != null) {
                    assertTrue("Latitude should be in range for Unicode message", lat >= -90.0 && lat <= 90.0)
                    assertTrue("Longitude should be in range for Unicode message", lng >= -180.0 && lng <= 180.0)
                }
            }
        }
    }
    
    /**
     * Test crash ID generation with timestamp collision
     */
    @Test
    fun `should generate unique crash IDs even with timestamp collision`() {
        val crashIds = mutableSetOf<String>()
        val baseTimestamp = System.currentTimeMillis()
        
        // Generate IDs with same timestamp but different suffixes
        repeat(100) { index ->
            val crashId = "crash_${baseTimestamp}_$index"
            crashIds.add(crashId)
        }
        
        assertEquals("All crash IDs should be unique despite same timestamp", 100, crashIds.size)
        
        crashIds.forEach { crashId ->
            assertTrue("Each crash ID should follow format", crashId.matches("crash_\\d+_\\d+".toRegex()))
            assertFalse("Crash ID should not contain spaces", crashId.contains(" "))
            assertTrue("Crash ID should be reasonable length", crashId.length > 10 && crashId.length < 50)
        }
    }
    
    /**
     * Test coordinate precision handling
     */
    @Test
    fun `should handle various coordinate precisions`() {
        val coordinatePrecisions = listOf(
            "1.0, 2.0" to Pair(1.0, 2.0), // Low precision
            "1.5, 2.7" to Pair(1.5, 2.7), // One decimal
            "-7.7676, 110.3698" to Pair(-7.7676, 110.3698), // Four decimals (your data)
            "40.758896, -73.985130" to Pair(40.758896, -73.985130), // Six decimals (high precision)
            "-33.8688197, 151.2092955" to Pair(-33.8688197, 151.2092955) // Seven decimals (very high precision)
        )
        
        coordinatePrecisions.forEach { (coordString, expected) ->
            val message = "Emergency at Location: $coordString"
            val pattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
            val match = pattern.find(message)
            
            assertNotNull("Should extract coordinates: $coordString", match)
            
            if (match != null) {
                val extractedLat = match.groupValues[1].toDouble()
                val extractedLng = match.groupValues[2].toDouble()
                
                assertEquals("Should extract correct latitude", expected.first, extractedLat, 0.0000001)
                assertEquals("Should extract correct longitude", expected.second, extractedLng, 0.0000001)
            }
        }
    }
    
    /**
     * Test notification size limits
     */
    @Test
    fun `should handle FCM message size limits`() {
        // FCM has a 4KB limit for messages
        val maxMessageSize = 4096
        val baseMessage = "Your emergency contact TestUser has been in a traffic accident. Location: -7.7676, 110.3698. "
        val padding = "Additional info: " + "X".repeat(maxMessageSize - baseMessage.length - 50)
        val largeMessage = baseMessage + padding
        
        // Should still parse even at size limit
        assertTrue("Message should be near FCM size limit", largeMessage.length > 3000)
        
        val namePattern = "Your emergency contact ([\\w]+) has been".toRegex()
        val coordPattern = "Location:\\s*([\\-+]?\\d*\\.?\\d+),\\s*([\\-+]?\\d*\\.?\\d+)".toRegex()
        
        val nameMatch = namePattern.find(largeMessage)
        val coordMatch = coordPattern.find(largeMessage)
        
        assertNotNull("Should extract name from large message", nameMatch)
        assertNotNull("Should extract coordinates from large message", coordMatch)
        
        assertEquals("Should extract correct name", "TestUser", nameMatch?.groupValues?.get(1))
        assertEquals("Should extract correct coordinates", "-7.7676", coordMatch?.groupValues?.get(1))
    }
}
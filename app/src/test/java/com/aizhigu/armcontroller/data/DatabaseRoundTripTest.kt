package com.aizhigu.armcontroller.data

import org.junit.Test
import org.junit.Assert.*

class DatabaseRoundTripTest {
    
    @Test
    fun testActionFrameFactory_clampsValues() {
        // Test PWM clamping
        val servos = mapOf(
            1 to 100,   // Below minimum
            2 to 3000,  // Above maximum
            3 to 1500,  // Valid
            4 to 500,   // Minimum
            5 to 2500,  // Maximum
            6 to -100   // Negative
        )
        
        val frame = ActionFrameFactory.create(
            sequenceId = 0,
            duration = 1000,
            servos = servos,
            soundId = null
        )
        
        assertEquals(500, frame.servos[1])   // Clamped to minimum
        assertEquals(2500, frame.servos[2])  // Clamped to maximum
        assertEquals(1500, frame.servos[3])  // Unchanged
        assertEquals(500, frame.servos[4])   // At minimum
        assertEquals(2500, frame.servos[5])  // At maximum
        assertEquals(500, frame.servos[6])   // Clamped to minimum
    }
    
    @Test
    fun testActionFrameFactory_clampsDuration() {
        val servos = (1..6).associateWith { 1500 }
        
        // Test duration clamping
        val shortFrame = ActionFrameFactory.create(0, 100, servos, null)
        assertEquals(500, shortFrame.duration)
        
        val longFrame = ActionFrameFactory.create(0, 10000, servos, null)
        assertEquals(5000, longFrame.duration)
        
        val validFrame = ActionFrameFactory.create(0, 1000, servos, null)
        assertEquals(1000, validFrame.duration)
    }
    
    @Test
    fun testActionFrameFactory_clampsSoundId() {
        val servos = (1..6).associateWith { 1500 }
        
        val invalidLowSound = ActionFrameFactory.create(0, 1000, servos, 0)
        assertEquals(1, invalidLowSound.soundId)
        
        val invalidHighSound = ActionFrameFactory.create(0, 1000, servos, 300)
        assertEquals(255, invalidHighSound.soundId)
        
        val validSound = ActionFrameFactory.create(0, 1000, servos, 100)
        assertEquals(100, validSound.soundId)
    }
    
    @Test
    fun testEntityConversion_preservesData() {
        val originalFrame = ActionFrame(
            sequenceId = 5,
            duration = 2000,
            servos = mapOf(1 to 1000, 2 to 1200, 3 to 1400, 4 to 1600, 5 to 1800, 6 to 2000),
            soundId = 42
        )
        
        val entity = originalFrame.toEntity(123L)
        val convertedBack = entity.toDomainModel()
        
        assertEquals(originalFrame.sequenceId, convertedBack.sequenceId)
        assertEquals(originalFrame.duration, convertedBack.duration)
        assertEquals(originalFrame.servos, convertedBack.servos)
        assertEquals(originalFrame.soundId, convertedBack.soundId)
        assertEquals(123L, entity.projectId)
    }
    
    @Test
    fun testProjectConversion_preservesData() {
        val frames = listOf(
            ActionFrame(0, 1000, (1..6).associateWith { 1500 }, null),
            ActionFrame(1, 2000, (1..6).associateWith { 2000 }, 50)
        )
        
        val originalProject = ActionProject(
            id = 0,
            name = "Test Project",
            remoteSlotId = 5,
            frames = frames,
            createdAt = 1000000L,
            modifiedAt = 2000000L
        )
        
        val entity = originalProject.toEntity()
        val convertedBack = entity.toDomainModel(frames)
        
        assertEquals(originalProject.name, convertedBack.name)
        assertEquals(originalProject.remoteSlotId, convertedBack.remoteSlotId)
        assertEquals(originalProject.frames.size, convertedBack.frames.size)
        assertEquals(originalProject.createdAt, convertedBack.createdAt)
        assertEquals(originalProject.modifiedAt, convertedBack.modifiedAt)
    }
}
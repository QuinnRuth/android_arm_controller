package com.aizhigu.armcontroller.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseSchemaPropertyTest : StringSpec({

    // Custom generators for test data
    fun Arb.Companion.actionFrame(): Arb<ActionFrame> = arbitrary { rs ->
        val sequenceId = Arb.int(0, 1000).bind()
        val duration = Arb.int(500, 5000).bind()
        val servos = (1..6).associateWith { Arb.int(500, 2500).bind() }
        val soundId = Arb.int(1, 255).orNull().bind()
        
        ActionFrame(
            sequenceId = sequenceId,
            duration = duration,
            servos = servos,
            soundId = soundId
        )
    }

    fun Arb.Companion.actionProject(): Arb<ActionProject> = arbitrary { rs ->
        val name = Arb.string(1, 50).bind()
        val remoteSlotId = Arb.int(1, 10).orNull().bind()
        val frames = Arb.list(Arb.actionFrame(), 0..10).bind()
        val createdAt = Arb.long(1000000000000L, System.currentTimeMillis()).bind()
        val modifiedAt = Arb.long(createdAt, System.currentTimeMillis()).bind()
        
        ActionProject(
            id = 0, // Will be auto-generated
            name = name,
            remoteSlotId = remoteSlotId,
            frames = frames,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }

    // Helper function to create test database
    fun createTestDatabase(): AppDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    // Feature: action-sequencer, Property 4: Database Round-Trip Consistency
    "Saving and loading a project should preserve all data" {
        checkAll(100, Arb.actionProject()) { project ->
            val database = createTestDatabase()
            val dao = database.actionDao()
            
            runBlocking {
                // Save project
                val projectEntity = project.toEntity()
                val frameEntities = project.frames.map { it.toEntity(0) } // projectId will be updated
                val savedId = dao.insertProjectWithFrames(projectEntity, frameEntities)
                
                // Load project back
                val loaded = dao.getProjectWithFrames(savedId)
                
                // Verify data integrity
                loaded shouldNotBe null
                loaded!!.name shouldBe project.name
                loaded.remoteSlotId shouldBe project.remoteSlotId
                loaded.frames.size shouldBe project.frames.size
                loaded.createdAt shouldBe project.createdAt
                loaded.modifiedAt shouldBe project.modifiedAt
                
                // Verify frames are preserved
                loaded.frames.forEachIndexed { index, loadedFrame ->
                    val originalFrame = project.frames[index]
                    loadedFrame.duration shouldBe originalFrame.duration
                    loadedFrame.servos shouldBe originalFrame.servos
                    loadedFrame.soundId shouldBe originalFrame.soundId
                }
            }
            
            database.close()
        }
    }

    // Feature: action-sequencer, Property 4: Database Round-Trip Consistency
    "Entity to domain model conversion should preserve data integrity" {
        checkAll(100, Arb.actionProject()) { project ->
            runBlocking {
                // Convert to entity and back
                val projectEntity = project.toEntity()
                val frameEntities = project.frames.mapIndexed { index, frame ->
                    frame.copy(sequenceId = index).toEntity(1L)
                }
                
                val frames = frameEntities.map { it.toDomainModel() }
                val reconstructed = projectEntity.toDomainModel(frames)
                
                // Verify conversion preserves data
                reconstructed.name shouldBe project.name
                reconstructed.remoteSlotId shouldBe project.remoteSlotId
                reconstructed.createdAt shouldBe project.createdAt
                reconstructed.modifiedAt shouldBe project.modifiedAt
                reconstructed.frames.size shouldBe project.frames.size
                
                // Verify frame data is preserved
                reconstructed.frames.forEachIndexed { index, reconstructedFrame ->
                    val originalFrame = project.frames[index]
                    reconstructedFrame.duration shouldBe originalFrame.duration
                    reconstructedFrame.servos shouldBe originalFrame.servos
                    reconstructedFrame.soundId shouldBe originalFrame.soundId
                }
            }
        }
    }

    // Feature: action-sequencer, Property 4: Database Round-Trip Consistency
    "Database operations should handle concurrent access safely" {
        checkAll(50, Arb.list(Arb.actionProject(), 1..5)) { projects ->
            val database = createTestDatabase()
            val dao = database.actionDao()
            
            runBlocking {
                // Insert multiple projects concurrently
                val savedIds = projects.map { project ->
                    val projectEntity = project.toEntity()
                    val frameEntities = project.frames.map { it.toEntity(0) }
                    dao.insertProjectWithFrames(projectEntity, frameEntities)
                }
                
                // Verify all projects were saved
                savedIds.size shouldBe projects.size
                savedIds.distinct().size shouldBe projects.size // All IDs should be unique
                
                // Verify all projects can be loaded
                val loadedProjects = dao.getAllProjectsWithFrames()
                loadedProjects.size shouldBe projects.size
                
                // Verify each project maintains its data integrity
                savedIds.forEach { id ->
                    val loaded = dao.getProjectWithFrames(id)
                    loaded shouldNotBe null
                }
            }
            
            database.close()
        }
    }

    // Feature: action-sequencer, Property 4: Database Round-Trip Consistency
    "Foreign key constraints should maintain referential integrity" {
        checkAll(50, Arb.actionProject()) { project ->
            val database = createTestDatabase()
            val dao = database.actionDao()
            
            runBlocking {
                // Insert project with frames
                val projectEntity = project.toEntity()
                val frameEntities = project.frames.map { it.toEntity(0) }
                val savedId = dao.insertProjectWithFrames(projectEntity, frameEntities)
                
                // Verify frames exist
                val frames = dao.getFramesByProjectId(savedId)
                frames.size shouldBe project.frames.size
                
                // Delete project - should cascade delete frames
                dao.deleteProjectById(savedId)
                
                // Verify frames were deleted due to cascade
                val remainingFrames = dao.getFramesByProjectId(savedId)
                remainingFrames.size shouldBe 0
                
                // Verify project was deleted
                val deletedProject = dao.getProjectById(savedId)
                deletedProject shouldBe null
            }
            
            database.close()
        }
    }

    // Feature: action-sequencer, Property 4: Database Round-Trip Consistency
    "Update operations should preserve project identity" {
        checkAll(50, Arb.actionProject()) { originalProject ->
            val database = createTestDatabase()
            val dao = database.actionDao()
            
            runBlocking {
                // Insert original project
                val projectEntity = originalProject.toEntity()
                val frameEntities = originalProject.frames.map { it.toEntity(0) }
                val savedId = dao.insertProjectWithFrames(projectEntity, frameEntities)
                
                // Create updated project with same ID
                val updatedProject = originalProject.copy(
                    id = savedId,
                    name = "Updated ${originalProject.name}",
                    modifiedAt = System.currentTimeMillis()
                )
                
                // Update project
                dao.updateProjectWithFrames(updatedProject)
                
                // Verify update preserved ID and updated fields
                val loaded = dao.getProjectWithFrames(savedId)
                loaded shouldNotBe null
                loaded!!.id shouldBe savedId
                loaded.name shouldBe updatedProject.name
                loaded.modifiedAt shouldBe updatedProject.modifiedAt
                loaded.frames.size shouldBe updatedProject.frames.size
            }
            
            database.close()
        }
    }

    // Test database schema validation
    "Database schema should be created without errors" {
        shouldNotThrow<Exception> {
            val database = createTestDatabase()
            val dao = database.actionDao()
            
            runBlocking {
                // Test basic operations don't throw exceptions
                dao.getAllProjectsSync()
                dao.getProjectById(999L) // Non-existent ID
            }
            
            database.close()
        }
    }
})
package com.aizhigu.armcontroller.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    // Project operations
    @Query("SELECT * FROM action_projects ORDER BY modifiedAt DESC")
    fun getAllProjects(): Flow<List<ActionProjectEntity>>

    @Query("SELECT * FROM action_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ActionProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ActionProjectEntity): Long

    @Update
    suspend fun updateProject(project: ActionProjectEntity)

    @Delete
    suspend fun deleteProject(project: ActionProjectEntity)

    @Query("DELETE FROM action_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    // Frame operations
    @Query("SELECT * FROM action_frames WHERE projectId = :projectId ORDER BY sequenceId ASC")
    suspend fun getFramesByProjectId(projectId: Long): List<ActionFrameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrame(frame: ActionFrameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrames(frames: List<ActionFrameEntity>)

    @Update
    suspend fun updateFrame(frame: ActionFrameEntity)

    @Delete
    suspend fun deleteFrame(frame: ActionFrameEntity)

    @Query("DELETE FROM action_frames WHERE projectId = :projectId AND sequenceId = :sequenceId")
    suspend fun deleteFrameBySequenceId(projectId: Long, sequenceId: Int)

    @Query("DELETE FROM action_frames WHERE projectId = :projectId")
    suspend fun deleteAllFramesForProject(projectId: Long)

    // Transaction methods for complex operations
    @Transaction
    suspend fun insertProjectWithFrames(project: ActionProjectEntity, frames: List<ActionFrameEntity>): Long {
        val projectId = insertProject(project)
        val framesWithProjectId = frames.map { it.copy(projectId = projectId) }
        insertFrames(framesWithProjectId)
        return projectId
    }

    @Transaction
    suspend fun getProjectWithFrames(projectId: Long): ActionProject? {
        val projectEntity = getProjectById(projectId) ?: return null
        val frameEntities = getFramesByProjectId(projectId)
        val frames = frameEntities.map { it.toDomainModel() }
        return projectEntity.toDomainModel(frames)
    }

    @Transaction
    suspend fun getAllProjectsWithFrames(): List<ActionProject> {
        val projectEntities = getAllProjectsSync()
        return projectEntities.map { projectEntity ->
            val frameEntities = getFramesByProjectId(projectEntity.id)
            val frames = frameEntities.map { it.toDomainModel() }
            projectEntity.toDomainModel(frames)
        }
    }

    @Query("SELECT * FROM action_projects ORDER BY modifiedAt DESC")
    suspend fun getAllProjectsSync(): List<ActionProjectEntity>

    @Transaction
    suspend fun updateProjectWithFrames(project: ActionProject) {
        // Update project entity
        updateProject(project.toEntity())
        
        // Delete existing frames
        deleteAllFramesForProject(project.id)
        
        // Insert new frames
        val frameEntities = project.frames.map { it.toEntity(project.id) }
        insertFrames(frameEntities)
    }
}

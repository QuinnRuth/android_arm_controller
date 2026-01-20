package com.aizhigu.armcontroller.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActionDao {
    @Query("SELECT * FROM action_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ActionProject>>

    @Query("SELECT * FROM action_projects WHERE id = :id")
    suspend fun getProjectById(id: String): ActionProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ActionProject)

    @Delete
    suspend fun deleteProject(project: ActionProject)
}

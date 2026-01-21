package com.aizhigu.armcontroller.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ActionProjectEntity::class, ActionFrameEntity::class], 
    version = 2, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun actionDao(): ActionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new action_projects table with updated schema
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS action_projects_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        remoteSlotId INTEGER,
                        createdAt INTEGER NOT NULL,
                        modifiedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create new action_frames table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS action_frames (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectId INTEGER NOT NULL,
                        sequenceId INTEGER NOT NULL,
                        duration INTEGER NOT NULL,
                        servo1 INTEGER NOT NULL,
                        servo2 INTEGER NOT NULL,
                        servo3 INTEGER NOT NULL,
                        servo4 INTEGER NOT NULL,
                        servo5 INTEGER NOT NULL,
                        servo6 INTEGER NOT NULL,
                        soundId INTEGER,
                        FOREIGN KEY(projectId) REFERENCES action_projects_new(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Migrate existing data if any exists
                database.execSQL("""
                    INSERT INTO action_projects_new (name, remoteSlotId, createdAt, modifiedAt)
                    SELECT name, NULL, createdAt, createdAt FROM action_projects
                """.trimIndent())

                // Drop old table and rename new one
                database.execSQL("DROP TABLE IF EXISTS action_projects")
                database.execSQL("ALTER TABLE action_projects_new RENAME TO action_projects")

                // Create indices for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_action_frames_projectId ON action_frames(projectId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_action_frames_sequenceId ON action_frames(sequenceId)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "arm_controller_db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // For development only
                .build()
                INSTANCE = instance
                instance
            }
        }

        // For testing purposes
        fun getInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            ).build()
        }
    }
}

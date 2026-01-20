package com.aizhigu.armcontroller.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

// The core data model for a single step
data class ActionFrame(
    val id: String = UUID.randomUUID().toString(),
    val duration: Int = 1000,
    val servos: List<Int> = listOf(1500, 1500, 1500, 1500, 1500, 1500), // 6-Axis
    val soundId: Int? = null
)

// The project file
@Entity(tableName = "action_projects")
data class ActionProject(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val frames: List<ActionFrame>,
    val createdAt: Long = System.currentTimeMillis()
)

// Converters for Room
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromActionFrameList(value: List<ActionFrame>): String {
        val type = object : TypeToken<List<ActionFrame>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toActionFrameList(value: String): List<ActionFrame> {
        val type = object : TypeToken<List<ActionFrame>>() {}.type
        return gson.fromJson(value, type)
    }
}

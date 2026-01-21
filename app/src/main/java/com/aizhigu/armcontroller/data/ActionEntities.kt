package com.aizhigu.armcontroller.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Domain Models
data class ActionFrame(
    val sequenceId: Int,
    val duration: Int,  // milliseconds, clamped to 500-5000
    val servos: Map<Int, Int>,  // servo index (1-6) to PWM (500-2500)
    val soundId: Int? = null  // MP3 track ID (1-255)
) {
    init {
        require(duration in 500..5000) { "Duration must be 500-5000ms" }
        require(servos.size == 6) { "Must have exactly 6 servo positions" }
        servos.forEach { (index, pwm) ->
            require(index in 1..6) { "Servo index must be 1-6" }
            require(pwm in 500..2500) { "PWM must be 500-2500" }
        }
        soundId?.let { require(it in 1..255) { "Sound ID must be 1-255" } }
    }
}

data class ActionProject(
    val id: Long = 0,
    val name: String,
    val remoteSlotId: Int? = null,  // 1-10 for Arduino storage
    val frames: List<ActionFrame>,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis()
) {
    init {
        require(name.isNotBlank()) { "Project name cannot be blank" }
        require(remoteSlotId == null || remoteSlotId in 1..10) { "Remote slot must be 1-10" }
    }
    
    val truncatedName: String
        get() = name.take(50)
    
    val totalDuration: Int
        get() = frames.sumOf { it.duration }
}

// Database Entities
@Entity(tableName = "action_projects")
data class ActionProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val remoteSlotId: Int?,
    val createdAt: Long,
    val modifiedAt: Long
)

@Entity(
    tableName = "action_frames",
    foreignKeys = [ForeignKey(
        entity = ActionProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ActionFrameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val sequenceId: Int,
    val duration: Int,
    val servo1: Int,
    val servo2: Int,
    val servo3: Int,
    val servo4: Int,
    val servo5: Int,
    val servo6: Int,
    val soundId: Int?
)

// Factory for creating ActionFrames with clamping
object ActionFrameFactory {
    fun create(
        sequenceId: Int,
        duration: Int,
        servos: Map<Int, Int>,
        soundId: Int? = null
    ): ActionFrame {
        val clampedDuration = duration.coerceIn(500, 5000)
        val clampedServos = servos.mapValues { (_, pwm) -> pwm.coerceIn(500, 2500) }
        val clampedSoundId = soundId?.coerceIn(1, 255)
        
        return ActionFrame(sequenceId, clampedDuration, clampedServos, clampedSoundId)
    }
}

// Mappers between entities and domain models
fun ActionProjectEntity.toDomainModel(frames: List<ActionFrame>): ActionProject {
    return ActionProject(
        id = id,
        name = name,
        remoteSlotId = remoteSlotId,
        frames = frames,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )
}

fun ActionProject.toEntity(): ActionProjectEntity {
    return ActionProjectEntity(
        id = if (id == 0L) 0 else id,
        name = name,
        remoteSlotId = remoteSlotId,
        createdAt = createdAt,
        modifiedAt = modifiedAt
    )
}

fun ActionFrameEntity.toDomainModel(): ActionFrame {
    val servos = mapOf(
        1 to servo1,
        2 to servo2,
        3 to servo3,
        4 to servo4,
        5 to servo5,
        6 to servo6
    )
    return ActionFrame(
        sequenceId = sequenceId,
        duration = duration,
        servos = servos,
        soundId = soundId
    )
}

fun ActionFrame.toEntity(projectId: Long): ActionFrameEntity {
    return ActionFrameEntity(
        projectId = projectId,
        sequenceId = sequenceId,
        duration = duration,
        servo1 = servos[1] ?: 1500,
        servo2 = servos[2] ?: 1500,
        servo3 = servos[3] ?: 1500,
        servo4 = servos[4] ?: 1500,
        servo5 = servos[5] ?: 1500,
        servo6 = servos[6] ?: 1500,
        soundId = soundId
    )
}

// Converters for Room (keeping for backward compatibility if needed)
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

package com.aizhigu.armcontroller.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aizhigu.armcontroller.data.ActionDao
import com.aizhigu.armcontroller.data.ActionFrame
import com.aizhigu.armcontroller.data.ActionProject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.aizhigu.armcontroller.data.ToxParser
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class ActionSequencerViewModel(
    private val actionDao: ActionDao,
    private val onSendCommand: (String) -> Unit // Callback to send data to Bluetooth
) : ViewModel() {

    // Current working project state
    private val _currentProject = MutableStateFlow<ActionProject?>(null)
    val currentProject = _currentProject.asStateFlow()

    // Working list of frames (UI edits this directly before saving)
    private val _frames = MutableStateFlow<List<ActionFrame>>(emptyList())
    val frames = _frames.asStateFlow()

    // Runner state
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private val _currentPlayingIndex = MutableStateFlow<Int>(-1)
    val currentPlayingIndex = _currentPlayingIndex.asStateFlow()

    private var runnerJob: Job? = null

    // ========== CRUD Operations ==========

    fun loadProject(project: ActionProject) {
        _currentProject.value = project
        _frames.value = project.frames
    }

    fun createNewProject(name: String) {
        val newProject = ActionProject(
            name = name,
            frames = emptyList()
        )
        _currentProject.value = newProject
        _frames.value = emptyList()
    }

    fun addFrame(frame: ActionFrame) {
        _frames.update { it + frame }
    }

    fun updateFrame(index: Int, frame: ActionFrame) {
        _frames.update { list ->
            if (index in list.indices) {
                val mutable = list.toMutableList()
                mutable[index] = frame
                mutable
            } else list
        }
    }

    fun deleteFrame(index: Int) {
        _frames.update { list ->
            if (index in list.indices) {
                val mutable = list.toMutableList()
                mutable.removeAt(index)
                mutable
            } else list
        }
    }

    fun insertFrame(index: Int, frame: ActionFrame) {
        _frames.update { list ->
            val mutable = list.toMutableList()
            if (index in 0..mutable.size) {
                mutable.add(index, frame)
            } else {
                mutable.add(frame)
            }
            mutable
        }
    }
    
    fun moveFrame(fromIndex: Int, toIndex: Int) {
        _frames.update { list ->
            val mutable = list.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = mutable.removeAt(fromIndex)
                mutable.add(toIndex, item)
            }
            mutable
        }
    }

    fun saveProject() {
        viewModelScope.launch {
            _currentProject.value?.let { proj ->
                val updated = proj.copy(frames = _frames.value)
                actionDao.insertProject(updated.toEntity())
                _currentProject.value = updated
            }
        }
    }

    fun importTox(content: String) {
        try {
            val newFrames = ToxParser.parse(content)
            if (newFrames.isNotEmpty()) {
                _frames.update { it + newFrames }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun captureFrame(currentServos: List<Int>) {
        // Create frame from current servo values
        val newFrame = ActionFrame(
            servos = currentServos,
            duration = 1000
        )
        addFrame(newFrame)
    }

    // ========== Runner Logic ==========

    fun togglePlay(loop: Boolean = false) {
        if (_isPlaying.value) {
            stop()
        } else {
            play(loop)
        }
    }

    fun startLoop() {
        if (_isPlaying.value) stop()
        play(loop = true)
    }

    fun playSingleStep() {
        if (_frames.value.isEmpty()) return
        
        // Determine next index (circular)
        val nextIndex = if (_currentPlayingIndex.value == -1) 0 
                        else (_currentPlayingIndex.value + 1) % _frames.value.size
        
        _isPlaying.value = true // Show as playing
        runnerJob?.cancel()
        runnerJob = viewModelScope.launch {
            try {
                _currentPlayingIndex.value = nextIndex
                val frame = _frames.value[nextIndex]
                executeFrame(frame)
                delay(frame.duration.toLong())
            } finally {
                _isPlaying.value = false
                // Do NOT reset index, so next step continues from here
            }
        }
    }

    private fun play(loop: Boolean) {
        if (_frames.value.isEmpty()) return
        
        _isPlaying.value = true
        runnerJob?.cancel()
        runnerJob = viewModelScope.launch {
            try {
                // If stepping, start from current index, else from 0
                var startIndex = if (_currentPlayingIndex.value != -1) _currentPlayingIndex.value else 0
                // If at end, start over
                if (startIndex >= _frames.value.size - 1) startIndex = 0

                do {
                    // Iterate from startIndex to end
                    for (index in startIndex until _frames.value.size) {
                        if (!isActive) break 
                        val frame = _frames.value[index]
                        _currentPlayingIndex.value = index
                        
                        executeFrame(frame)
                        delay(frame.duration.toLong())
                    }
                    startIndex = 0 // Next loop starts from 0
                } while (loop && _isPlaying.value && isActive)
            } finally {
                stop()
            }
        }
    }

    fun stop() {
        _isPlaying.value = false
        _currentPlayingIndex.value = -1
        runnerJob?.cancel()
    }

    private fun executeFrame(frame: ActionFrame) {
        // Send command for each servo
        // Format: #1P1500T1000!
        // We can optimize by bundling, but individual commands are safer for this simple protocol
        val time = frame.duration
        frame.servos.forEachIndexed { index, pwm ->
            // Axis ID 1-6
            val cmd = "#${index + 1}P${pwm}T${time}!"
            onSendCommand(cmd)
        }
    }

    // ========== Conversion Utils ==========
    
    companion object {
        fun pwmToDegree(pwm: Int): Int {
            // 500 -> 0, 2500 -> 180
            return ((pwm - 500) * 180 / 2000).coerceIn(0, 180)
        }

        fun degreeToPwm(degree: Int): Int {
            // 0 -> 500, 180 -> 2500
            return (500 + (degree * 2000 / 180)).coerceIn(500, 2500)
        }
    }
}

class ActionSequencerViewModelFactory(
    private val actionDao: ActionDao,
    private val onSendCommand: (String) -> Unit
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActionSequencerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActionSequencerViewModel(actionDao, onSendCommand) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

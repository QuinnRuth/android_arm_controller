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
                actionDao.insertProject(updated)
                _currentProject.value = updated
            }
        }
    }

    // ========== Runner Logic ==========

    fun togglePlay(loop: Boolean = false) {
        if (_isPlaying.value) {
            stop()
        } else {
            play(loop)
        }
    }

    private fun play(loop: Boolean) {
        if (_frames.value.isEmpty()) return
        
        _isPlaying.value = true
        runnerJob?.cancel()
        runnerJob = viewModelScope.launch {
            try {
                do {
                    _frames.value.forEachIndexed { index, frame ->
                        _currentPlayingIndex.value = index
                        
                        // Execute Frame
                        executeFrame(frame)
                        
                        // Wait for duration
                        delay(frame.duration.toLong())
                    }
                } while (loop && _isPlaying.value)
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

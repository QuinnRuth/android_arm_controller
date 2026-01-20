package com.aizhigu.armcontroller.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aizhigu.armcontroller.data.ActionFrame
import com.aizhigu.armcontroller.ui.theme.Pink80
import com.aizhigu.armcontroller.ui.theme.Purple40

// Custom Cyberpunk Colors
val CyberBlack = Color(0xFF0D0D0D)
val CyberBlue = Color(0xFF0F3460) // Panel & Card
val CyberMagenta = Color(0xFFE94560) // Accents / Active
val CyberText = Color(0xFFFFFFFF)
val CyberDim = Color(0xFF1A1A2E)

@Composable
fun SequencerScreen(
    viewModel: ActionSequencerViewModel,
    modifier: Modifier = Modifier
) {
    val frames by viewModel.frames.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playingIndex by viewModel.currentPlayingIndex.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingFrameIndex by remember { mutableStateOf(-1) }
    var editingFrame by remember { mutableStateOf<ActionFrame?>(null) }

    // Cyberpunk Background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // == Header / Toolbar ==
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberDim)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTION SEQUENCER",
                    color = CyberMagenta,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )

                // Project Controls
                IconButton(onClick = { /* TODO: Save */ viewModel.saveProject() }) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = CyberText)
                }
            }

            // == Timeline List ==
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(frames) { index, frame ->
                    ActionFrameCard(
                        index = index,
                        frame = frame,
                        isPlaying = index == playingIndex,
                        onClick = {
                            editingFrameIndex = index
                            editingFrame = frame
                            showEditDialog = true
                        },
                        onDelete = { viewModel.deleteFrame(index) }
                    )
                }
                
                // Add Button at the end of list
                item {
                    Button(
                        onClick = { 
                            // Add default frame (Home position)
                            viewModel.addFrame(ActionFrame()) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, CyberBlue, RoundedCornerShape(4.dp)),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyberMagenta)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD KEYFRAME", color = CyberMagenta, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // == Bottom Command Bar ==
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberDim)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Stop
                Button(
                    onClick = { viewModel.togglePlay() },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isPlaying) CyberMagenta else CyberBlue),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPlaying) "STOP" else "RUN", fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Loop Toggle
                Button(
                    onClick = { viewModel.togglePlay(loop = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Loop, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOOP", fontFamily = FontFamily.Monospace)
                }
            }
        }

        // == Edit Dialog ==
        if (showEditDialog && editingFrame != null) {
            EditFrameDialog(
                frame = editingFrame!!,
                onDismiss = { showEditDialog = false },
                onConfirm = { newFrame ->
                    viewModel.updateFrame(editingFrameIndex, newFrame)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ActionFrameCard(
    index: Int,
    frame: ActionFrame,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isPlaying) 2.dp else 1.dp,
                color = if (isPlaying) CyberMagenta else CyberBlue,
                shape = RoundedCornerShape(4.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CyberBlue.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(CyberBlue, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DURATION: ${frame.duration}ms",
                    color = CyberMagenta,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Mini visualization of values (just showing first 3 for simplicity)
                Text(
                    text = "AXIS: [${ActionSequencerViewModel.pwmToDegree(frame.servos[0])}°] [${ActionSequencerViewModel.pwmToDegree(frame.servos[1])}°] ...",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun EditFrameDialog(
    frame: ActionFrame,
    onDismiss: () -> Unit,
    onConfirm: (ActionFrame) -> Unit
) {
    var duration by remember { mutableStateOf(frame.duration.toString()) }
    // Store degrees instead of PWM for editing
    var servoDegrees = remember {
        frame.servos.map { ActionSequencerViewModel.pwmToDegree(it) }.toMutableStateList()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = CyberDim,
            border = androidx.compose.foundation.BorderStroke(1.dp, CyberBlue),
            modifier = Modifier.fillMaxWidth().height(600.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "EDIT KEYFRAME",
                    color = CyberText,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Sliders List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { if (it.all { c -> c.isDigit() }) duration = it },
                            label = { Text("Duration (ms)", color = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CyberText,
                                focusedBorderColor = CyberMagenta,
                                unfocusedBorderColor = CyberBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    itemsIndexed(servoDegrees) { index, degree ->
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "AXIS-${index + 1}",
                                    color = CyberBlue,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${degree}°",
                                    color = CyberMagenta,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Slider(
                                value = degree.toFloat(),
                                onValueChange = { servoDegrees[index] = it.toInt() },
                                valueRange = 0f..180f,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyberMagenta,
                                    activeTrackColor = CyberMagenta,
                                    inactiveTrackColor = CyberBlue
                                )
                            )
                        }
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newDuration = duration.toIntOrNull() ?: 1000
                            val newServos = servoDegrees.map { ActionSequencerViewModel.degreeToPwm(it) }
                            onConfirm(frame.copy(duration = newDuration, servos = newServos))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta)
                    ) {
                        Text("UPDATE", color = Color.White)
                    }
                }
            }
        }
    }
}

package com.aizhigu.armcontroller.data

import android.util.Log

object ToxParser {
    private val TABLE_REGEX = Regex("<Table1>(.*?)</Table1>", RegexOption.DOT_MATCHES_ALL)
    private val SERVO_REGEX = Regex("#([1-6])SV.*?P(\\d+)")
    private val DURATION_REGEX = Regex("T(\\d+)") // Usually in Cmd tag

    fun parse(xmlContent: String): List<ActionFrame> {
        val frames = mutableListOf<ActionFrame>()
        val matches = TABLE_REGEX.findAll(xmlContent)
        
        for (match in matches) {
            val tableContent = match.groupValues[1]
            
            // Extract Duration from Cmd or anywhere
            // <Cmd>...T1000</Cmd>
            val durationMatch = DURATION_REGEX.find(tableContent)
            val duration = durationMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1000
            
            // Extract Servos
            // #1SV...P1500...
            val servoMap = mutableMapOf<Int, Int>()
            val servoMatches = SERVO_REGEX.findAll(tableContent)
            
            for (m in servoMatches) {
                val index = m.groupValues[1].toInt() // 1-6
                val pwm = m.groupValues[2].toInt()
                servoMap[index] = pwm
            }
            
            // Fill 6 axes (default 1500 if missing)
            val servos = List(6) { i ->
                servoMap[i + 1] ?: 1500
            }
            
            frames.add(ActionFrame(
                servos = servos,
                duration = duration
            ))
        }
        
        return frames
    }
}

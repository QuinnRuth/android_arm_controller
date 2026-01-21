package com.aizhigu.armcontroller.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldBeInRange
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll

class InputValidationPropertyTest : StringSpec({
    
    // Feature: action-sequencer, Property 1: Input Validation and Clamping
    "PWM values should be clamped to 500-2500 range for all inputs" {
        checkAll(100, Arb.int()) { pwm ->
            val servos = (1..6).associateWith { pwm }
            val frame = ActionFrameFactory.create(
                sequenceId = 0,
                duration = 1000,
                servos = servos,
                soundId = null
            )
            
            frame.servos.values.forEach { clampedPwm ->
                clampedPwm shouldBeInRange 500..2500
            }
        }
    }
    
    // Feature: action-sequencer, Property 1: Input Validation and Clamping
    "Duration should be clamped to 500-5000ms range for all inputs" {
        checkAll(100, Arb.int()) { duration ->
            val servos = (1..6).associateWith { 1500 }
            val frame = ActionFrameFactory.create(
                sequenceId = 0,
                duration = duration,
                servos = servos,
                soundId = null
            )
            
            frame.duration shouldBeInRange 500..5000
        }
    }
    
    // Feature: action-sequencer, Property 1: Input Validation and Clamping
    "Sound ID should be clamped to 1-255 range for all non-null inputs" {
        checkAll(100, Arb.int()) { soundId ->
            val servos = (1..6).associateWith { 1500 }
            val frame = ActionFrameFactory.create(
                sequenceId = 0,
                duration = 1000,
                servos = servos,
                soundId = soundId
            )
            
            frame.soundId?.let { clampedSoundId ->
                clampedSoundId shouldBeInRange 1..255
            }
        }
    }
    
    // Feature: action-sequencer, Property 1: Input Validation and Clamping
    "All servo indices should be preserved and mapped correctly" {
        checkAll(100, Arb.int()) { pwm ->
            val servos = (1..6).associateWith { pwm }
            val frame = ActionFrameFactory.create(
                sequenceId = 0,
                duration = 1000,
                servos = servos,
                soundId = null
            )
            
            // Verify all 6 servos are present
            frame.servos.size shouldBe 6
            
            // Verify all servo indices 1-6 are present
            (1..6).forEach { index ->
                frame.servos.containsKey(index) shouldBe true
            }
        }
    }
    
    // Feature: action-sequencer, Property 1: Input Validation and Clamping
    "Clamping should be idempotent - clamping already valid values should not change them" {
        checkAll(100, Arb.int(500, 2500), Arb.int(500, 5000), Arb.int(1, 255).orNull()) { pwm, duration, soundId ->
            val servos = (1..6).associateWith { pwm }
            val frame1 = ActionFrameFactory.create(
                sequenceId = 0,
                duration = duration,
                servos = servos,
                soundId = soundId
            )
            
            // Create another frame with the already clamped values
            val frame2 = ActionFrameFactory.create(
                sequenceId = 0,
                duration = frame1.duration,
                servos = frame1.servos,
                soundId = frame1.soundId
            )
            
            // Values should be identical (idempotent)
            frame1.duration shouldBe frame2.duration
            frame1.servos shouldBe frame2.servos
            frame1.soundId shouldBe frame2.soundId
        }
    }
    
    // Feature: action-sequencer, Property 1: Input Validation and Clamping
    "Extreme values should be handled correctly" {
        checkAll(100, Arb.choice(
            Arb.constant(Int.MIN_VALUE),
            Arb.constant(Int.MAX_VALUE),
            Arb.constant(0),
            Arb.constant(-1),
            Arb.int()
        )) { extremeValue ->
            val servos = (1..6).associateWith { extremeValue }
            val frame = ActionFrameFactory.create(
                sequenceId = 0,
                duration = extremeValue,
                servos = servos,
                soundId = extremeValue
            )
            
            // All values should be within valid ranges regardless of input
            frame.duration shouldBeInRange 500..5000
            frame.servos.values.forEach { pwm ->
                pwm shouldBeInRange 500..2500
            }
            frame.soundId?.let { soundId ->
                soundId shouldBeInRange 1..255
            }
        }
    }
})
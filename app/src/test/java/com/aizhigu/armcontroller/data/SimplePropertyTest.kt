package com.aizhigu.armcontroller.data

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class SimplePropertyTest : StringSpec({
    
    "ActionFrameFactory should clamp PWM values correctly" {
        checkAll(100, Arb.int()) { pwm ->
            val servos = (1..6).associateWith { pwm }
            val frame = ActionFrameFactory.create(
                sequenceId = 0,
                duration = 1000,
                servos = servos,
                soundId = null
            )
            
            frame.servos.values.forEach { clampedPwm ->
                clampedPwm >= 500 shouldBe true
                clampedPwm <= 2500 shouldBe true
            }
        }
    }
    
    "ActionFrameFactory should clamp duration correctly" {
        checkAll(100, Arb.int()) { duration ->
            val servos = (1..6).associateWith { 1500 }
            val frame = ActionFrameFactory.create(
                sequenceId = 0,
                duration = duration,
                servos = servos,
                soundId = null
            )
            
            frame.duration >= 500 shouldBe true
            frame.duration <= 5000 shouldBe true
        }
    }
})
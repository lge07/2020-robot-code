package frc.robot.subsystems

import com.revrobotics.ColorSensorV3
import edu.wpi.first.wpilibj.SpeedController
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj.util.Color
import edu.wpi.first.wpilibj2.command.SubsystemBase
import frc.robot.Constants


/**
 * Indexer Subsystem
 *
 * Just a indexer motor
 */
class GateSubsystem(val motor: SpeedController, val colorSensor: ColorSensorV3) : SubsystemBase() {

    override fun periodic() {
        debugMeasurment()
    }

    /* set a motor speed, no matter if a ball is in the gate or not */
    fun setSpeed(speed: Double) {
        motor.set(speed)
    }

    /* set a speed (only run if a ball isn't in gate) */
    fun setSpeedSensored(speed: Double) {
        if (isBallTriggered()) {
            motor.set(0.0)
        } else {
            motor.set(speed)
        }
    }

    /* check if ball sensor is triggered */
    fun isBallTriggered(): Boolean {
        val IR = colorSensor.getIR()
        return IR > (Constants.constants["GateColorSensorThreshold"] ?: 7.0) || IR == 0
    }

    /* print status of sensor + its reading */
    fun debugMeasurment() {
        SmartDashboard.putBoolean("Gate Ball Sensor", colorSensor.getIR() != 0)
        SmartDashboard.putNumber("Gate Ball Sensor Reading", colorSensor.getIR().toDouble())
    }
}

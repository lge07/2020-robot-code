package frc.robot.subsystems

import edu.wpi.first.wpilibj2.command.SubsystemBase
import com.revrobotics.CANSparkMax
import com.revrobotics.CANError
import com.revrobotics.CANEncoder
import com.revrobotics.CANPIDController
import edu.wpi.first.wpilibj.SpeedController

/**
 * Shooter Subsystem
 *
 * Just a flywheel motor
 */
class ShooterSubsystem(val flywheelMotor: CANSparkMax) : SubsystemBase() {
    /* default command (don't spin) */
    override fun periodic() {
        flywheelMotor.set(0.0)
    }

    /* set a motor speed */
    fun setSpeed(speed: Double) {
        flywheelMotor.set(speed)
    }

    fun getPidController(): CANPIDController{
        return flywheelMotor.getPIDController()
    }

    fun getEncoder(): CANEncoder{
        return flywheelMotor.getEncoder()
    }

    fun pidWrite(output: Double){
        flywheelMotor.pidWrite(output)
    }
}
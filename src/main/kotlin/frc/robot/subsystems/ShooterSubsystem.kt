/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems

import edu.wpi.first.wpilibj2.command.SubsystemBase
import com.revrobotics.CANSparkMax

class ShooterSubsystem(val motor: CANSparkMax) : SubsystemBase() {
  /**
   * Creates a new ExampleSubsystem.
   */

  init{
    var encoder = motor.getEncoder()
  }

  fun setSpeed(speed: Double){
      motor.set(speed)
  }

  fun getVelocity(): Double{
    return encoder.getVelocity()
  }
  /**
   * Will be called periodically whenever the CommandScheduler runs.
   */
  override fun periodic() {
      motor.set(0.0)
  }
}
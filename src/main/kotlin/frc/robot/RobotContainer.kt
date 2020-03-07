/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot

import frc.robot.commands.*
import frc.robot.subsystems.*
import frc.robot.triggers.*
import edu.wpi.first.wpilibj2.command.Command
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import edu.wpi.first.wpilibj.GenericHID.Hand.*
import edu.wpi.first.wpilibj.XboxController.Button.*

import com.ctre.phoenix.motorcontrol.can.*
import com.kauailabs.navx.frc.AHRS
import edu.wpi.first.wpilibj2.command.button.JoystickButton
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel.MotorType
import edu.wpi.first.wpilibj.*
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup
import edu.wpi.first.wpilibj2.command.button.Trigger
import edu.wpi.first.wpilibj2.command.*

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
class RobotContainer {
    val sch = CommandScheduler.getInstance()
    var m_autoCommandChooser: SendableChooser<Command> = SendableChooser()

    /* controller0 - secondary driver controller */
    val controller0 = XboxController(1)
    /* controller1 - primary driver controller (overriden by controller0) */
    val controller1 = XboxController(0)

    /** --- setup drivetrain --- **/
    val motorFrontLeft = WPI_TalonSRX(Constants.kDrivetrainFrontLeftPort)
    val motorBackLeft = WPI_TalonSRX(Constants.kDrivetrainBackLeftPort)
    val motorFrontRight = WPI_TalonSRX(Constants.kDrivetrainFrontRightPort)
    val motorBackRight = WPI_TalonSRX(Constants.kDrivetrainBackRightPort)


    /* keep speeds same on motors on each side */
    val motorsLeft = SpeedControllerGroup(motorFrontLeft, motorBackLeft)
    val motorsRight = SpeedControllerGroup(motorFrontRight, motorBackRight)


    val gyro = AHRS()
    val leftDrivetrainEncoder = Encoder(Constants.leftDrivetrainEncoderPortA, Constants.leftDrivetrainEncoderPortB)
    val rightDrivetrainencoder = Encoder(Constants.rightDrivetrainEncoderPortA, Constants.rightDrivetrainEncoderPortB)

    val drivetrain = DrivetrainSubsystem(motorsLeft, motorsRight, gyro, leftDrivetrainEncoder, rightDrivetrainencoder)
    val shooter = ShooterSubsystem(CANSparkMax(Constants.kShooterPort, MotorType.kBrushless))
    val intake = IntakeSubsystem(WPI_TalonSRX(Constants.kIntakePort), WPI_TalonSRX(Constants.kIntake2Port))

    val indexer = IndexerSubsystem(WPI_TalonSRX(Constants.kIndexerPort))
    val gate = GateSubsystem(WPI_TalonSRX(Constants.kGatePort))
    val lights = LEDSubsystem(AddressableLED(Constants.kLED0Port), 60, DriverStation.getInstance())

    /*** --- commands --- ***/
    //drive by a joystick (controller1)
    val XboxDriveCommand = XboxDrive(drivetrain, controller1)

    /** -- 0 point autos -- **/
    val noAuto = DriveDoubleSupplier(drivetrain, { 0.0 }, { 0.0 })

    /** --- 5 point autos --- **/
    //backup simple auto
    val backupAuto = DriveDoubleSupplier(drivetrain, { -0.3 }, { 0.0 }).withTimeout(2.0)
    //forward simple auto
    val forwardAuto = DriveDoubleSupplier(drivetrain, { 0.3 }, { 0.0 }).withTimeout(2.0)

    //backup and SPIN!! (looking cool is basically the same thing as winning)
    val spinAuto = SequentialCommandGroup(
        DriveDoubleSupplier(drivetrain, { -0.3 }, { 0.0 }).withTimeout(2.0),
        DriveDoubleSupplier(drivetrain, { 0.0 }, { 0.5 }).withTimeout(12.0)
    )

    /** -- more than 5 point autos (hopefully) -- **/
    // power port vision
    val visionHighGoalLineUp = { SequentialCommandGroup(
            DriveDoubleSupplier(drivetrain, { 0.75}, { 0.0 }).withTimeout(0.2),
            DriveDoubleSupplier(drivetrain, { -0.75}, { 0.0 }).withTimeout(0.2),
            DriveDoubleSupplier(drivetrain, { 0.0 }, {0.0}).withTimeout(1.0),
        VisionHighGoal(drivetrain, 0.3),
        DriveDoubleSupplier(drivetrain, { 0.3 }, { 0.0 }).withTimeout(0.5),
        CompositeShoot(intake, indexer, gate, shooter, Constants.kAutoShootTime),
            DriveDoubleSupplier(drivetrain, {-0.3}, {0.0}).withTimeout(Constants.kAutoBackupTime),
            TurnToAngle(drivetrain, drivetrain.getAngle() + 180, 0.0)
    ) }

    val forwardShootAuto = SequentialCommandGroup(
        DriveDoubleSupplier(drivetrain, { 0.75 }, { 0.0 }).withTimeout(0.2),
        DriveDoubleSupplier(drivetrain, { 0.0 }, { 0.0 }).withTimeout(1.5),
        DriveDoubleSupplier(drivetrain, { 0.3 }, { 0.0 }).withTimeout(2.5),
        CompositeShoot(intake, indexer, gate, shooter, 5.0)
    )

    val forwardShootAutoNoIntake = SequentialCommandGroup(
        ParallelCommandGroup (
            SequentialCommandGroup(
                DriveDoubleSupplier(drivetrain, { 0.45 }, { 0.0 }).withTimeout(0.5),
		        DriveDoubleSupplier(drivetrain, { -0.45 }, {0.0}).withTimeout(0.5),
                DriveDoubleSupplier(drivetrain, { 0.0 }, { 0.0 }).withTimeout(1.5),
                DriveDoubleSupplier(drivetrain, { 0.3 }, { 0.0 }).withTimeout(3.5)
            ),
            FixedIntakeSpeed(intake, { 0.0 }),
            FixedIndexerSpeed(indexer, { 0.0 })
        ).withTimeout(6.0),
        CompositeShoot(intake, indexer, gate, shooter, 5.0),
        ParallelCommandGroup(
            FixedIntakeSpeed(intake, { 0.0 }),
            FixedIndexerSpeed(indexer, { 0.0 })
        )

    )


    /*LED Lights */
    val setAlliance = SetAlliance(lights)
    val setRed = SetColor(lights, 255, 0, 0)
    val setBlue = SetColor(lights, 0, 0, 255)
    val setWhite = SetColor(lights, 255, 255, 255)
    val setRainbow = SetRainbow(lights)

    /**
     * The container for the robot.  Contains subsystems, OI devices, and commands.
     */
    init {
        // Configure the button bindings
        configureButtonBindings()
    }

    /**
     * Use this method to define your button->command mappings.  Buttons can be created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling passing it to a
     * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */

    fun configureButtonBindings() {
        Constants.loadConstants()
        /* Shooting and vision lining up */
        JoystickButton(controller1, kBumperRight.value).whenHeld(
            CompositeShoot(intake, indexer, gate, shooter, 5.0)
        )
        JoystickButton(controller1, kBumperLeft.value).whenHeld(
            visionHighGoalLineUp()
        )



        /* setup default commands */
        drivetrain.defaultCommand = XboxDriveCommand
        gate.defaultCommand = FixedGateSpeed(gate, {
            if(controller0.bButton) Constants.kGateDir * controller0.getY(kLeft) else 0.0
        })
        shooter.defaultCommand = FixedShooterSpeed(shooter, {
            if(controller0.aButton) Constants.kShooterSpeed else 0.0
        })

        indexer.defaultCommand = FixedIndexerSpeed(indexer, {
            if(controller0.getBumper(kLeft) || controller1.xButton) Constants.kIndexerSpeed else (
                if(controller0.xButton) controller0.getY(kLeft) * Constants.kIndexerDir else 0.0 )
        })
        intake.defaultCommand = FixedIntakeSpeed(intake, {
            if(controller0.getBumper(kRight) || controller1.xButton) Constants.kIntakeSpeed else (
                if(controller0.yButton) controller0.getY(kLeft) * Constants.kIntakeDir else 0.0 )
        })
        lights.defaultCommand = setAlliance

        /* set options for autonomous */
        m_autoCommandChooser.setDefaultOption("Power Port Vision Autonomous", visionHighGoalLineUp())
        m_autoCommandChooser.addOption("Backup 2s Autonomous", backupAuto)
        m_autoCommandChooser.addOption("Forward 2s Autonomous", forwardAuto)
        m_autoCommandChooser.addOption("Backup 2s and Look Cool Autonomous", spinAuto)
        m_autoCommandChooser.addOption("Forward 4.5s and Shoot", forwardShootAuto)
        m_autoCommandChooser.addOption("Forward 4.5s and Shoot No Intake", forwardShootAutoNoIntake)
        m_autoCommandChooser.addOption("No auto (DON'T PICK)", noAuto)

        SmartDashboard.putData("Auto mode", m_autoCommandChooser)
    }

    fun getAutonomousCommand(): Command {
        /*var autoVoltageConstraint = DifferentialDriveVoltageConstraint(
            SimpleMotorFeedforward(Constants.chassisksVolts,
                Constants.kvVoltSecondsPerMeter,
                Constants.kaVoltSecondsSquaredPerMeter),
            Constants.kDriveKinematics,
            10.0)

        val config: TrajectoryConfig = TrajectoryConfig(
            Constants.kMaxSpeedMetersPerSecond,
            Constants.kMaxAccelerationMetersPerSecondSquared
        )
            .setKinematics(Constants.kDriveKinematics)
            .addConstraint(autoVoltageConstraint)

        val trajectoryJSON: String = "PathWeaver/Paths/AutoRoute"
        val trajectoryPath: Path = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON)
        val trajectory: Trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath)

        val ramseteCommand: RamseteCommand = RamseteCommand(
            trajectory,
            drivetrain::getPose,
            RamseteController(Constants.kRamseteB,Constants.kRamseteZeta),
            SimpleMotorFeedforward(Constants.chassisksVolts,
                Constants.kvVoltSecondsPerMeter,
                Constants.kaVoltSecondsSquaredPerMeter),
            Constants.kDriveKinematics,
            drivetrain::getWheelSpeeds,
            PIDController(Constants.kPDriveVel,0.0,0.0),
            PIDController(Constants.kPDriveVel,0.0,0.0),
            drivetrain::tankDriveVolts,
            arrayOf<Subsystem>(drivetrain)
        )*/

        // Return the selected command
        return m_autoCommandChooser.selected
    }
}

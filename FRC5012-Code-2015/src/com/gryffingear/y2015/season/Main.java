package com.gryffingear.y2015.season;

import com.gryffingear.y2015.config.Ports;
import com.gryffingear.y2015.systems.Robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PowerDistributionPanel;

public class Main extends IterativeRobot {

  Joystick leftstick = new Joystick(Ports.Controls.LEFT_JOY_PORT);
  Joystick rightstick = new Joystick(Ports.Controls.RIGHT_JOY_PORT);
  Joystick gamepad = new Joystick(Ports.Controls.OPERATOR_JOY_PORT);

  Robot bot = Robot.getInstance();
  PowerDistributionPanel pdp = new PowerDistributionPanel();
  
  @Override
  public void robotInit() {

  }

  @Override
  public void autonomousInit() {

  }

  @Override
  public void autonomousPeriodic() {

  }

  @Override
  public void teleopInit() {

    pdp.clearStickyFaults();
    pdp.getTotalCurrent();

    // test66
  }

  @Override
  public void teleopPeriodic() {

    // double throttle = (leftstick.getRawAxis(0) +
    // rightstick.getRawAxis(1)) /2;
    // double turning = (leftstick.getRawAxis(0) - rightstick.getRawAxis(1))
    // /2;

    // Driver Controls
    bot.drive.tankDrive(gamepad.getRawAxis(1), gamepad.getRawAxis(3));

    // Operator Controls

    bot.claw.setClaw(gamepad.getRawButton(6));

    if (gamepad.getRawButton(7)) {
      bot.elevator.set(.5);
    } else if (gamepad.getRawButton(8)) {
      bot.elevator.set(-.33);
    } else {
      bot.elevator.set(0);
    }

    System.out.println(pdp.getTotalCurrent());

  }

  /**
   * This function is called periodically during test mode
   */
  @Override
  public void testPeriodic() {

  }

  @Override
  public void disabledPeriodic() {

  }

}

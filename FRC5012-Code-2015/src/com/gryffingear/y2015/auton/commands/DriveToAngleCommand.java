package com.gryffingear.y2015.auton.commands;

import com.gryffingear.y2015.systems.Robot;

import edu.wpi.first.wpilibj.command.Command;

public class DriveToAngleCommand extends Command {

  private double speed = 0.0;
  private double angle = 0.0;
  private double timeout = 0.0;

  public DriveToAngleCommand(double speed, double angle, double timeout) {

    this.speed = speed;
    this.angle = angle;
    this.timeout = timeout;
    this.setTimeout(timeout);
  }

  protected void initialize() {

    Robot.getInstance().drive.resetGyro();
  }

  protected boolean isFinished() {

    return this.isTimedOut();
  }

  protected void execute() {

    double p = 0.025;
    double error = Robot.getInstance().drive.getYaw() - this.angle;
    Robot.getInstance().drive.tankDrive(p * error * speed, -p * error * speed);
  }

  protected void end() {

    Robot.getInstance().drive.tankDrive(0.0, 0.0);
  }

  protected void interrupted() {

    Robot.getInstance().drive.tankDrive(0.0, 0.0);
  }
}

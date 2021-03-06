package com.gryffingear.y2015.systems;

import com.gryffingear.y2015.config.Constants;
import com.gryffingear.y2015.systems.controllers.ElevatorPositionController;
import com.gryffingear.y2015.utilities.Ma3Encoder;

import edu.wpi.first.wpilibj.CANTalon;
import edu.wpi.first.wpilibj.DigitalInput;

public class Elevator {

  private CANTalon elevatorMotor = null;
  private DigitalInput lowerlimit_switch;
  private DigitalInput upperlimit_switch;
  private Ma3Encoder posRef = null;
  public ElevatorPositionController epc = null;

  public Elevator(int elevator_port, int lowerlimit_port, int upperlimit_port, int encoderPort) {

    upperlimit_switch = new DigitalInput(upperlimit_port);
    lowerlimit_switch = new DigitalInput(lowerlimit_port);
    elevatorMotor = configureTalon(new CANTalon(elevator_port));

    posRef = new Ma3Encoder(encoderPort);
    epc = new ElevatorPositionController(posRef);
    epc.setEnabled(true);
    epc.setGains(0.5);
  }

  private CANTalon configureTalon(CANTalon in) {

    in.clearStickyFaults();
    in.changeControlMode(CANTalon.ControlMode.PercentVbus);
    in.setVoltageRampRate(144);
    in.enableBrakeMode(true);
    in.enableControl();
    return in;
  }

  public void set(double value) {

    value = -value;
    if (value < 0.0 && getUpperLimitSwitch()) {
      value = 0.0;
    } else if (value > 0.0 && getLowerLimitSwitch()) {
      value = 0.0;
    }

    if (getLowerLimitSwitch()) {
      this.posRef.reset();
    } else if (getUpperLimitSwitch()) {
      // this.posRef.setOffset(11.0);
    }

    elevatorMotor.set(value);
  }

  public boolean getLowerLimitSwitch() {

    return !lowerlimit_switch.get();
  }

  public boolean getUpperLimitSwitch() {

    return !upperlimit_switch.get();
  }

  public double getCurrent() {

    return elevatorMotor.getOutputCurrent();
    // Todo: read current from either PDP or CANTalon...
  }

  public void resetEncoder() {

    this.posRef.reset();
  }

  public double getEncoder() {

    return this.posRef.get();
  }
  private double openLoopInput = 0.0;

  public void setOpenLoop(double in) {

    openLoopInput = in;
  }

  private double position = 0.0; // Todo set to default position

  public void setPosition(double position) {

    this.position = position;
  }

  private int state = 0;
  private int prevState = 0;
  private int lastState = 0;

  public void setState(int in) {

    this.state = in;
  }

  public int getState() {

    return this.state;
  }

  public boolean isResetting() {

    return this.state == States.RESET_DOWN;
  }
  public static class States {

    public static final int DISABLED = 0;
    public static final int OPEN_LOOP = 1;
    public static final int CLOSED_LOOP = 2;
    public static final int MOVING_UP = 3;
    public static final int MOVING_DN = 4;
    public static final int HOLDING = 5;
    public static final int TEST_CALIBRATE = 6;
    public static final int LIVE_CALIBRATE = 7;
    public static final int RESET_DOWN = 8;
  }

  long timeSinceStateChange = 0;
  public void run() {

    prevState = state;

    // Code that should run after a change of state
    if (prevState != state) {
      lastState = prevState;
      // Store time since state change.
      timeSinceStateChange = System.currentTimeMillis();

    }

    double output = 0.0;
    epc.setPosition(position);
    switch (state) {
    case States.DISABLED: // Disabled state. Stop everything
      output = 0.0;
      epc.setEnabled(false);

      break;

    case States.OPEN_LOOP: // Open loop state. send input to output
      epc.setEnabled(false);
      double scale = 1.0;
      if ((this.getEncoder() > 38.0 && openLoopInput > 0.0)) {
        scale = 0.5;
      } else {
        scale = 1;
      }
      output = openLoopInput * scale;

      break;
    case States.CLOSED_LOOP: // Open loop state. send input to output

      epc.setEnabled(true);
      if (epc.isUnder())
        state = States.MOVING_UP;
      else if (!epc.isUnder())
        state = States.MOVING_DN;

      break;
    case States.MOVING_UP: // Closed loop moving up. Higher gains = moar
      // power

      epc.setEnabled(true);
      epc.setGains(Constants.Elevator.P_UP);
      if (epc.isNearTarget())
        setState(state = States.HOLDING);

      break;
    case States.MOVING_DN: // Closed loop moving down, lower gains = slower
      // to fall

      epc.setEnabled(true);
      epc.setGains(Constants.Elevator.P_DN);
      if (epc.isNearTarget())
        setState(States.HOLDING);

      break;
    case States.HOLDING: // Closed loop holding position, super high gains =
      // super holding power

      epc.setEnabled(true);
      epc.setGains(Constants.Elevator.P_HOLD);
      // Exit holding state if
      if ((!epc.isNearTarget()) && epc.isUnder())
        setState(States.MOVING_UP);
      if ((!epc.isNearTarget()) && (!epc.isUnder()))
        setState(States.MOVING_DN);

      break;
    case States.TEST_CALIBRATE: // Calibration in test mode.
      if (!getLowerLimitSwitch() || (System.currentTimeMillis() - timeSinceStateChange) < 3000) {
        output = 1.0;
      } else {
        this.resetEncoder();
        output = 0;
        setState(States.DISABLED); // Disable elevator after calibration
      }
      break;
    case States.LIVE_CALIBRATE: // Calibration during operation
      if (!getLowerLimitSwitch() || (System.currentTimeMillis() - timeSinceStateChange) < 3000) {
        output = 1.0;
      } else {
        this.resetEncoder();
        output = 0;
        setState(lastState); // Return to the previously running state after
                             // calibration
      }
    case States.RESET_DOWN: // Calibration during operation

      epc.setEnabled(false);
      if (!getLowerLimitSwitch()) {
          output = -1.0;
      } else {
        this.setState(prevState);
      }
      break;
    default:
      output = 0;
      epc.setEnabled(false);
      System.out.println("Invalid state!");
      break;
    }

    if (epc.getEnabled()) {
      output = epc.get();
    }

    this.set(output);
  }
}

import joystick.JInputJoystick;

import net.java.games.input.*;

/**
 * CameraMountJoystickDriver Main class<p>
 * This program will search for a joystick and a teensy serial port with firmware loaded from the
 * <a href="https://github.com/Team997Coders/MiniPanTiltTeensy">MiniPanTiltTeensy</a> project. The left joystick
 * will control the <a href="https://www.adafruit.com/product/1967">Adafruit servos</a> and the left joystick button will center the servos.
 */
public class Main {
  private static final int CENTER_BUTTON = 10;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // First create a joystick object.
    JInputJoystick joystick = new JInputJoystick(Controller.Type.STICK, Controller.Type.GAMEPAD);

    // Check if a joystick was found.
    if( !joystick.isControllerConnected() ){
      System.out.println("No controller found!");
      System.exit(0);
    }

    // Create a teensy object.
    MiniPanTiltTeensy teensy = null;
    try {
      // Get a reference to the MiniPanTiltTeensy
      teensy = new MiniPanTiltTeensy();
      // Center it
      teensy.center();
      // TODO: Why do I need to do this twice...there is a bug at startup.
      teensy.center();
    } catch (Exception e) {
      System.out.println(e.getMessage());
      System.exit(1);
    }

    while(true) {
      // Get current state of joystick! And check, if joystick is disconnected.
      if( !joystick.pollController() ) {
        System.out.println("Controller disconnected!");
        teensy.closePort();
        System.exit(1);
      }
      
      // Left controller joystick
      float xValueLeftJoystick = joystick.getXAxisValue();
      float yValueLeftJoystick = joystick.getYAxisValue();        

      // Throw away tiny values in case joystick gives back small
      // junk when zeroed.
      if (xValueLeftJoystick > -0.02 && xValueLeftJoystick < 0.02) {
        xValueLeftJoystick = 0;
      }
      if (yValueLeftJoystick > -0.02 && yValueLeftJoystick < 0.02) {
        yValueLeftJoystick = 0;
      }
      // Center if button pushed
      try {
        if (joystick.getButtonValue(CENTER_BUTTON)) {
          if (!teensy.center()) {
            System.out.println("Teensy disconnected!");
            System.exit(1);
          }
        } else {
          // Otherwise slew
          if (!teensy.slew(Math.round(xValueLeftJoystick * 100) * -1, Math.round(yValueLeftJoystick * 100))) {
            System.out.println("Teensy disconnected!");
            System.exit(1);
          }
        }
      } catch (IndexOutOfBoundsException e) {
        System.out.println("Center button not found...continuing.");
      }

      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        teensy.closePort();
        System.exit(0);
      }
    }
  }    
}
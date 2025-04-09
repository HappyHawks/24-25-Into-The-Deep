package pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Arm Servo test", group = "Tuner Tests" )
public class armServo extends LinearOpMode{

    Servo armServo, arm_clawServo, fiveTurn;

    @Override
    public void runOpMode() {
        armServo = hardwareMap.get(Servo.class, "armServo");
        arm_clawServo = hardwareMap.get(Servo.class, "arm_clawServo");
        fiveTurn = hardwareMap.get(Servo.class, "fiveTurn");
        waitForStart();

        while (opModeIsActive()) {
            if (gamepad2.a) {
                armServo.setPosition(.1); // Move to max position
                arm_clawServo.setPosition(.2);
            } else if (gamepad2.b) {
                armServo.setPosition(5); // Move to min position
            }

            if (gamepad2.x) {
                fiveTurn.setPosition(.01);
            } else if (gamepad2.left_bumper) {
                fiveTurn.setPosition(.02);
            } else {
                fiveTurn.setPosition(5);
            }

            telemetry.addData("Arm Servo Position", armServo.getPosition());
            telemetry.update();
        }
    }

}

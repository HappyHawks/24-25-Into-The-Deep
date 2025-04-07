package pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Two Player Teleop")
public class p2teleop extends LinearOpMode {

    // Declare hardware variables
    DcMotor slide_horizontalMotor, winch_rightMotor, winch_leftMotor, slide_verticalMotor, hang_leftServo, hang_rightServo;
    DcMotor topRight, topLeft, bottomRight, bottomLeft;  // Declare drivetrain motors
        Servo arm_clawServo, armServo, claw, rdServo, ldServo;

    // Slide speed variable
    private static final double SLIDE_SPEED = 0.9; // Adjust this to control the speed

    @Override
    public void runOpMode() {
        // Initialize hardware
        slide_horizontalMotor = hardwareMap.dcMotor.get("slide_horizontalMotor");
        winch_rightMotor = hardwareMap.dcMotor.get("winch_rightMotor");
        winch_leftMotor = hardwareMap.dcMotor.get("winch_leftMotor");
        slide_verticalMotor = hardwareMap.dcMotor.get("slide_verticalMotor");

        // Initialize drivetrain motors
        topLeft = hardwareMap.dcMotor.get("leftFront");
        topRight = hardwareMap.dcMotor.get("rightFront");
        bottomRight = hardwareMap.dcMotor.get("rightRear");
        bottomLeft = hardwareMap.dcMotor.get("leftRear");
        arm_clawServo = hardwareMap.servo.get("arm_clawServo");
        armServo = hardwareMap.servo.get("armServo");
        claw = hardwareMap.servo.get("claw");
        rdServo = hardwareMap.servo.get("rdServo");
        ldServo = hardwareMap.servo.get("ldServo");

        // Reverse motors if needed
        topLeft.setDirection(DcMotor.Direction.REVERSE);
        bottomLeft.setDirection(DcMotor.Direction.REVERSE);
        slide_verticalMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Variables for toggling the servo positions
        boolean xToggle = false;
        boolean yToggle = false;
        boolean previousX = false;
        boolean previousY = false;

        waitForStart();

        while (opModeIsActive()){


            if (gamepad1.right_bumper) {
                winch_leftMotor.setPower(1);
                winch_rightMotor.setPower(-1);
            } else if (gamepad1.left_bumper) {
                winch_rightMotor.setPower(1);
                winch_leftMotor.setPower(-1);
            } else {
                winch_leftMotor.setPower(0);
                winch_rightMotor.setPower(0);
            }

            // -------------------- ARM SERVO CONTROL --------------------
            if (gamepad2.b) {
                armServo.setPosition(.3);
            } else if (gamepad2.a) {
                armServo.setPosition(1);
            }

            telemetry.addData("Arm Servo Position", armServo.getPosition());

            //if (gamepad2.right_trigger == 1) {
              //  armServo.setPosition(.5);
            //}

            //if (gamepad2.left_trigger == 1) {
              //armServo.setPosition(0);
            //}

            //if (gamepad2.a) {
                //armServo.setPosition(1);
            //}

            //if (gamepad2.y) {
                //telemetry.addData("y button detected");
            // this was commented out  because it was used for a test if the servo programming was wrong or the cable was damaged.
            // it ended up being the cable.

            // --- Toggle for arm_clawServo using gamepad2.y ---
            // Rising edge detection for y button
            if (gamepad2.y && !previousY) {
                yToggle = !yToggle;
            }

            // Set arm_clawServo based on toggle state
            if (yToggle) {
                arm_clawServo.setPosition(1);
            } else {
                arm_clawServo.setPosition(0);
            }

            // --- Toggle for claw servo using gamepad2.x ---
            // Rising edge detection for x button
            if (gamepad2.x && !previousX) {
                xToggle = !xToggle;
            }
            // Set claw servo based on toggle state
            if (xToggle) {
                claw.setPosition(1);
            } else {
                claw.setPosition(0.2);
            }

            // Update previous button states for rising edge detection
            previousX = gamepad2.x;
            previousY = gamepad2.y;


            //rdServo.setPosition(0.5);
            //ldServo.setPosition(0.5);
            double servoStep = 0.1;

            // Get the current positions of the servos
            double rdServoPosition = rdServo.getPosition();
            double ldServoPosition = ldServo.getPosition();

            // Move servos when bumpers are held
            if (gamepad2.left_bumper) {
                // Move rdServo upward and ldServo downward
                rdServoPosition += servoStep;
                ldServoPosition -= servoStep;
            } else if (gamepad2.right_bumper) {
                // Move rdServo downward and ldServo upward
                rdServoPosition -= servoStep;
                ldServoPosition += servoStep;
            }

            // Clamp positions to valid range (0 to 1)
            rdServoPosition = Math.max(0, Math.min(1, rdServoPosition));
            ldServoPosition = Math.max(0, Math.min(1, ldServoPosition));

            // Apply the new positions
            rdServo.setPosition(rdServoPosition);
            ldServo.setPosition(ldServoPosition);

            // Slide control (hold to move, release to stop)
            if (gamepad2.dpad_up) {
                slide_verticalMotor.setPower(-SLIDE_SPEED); // Move up
            } else if (gamepad2.dpad_down) {
                slide_verticalMotor.setPower(SLIDE_SPEED); // Move down
            } else {
                slide_verticalMotor.setPower(0); // Stop when released
            }

            // Slide control (hold to move, release to stop)
            if (gamepad2.dpad_left) {
                slide_horizontalMotor.setPower(-SLIDE_SPEED); // Move up
            } else if (gamepad2.dpad_right) {
                slide_horizontalMotor.setPower(SLIDE_SPEED); // Move down
            } else {
                slide_horizontalMotor.setPower(0); // Stop when released
            }

            // Drivetrain control
            driveTrainControl();

            telemetry.update();
        }
    }

    // Method to control the drivetrain using mecanum logic
    private void driveTrainControl() {
        double drive = gamepad1.left_stick_y; // Forward/backward
        double strafe = gamepad1.left_stick_x; // Left/right
        double rotate = gamepad1.right_stick_x; // Rotation

        // Mecanum drive calculation
        double frontLeftPower = (drive + strafe + rotate);
        double backLeftPower = (drive - strafe + rotate);
        double frontRightPower = (drive - strafe - rotate);
        double backRightPower = (drive + strafe - rotate);

        // Normalize the values to ensure the power values are within the range [-1, 1]
        double max = Math.max(Math.abs(frontLeftPower), Math.max(Math.abs(backLeftPower), Math.max(Math.abs(frontRightPower), Math.abs(backRightPower))));
        if (max > 1.0) {
            frontLeftPower /= max;
            backLeftPower /= max;
            frontRightPower /= max;
            backRightPower /= max;
        }

        // Set motor powers
        topLeft.setPower(frontLeftPower);
        bottomLeft.setPower(backLeftPower);
        topRight.setPower(frontRightPower);
        bottomRight.setPower(backRightPower);
    }
}

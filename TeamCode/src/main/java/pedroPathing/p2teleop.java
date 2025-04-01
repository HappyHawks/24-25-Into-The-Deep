package pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Two Player Teleop")
public class p2teleop extends LinearOpMode {

    // Declare hardware variables
    DcMotor slide_horizontalMotor, winch_rightMotor, winch_leftMotor, slide_verticalMotor;
    DcMotor topRight, topLeft, bottomRight, bottomLeft;  // Declare drivetrain motors
    Servo arm_clawServo, armServo, claw, rdServo, ldServo;

    // Slide speed variable
    private static final double SLIDE_SPEED = 0.5; // Adjust this to control the speed

    @Override
    public void runOpMode() {
        // Initialize hardware
        slide_horizontalMotor = hardwareMap.dcMotor.get("slide_horizontalMotor");
        winch_rightMotor = hardwareMap.dcMotor.get("winch_rightMotor");
        winch_leftMotor = hardwareMap.dcMotor.get("winch_leftMotor");
        slide_verticalMotor = hardwareMap.dcMotor.get("slide_verticalMotor");

        // Initialize drivetrain motors
        topLeft = hardwareMap.dcMotor.get("topLeft");
        topRight = hardwareMap.dcMotor.get("topRight");
        bottomRight = hardwareMap.dcMotor.get("bottomRight");
        bottomLeft = hardwareMap.dcMotor.get("bottomLeft");

        arm_clawServo = hardwareMap.servo.get("arm_clawServo");
        armServo = hardwareMap.servo.get("armServo");
        claw = hardwareMap.servo.get("claw");
        rdServo = hardwareMap.servo.get("rdServo");
        ldServo = hardwareMap.servo.get("ldServo");

        // Reverse motors if needed
        topLeft.setDirection(DcMotor.Direction.REVERSE);
        bottomLeft.setDirection(DcMotor.Direction.REVERSE);
        slide_verticalMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        waitForStart();

        while (opModeIsActive()) {
            // Claw controls
            if (gamepad2.a) {
                arm_clawServo.setPosition(0); // Open claw
            } else {
                arm_clawServo.setPosition(1); // Close claw
            }

            // Arm servo control
            if (gamepad2.b) {
                armServo.setPosition(0);
            } else {
                armServo.setPosition(1);
            }

            // Inside your loop:
            if (gamepad2.x) {
                claw.setPosition(1);
            } else {
                claw.setPosition(0);
            }

            // Servo movement speed (adjust as needed)
            double servoStep = 0.01;

            // Move servos up when left bumper is held
            if (gamepad2.left_bumper) {
                rdServo.setPosition(0);
                ldServo.setPosition(1);
            }

            // Move servos down when right bumper is held
            if (gamepad2.right_bumper) {
                rdServo.setPosition(1);
                ldServo.setPosition(0);
            }

            // Slide control (hold to move, release to stop)
            if (gamepad2.dpad_up) {
                slide_verticalMotor.setPower(SLIDE_SPEED); // Move up
            } else if (gamepad2.dpad_down) {
                slide_verticalMotor.setPower(-SLIDE_SPEED); // Move down
            } else {
                slide_verticalMotor.setPower(0); // Stop when released
            }

            // Slide control (hold to move, release to stop)
            if (gamepad2.dpad_left) {
                slide_horizontalMotor.setPower(SLIDE_SPEED); // Move up
            } else if (gamepad2.dpad_right) {
                slide_horizontalMotor.setPower(-SLIDE_SPEED); // Move down
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

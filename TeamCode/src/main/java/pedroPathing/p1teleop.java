package pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "One Player Teleop")
public class p1teleop extends LinearOpMode {

    // Declare hardware variables
    DcMotor slide_horizontalMotor, winch_rightMotor, winch_leftMotor, slide_verticalMotor;
    DcMotor topRight, topLeft, bottomRight, bottomLeft;  // Drivetrain motors
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

        // Variables for toggling the servo positions
        boolean xToggle = false;
        boolean yToggle = false;
        boolean previousX = false;
        boolean previousY = false;

        waitForStart();

        while (opModeIsActive()){
            // Arm servo control using gamepad1 buttons
            if (gamepad1.b) {
                armServo.setPosition(0.15);
            }
            if (gamepad1.right_trigger == 1) {
                armServo.setPosition(0.5);
            }
            if (gamepad1.left_trigger == 1) {
                armServo.setPosition(0);
            }
            if (gamepad1.a) {
                armServo.setPosition(1);
            }

            // --- Toggle for arm_clawServo using gamepad1.y ---
            if (gamepad1.y && !previousY) {
                yToggle = !yToggle;
            }
            if (yToggle) {
                arm_clawServo.setPosition(1);
            } else {
                arm_clawServo.setPosition(0);
            }

            // --- Toggle for claw servo using gamepad1.x ---
            if (gamepad1.x && !previousX) {
                xToggle = !xToggle;
            }
            if (xToggle) {
                claw.setPosition(1);
            } else {
                claw.setPosition(0.2);
            }
            // Update previous button states for rising edge detection
            previousX = gamepad1.x;
            previousY = gamepad1.y;

            // Control for rdServo and ldServo using bumpers
            double servoStep = 0.1;
            double rdServoPosition = rdServo.getPosition();
            double ldServoPosition = ldServo.getPosition();

            if (gamepad1.left_bumper) {
                rdServoPosition += servoStep;
                ldServoPosition -= servoStep;
            } else if (gamepad1.right_bumper) {
                rdServoPosition -= servoStep;
                ldServoPosition += servoStep;
            }
            // Clamp positions to valid range (0 to 1)
            rdServoPosition = Math.max(0, Math.min(1, rdServoPosition));
            ldServoPosition = Math.max(0, Math.min(1, ldServoPosition));
            rdServo.setPosition(rdServoPosition);
            ldServo.setPosition(ldServoPosition);

            // Slide control using gamepad1 D-pad for vertical movement
            if (gamepad1.dpad_up) {
                slide_verticalMotor.setPower(SLIDE_SPEED); // Move up
            } else if (gamepad1.dpad_down) {
                slide_verticalMotor.setPower(-SLIDE_SPEED); // Move down
            } else {
                slide_verticalMotor.setPower(0);
            }

            // Slide control using gamepad1 D-pad for horizontal movement
            if (gamepad1.dpad_left) {
                slide_horizontalMotor.setPower(SLIDE_SPEED); // Move left
            } else if (gamepad1.dpad_right) {
                slide_horizontalMotor.setPower(-SLIDE_SPEED); // Move right
            } else {
                slide_horizontalMotor.setPower(0);
            }

            // Drivetrain control
            driveTrainControl();

            telemetry.update();
        }
    }

    // Drivetrain control using mecanum drive logic with speed reduction when the Back button is held
    private void driveTrainControl() {
        double speedMultiplier = gamepad1.back ? 0.6 : 1.0;  // 40% speed reduction when Back is held

        double drive = gamepad1.left_stick_y * speedMultiplier;    // Forward/backward
        double strafe = gamepad1.left_stick_x * speedMultiplier;     // Left/right
        double rotate = gamepad1.right_stick_x * speedMultiplier;    // Rotation

        double frontLeftPower = (drive + strafe + rotate);
        double backLeftPower = (drive - strafe + rotate);
        double frontRightPower = (drive - strafe - rotate);
        double backRightPower = (drive + strafe - rotate);

        double max = Math.max(Math.abs(frontLeftPower), Math.max(Math.abs(backLeftPower),
                Math.max(Math.abs(frontRightPower), Math.abs(backRightPower))));
        if (max > 1.0) {
            frontLeftPower /= max;
            backLeftPower /= max;
            frontRightPower /= max;
            backRightPower /= max;
        }

        topLeft.setPower(frontLeftPower);
        bottomLeft.setPower(backLeftPower);
        topRight.setPower(frontRightPower);
        bottomRight.setPower(backRightPower);
    }
}

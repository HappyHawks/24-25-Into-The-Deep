package pedroPathing;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Two Player State Machine Teleop")
public class StateTeleop extends LinearOpMode {

    // Declare Motors and Servos
    DcMotorEx slide_horizontalMotor, winch_rightMotor, winch_leftMotor, slide_verticalMotor;
    DcMotorEx topRight, topLeft, bottomRight, bottomLeft;
    Servo arm_clawServo, armServo, claw, rdServo, ldServo;

    // Constants
    private static final double SLIDE_SPEED = 0.5;
    private static final int HORIZONTAL_EXTEND_INTAKE = -500;
    private static final int HORIZONTAL_EXTEND_DONE = 0;
    private static final double ARM_SERVO_INTAKE = 0.1;  // Adjusted value
    private static final double ARM_SERVO_THROW = 0.9;   // Adjusted value
    private static final double ARM_CLAW_SERVO_OPEN = 1;
    private static final double ARM_CLAW_SERVO_CLOSE = 0.2;

    // Enums for State Machines
    private enum RobotState {
        IDLE,
        INTAKE_READY,
        INTAKE_DONE,
        SAMPLE_OUTAKE,
        SPECIMEN_THROWING,
        SPECIMEN_INTAKE,
        SPECIMEN_OUTAKE
    }

    private RobotState currentState = RobotState.IDLE;

    @Override
    public void runOpMode() {
        // Initialize Hardware
        slide_horizontalMotor = hardwareMap.get(DcMotorEx.class, "slide_horizontalMotor");
        winch_rightMotor = hardwareMap.get(DcMotorEx.class, "winch_rightMotor");
        winch_leftMotor = hardwareMap.get(DcMotorEx.class, "winch_leftMotor");
        slide_verticalMotor = hardwareMap.get(DcMotorEx.class, "slide_verticalMotor");

        topLeft = hardwareMap.get(DcMotorEx.class, "leftFront");
        topRight = hardwareMap.get(DcMotorEx.class, "rightFront");
        bottomRight = hardwareMap.get(DcMotorEx.class, "rightRear");
        bottomLeft = hardwareMap.get(DcMotorEx.class, "leftRear");
        arm_clawServo = hardwareMap.servo.get("arm_clawServo");
        armServo = hardwareMap.get(Servo.class, "armServo");
        claw = hardwareMap.get(Servo.class, "claw");
        rdServo = hardwareMap.servo.get("rdServo");
        ldServo = hardwareMap.servo.get("ldServo");

        topLeft.setDirection(DcMotor.Direction.REVERSE);
        bottomLeft.setDirection(DcMotor.Direction.REVERSE);
        slide_verticalMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialize Servo Positions (IDLE state)
        rdServo.setPosition(0.5);
        ldServo.setPosition(0.5);
        armServo.setPosition(ARM_SERVO_INTAKE);

        // Reset Encoder Positions
        slide_horizontalMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slide_verticalMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        topLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        topRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bottomLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        bottomRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slide_horizontalMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slide_verticalMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        topLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        topRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bottomLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        bottomRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        boolean clawToggle = false;
        boolean previousClaw = false;

        waitForStart();

        while (opModeIsActive()) {
            // State Machine Core
            switch (currentState) {
                case IDLE:
                    handleIdleState();
                    break;
                case INTAKE_READY:
                    handleIntakeReadyState();
                    break;
                case INTAKE_DONE:
                    handleIntakeDoneState();
                    break;
                case SAMPLE_OUTAKE:
                    handleSampleOutakeState();
                    break;
                case SPECIMEN_THROWING:
                    handleSpecimenThrowingState();
                    break;
                case SPECIMEN_INTAKE:
                    handleSpecimenIntakeState();
                    break;
                case SPECIMEN_OUTAKE:
                    handleSpecimenOutakeState();
                    break;
            }

            // Independent Controls (Drive Train & Hanging)
            driveTrainControl();
            handleHanging();

            // Servo Toggles (claw)
            if (gamepad2.x && !previousClaw) clawToggle = !clawToggle;
            claw.setPosition(clawToggle ? 1 : 0.2);
            previousClaw = gamepad2.x;

            // Wrist Control (rd & ld Servos)
            adjustWristPosition();

            telemetry.addData("Current State", currentState);
            telemetry.addData("Horizontal Slide Encoder", slide_horizontalMotor.getCurrentPosition());
            telemetry.addData("Vertical Slide Encoder", slide_verticalMotor.getCurrentPosition());
            telemetry.addData("Top Left Encoder", topLeft.getCurrentPosition());
            telemetry.addData("Top Right Encoder", topRight.getCurrentPosition());
            telemetry.addData("Bottom Left Encoder", bottomLeft.getCurrentPosition());
            telemetry.addData("Bottom Right Encoder", bottomRight.getCurrentPosition());
            telemetry.addData("Arm Servo Position", armServo.getPosition());
            telemetry.update();

            // ARM Servo Control
            if (gamepad2.a) {
                armServo.setPosition(ARM_SERVO_THROW);
            } else if (gamepad2.b) {
                armServo.setPosition(ARM_SERVO_INTAKE);
            }
        }
    }

    // -------------------- STATE HANDLER METHODS --------------------

    private void handleIdleState() {
        // Actions: The slide is all rolled up, the rdServo and ld Servo are all making the claw face up.
        // Arm servo is in position (gamepad.righttrigger == 1 or armServo.setPosition(.5);)
        slide_horizontalMotor.setPower(0);
        slide_verticalMotor.setPower(0);
        rdServo.setPosition(0.5);
        ldServo.setPosition(0.5);
        armServo.setPosition(ARM_SERVO_INTAKE); // Set arm to intake position in IDLE
        arm_clawServo.setPosition(ARM_CLAW_SERVO_OPEN);
        // Transition: System reset or manual trigger
        if (gamepad2.dpad_right) {
            currentState = RobotState.INTAKE_READY;
        }
    }

    private void handleIntakeReadyState() {
        // Actions: Horizontal slide extends into submersible 20cm. Claw opens. Wrist goes down.
        slide_horizontalMotor.setPower(SLIDE_SPEED); // Extend
        claw.setPosition(0); // Open Claw
        rdServo.setPosition(0); // Wrist Down (adjust values)
        ldServo.setPosition(1);

        // Transition: Auto/gamepad2.dpad_right -> Intake Done
        if (slide_horizontalMotor.getCurrentPosition() >= HORIZONTAL_EXTEND_INTAKE) {
            slide_horizontalMotor.setPower(0); // Stop extending
            currentState = RobotState.INTAKE_DONE;
        }
    }

    private void handleIntakeDoneState() {
        // Actions: Close claw. Wrist goes up. Slide extends in 18cm.
        claw.setPosition(1); // Close Claw
        rdServo.setPosition(0.5); // Wrist Up (adjust values)
        ldServo.setPosition(0.5);
        slide_horizontalMotor.setPower(SLIDE_SPEED); // Extend further

        // Transition: Auto completion -> Sample Outake or Specimen Throwing
        if (slide_horizontalMotor.getCurrentPosition() >= HORIZONTAL_EXTEND_DONE) {
            slide_horizontalMotor.setPower(0);
            // Placeholder: Add logic to determine next state based on sample
            // identification (e.g., using a sensor or gamepad input)
            currentState = RobotState.SAMPLE_OUTAKE; // Default transition
        }
    }

    private void handleSampleOutakeState() {
        // Actions: Vertical slide positions to grab sample. Arm swings (B), arm claw closes, horizontal claw opens.
        // Vertical slide maxes. Arm swings (A).
        slide_verticalMotor.setPower(SLIDE_SPEED); // Move vertical slide (adjust power)
        armServo.setPosition(0.1); // Swing Arm (B)

        if (gamepad2.b) {
            arm_clawServo.setPosition(ARM_CLAW_SERVO_CLOSE); // Close arm claw
            claw.setPosition(0); // Open horizontal claw
        }
        //when the vertical slide has reach its limit and the b button is clicked
        if (slide_verticalMotor.getCurrentPosition() >= 1000 && gamepad2.b) {
            armServo.setPosition(1); // Swing Arm (A)
            slide_verticalMotor.setPower(0);
            currentState = RobotState.IDLE;
        }
        // Transition: Gamepad2.B -> A -> IDLE
        // Human player determines release (handled manually)
        // Transition to IDLE handled when ready
    }

    private void handleSpecimenThrowingState() {
        // Actions: Horizontal slide extends. Wrist flicks + claw release.
        slide_horizontalMotor.setPower(SLIDE_SPEED); // Extend
        rdServo.setPosition(1); // Flick Wrist (adjust values)
        ldServo.setPosition(0);
        claw.setPosition(0); // Release Claw

        // Transition: Auto after specimen selection -> Specimen Intake, Intake Ready, or IDLE
        // Placeholder: Add logic to transition based on game conditions
        currentState = RobotState.SPECIMEN_INTAKE; // Default transition
    }

    private void handleSpecimenIntakeState() {
        // Actions: Vertical slide goes up to its encoder position. Arm goes to its preset position (gamepad D or armServo.setPosition(.5);).
        // Then the arm claw goes to its preset open position.
        slide_verticalMotor.setPower(SLIDE_SPEED); // Move up
        armServo.setPosition(ARM_SERVO_INTAKE); // Preset arm position
        arm_clawServo.setPosition(ARM_CLAW_SERVO_OPEN); // Open arm claw

        // Transition: Gamepad2.D -> Specimen Outake
        if (gamepad2.right_trigger == 1) {
            slide_verticalMotor.setPower(0);
            currentState = RobotState.SPECIMEN_OUTAKE;
        }
    }

    private void handleSpecimenOutakeState() {
        // Actions: Human-triggered claw close -> slide ascends -> arm position 2 (right trigger, or armServo.setPosition(0)).
        // Manual slide extension until clip. Human-controlled release.
        if (gamepad2.right_trigger > 0.5) { // Human trigger
            arm_clawServo.setPosition(ARM_CLAW_SERVO_CLOSE); // Close claw
            slide_verticalMotor.setPower(SLIDE_SPEED); // Ascend

            // Move arm to throwing position
            armServo.setPosition(ARM_SERVO_THROW);
        }

        // Transition: Gamepad2.right_trigger + button -> IDLE
        // Manual control for slide extension and release handled by human
        if (gamepad2.left_bumper) { // Example button for release
            slide_verticalMotor.setPower(0);
            currentState = RobotState.IDLE;
        }
    }

    // -------------------- HELPER METHODS --------------------

    private void driveTrainControl() {
        double drive = gamepad1.left_stick_y;
        double strafe = gamepad1.left_stick_x;
        double rotate = gamepad1.right_stick_x;

        double frontLeftPower = (drive + strafe + rotate);
        double backLeftPower = (drive - strafe + rotate);
        double frontRightPower = (drive - strafe - rotate);
        double backRightPower = (drive + strafe - rotate);

        double max = Math.max(Math.abs(frontLeftPower), Math.max(Math.abs(backLeftPower), Math.max(Math.abs(frontRightPower), Math.abs(backRightPower))));
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

    private void handleHanging() {
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
    }

    private void adjustWristPosition() {
        double servoStep = 0.1;
        double rdPos = rdServo.getPosition();
        double ldPos = ldServo.getPosition();

        if (gamepad2.left_bumper) {
            rdPos += servoStep;
            ldPos -= servoStep;
        } else if (gamepad2.right_bumper) {
            rdPos -= servoStep;
            ldPos += servoStep;
        }

        rdServo.setPosition(Math.max(0, Math.min(1, rdPos)));
        ldServo.setPosition(Math.max(0, Math.min(1, ldPos)));
    }
}

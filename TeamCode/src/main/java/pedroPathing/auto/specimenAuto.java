package pedroPathing.auto;

// Step One: Imports - include all necessary PedroPathing and FTC imports.
import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.Path;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;
import com.pedropathing.util.Timer;
import com.pedropathing.util.Constants;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;
import pedroPathing.examples.GeneratedPaths; // Use your existing GeneratedPaths class

/**
 * specimenAuto demonstrates a PedroPathing autonomous routine that uses the
 * pre-generated path from GeneratedPaths (line1) to move the robot straight to the right.
 * The program executes the path and then ends the autonomous routine.
 */
@Autonomous(name = "Blue Auto", group = "Examples")
public class specimenAuto extends OpMode {

    // Step Two: Pose Initialization
    // Starting at the first point from GeneratedPaths (8.000, 45.000) with a 0 radian heading.
    private final Pose startPose = new Pose(8.000, 45.000, Math.toRadians(0));

    // Timers and state variable for managing autonomous progress.
    private Timer pathTimer, opmodeTimer;
    private int pathState;

    // The Follower object handles path following.
    private Follower follower;

    // Step Three: Path Initialization
    // Since we are using paths from GeneratedPaths, no additional path building is required.
    public void buildPaths() {
        // No local path building needed.
    }

    // Step Four: Managing Path States
    // The state machine commands the Follower to execute the path and waits for completion.
    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                // Begin by following the pre-generated path (line1) from GeneratedPaths.
                follower.followPath(GeneratedPaths.line1);
                // Advance the state to wait for path completion.
                setPathState(1);
                break;
            case 1:
                // When the follower is no longer busy (path is complete), end the autonomous routine.
                if (!follower.isBusy()) {
                    setPathState(-1); // End of autonomous.
                }
                break;
            default:
                // Do nothing for state -1 or unhandled states.
                break;
        }
    }

    // Helper method for updating the path state and resetting the timer.
    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }

    // Step Five: Initialization Method
    @Override
    public void init() {
        pathTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();

        // Set robot constants for Follower and Localizer.
        Constants.setConstants(FConstants.class, LConstants.class);

        // Initialize the Follower with the hardware map and constant classes.
        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        // Set the starting pose.
        follower.setStartingPose(startPose);
        // Build paths (using GeneratedPaths, no local path definitions required).
        buildPaths();
    }

    @Override
    public void init_loop() {
        // Optional: add telemetry or other initialization tasks.
    }

    @Override
    public void start() {
        opmodeTimer.resetTimer();
        setPathState(0); // Begin with state 0.
    }

    @Override
    public void loop() {
        // Update the follower and the autonomous path state.
        follower.update();
        autonomousPathUpdate();

        // Telemetry output for debugging.
        telemetry.addData("Path State", pathState);
        telemetry.addData("Position", follower.getPose().toString());
        telemetry.update();
    }

    @Override
    public void stop() {
        // Optional: add cleanup or stop actions if needed.
    }
}

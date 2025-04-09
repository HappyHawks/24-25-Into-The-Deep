package pedroPathing.examples;

import com.pedropathing.pathgen.BezierCurve;
import com.pedropathing.pathgen.BezierLine;
import com.pedropathing.pathgen.PathBuilder;
import com.pedropathing.pathgen.PathChain;
import com.pedropathing.pathgen.Point;

/**
 * GeneratedPaths defines six path segments based on your provided points.
 * Each path segment is built with its own PathBuilder instance.
 */
public class GeneratedPaths {

    public static PathChain line1 = new PathBuilder()
            .addPath(
                    new BezierLine(
                            new Point(8.000, 80.000, Point.CARTESIAN),
                            new Point(7.591, 24.672, Point.CARTESIAN)
                    )
            )
            .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(0))
            .build();

}

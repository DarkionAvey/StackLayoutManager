package library.stacklayoutmanager.extras.layoutinterpolators;


import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Build;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows the path to end at points farther than 1
 * while the stock PathInterpolator doesn't. Therefore, you can
 * create more sophisticated layout interpolators using this class
 * Originally developed for SystemUI stack view
 * <p>
 * Since path approximate is only public on Oreo+, we use a backport
 * courtesy of alexjlockwood
 */
public class FreePathInterpolator implements android.view.animation.Interpolator {
    // This governs how accurate the approximation of the Path is.
    private static final float PRECISION = 0.002f;
    private float[] mX;
    private float[] mY;
    private float mArcLength;
    /**
     * Create an interpolator for an arbitrary <code>Path</code>.
     *
     * @param path The <code>Path</code> to use to make the line representing the interpolator.
     */
    public FreePathInterpolator(Path path) {
        initPath(path);
    }

    private float[] approximate(Path p, float PRECISION) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return p.approximate(PRECISION);
        } else
            return PathCompat.approximate(p, PRECISION);
    }

    private void initPath(Path path) {
        float[] pointComponents = approximate(path, PRECISION);

        int numPoints = pointComponents.length / 3;

        mX = new float[numPoints];
        mY = new float[numPoints];
        mArcLength = 0;
        float prevX = 0;
        float prevY = 0;
        float prevFraction = 0;
        int componentIndex = 0;
        for (int i = 0; i < numPoints; i++) {
            float fraction = pointComponents[componentIndex++];
            float x = pointComponents[componentIndex++];
            float y = pointComponents[componentIndex++];
            if (fraction == prevFraction && x != prevX) {
                throw new IllegalArgumentException(
                        "The Path cannot have discontinuity in the X axis.");
            }
            if (x < prevX) {
                throw new IllegalArgumentException("The Path cannot loop back on itself.");
            }
            mX[i] = x;
            mY[i] = y;
            mArcLength += Math.hypot(x - prevX, y - prevY);
            prevX = x;
            prevY = y;
            prevFraction = fraction;
        }
    }

    /**
     * Using the line in the Path in this interpolator that can be described as
     * <code>y = f(x)</code>, finds the y coordinate of the line given <code>t</code>
     * as the x coordinate.
     *
     * @param t Treated as the x coordinate along the line.
     * @return The y coordinate of the Path along the line where x = <code>t</code>.
     */
    @Override
    public float getInterpolation(float t) {
        int startIndex = 0;
        int endIndex = mX.length - 1;

        // Return early if out of bounds
        if (t <= 0) {
            return mY[startIndex];
        } else if (t >= 1) {
            return mY[endIndex];
        }

        // Do a binary search for the correct x to interpolate between.
        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (t < mX[midIndex]) {
                endIndex = midIndex;
            } else {
                startIndex = midIndex;
            }
        }

        float xRange = mX[endIndex] - mX[startIndex];
        if (xRange == 0) {
            return mY[startIndex];
        }

        float tInRange = t - mX[startIndex];
        float fraction = tInRange / xRange;

        float startY = mY[startIndex];
        float endY = mY[endIndex];
        return startY + (fraction * (endY - startY));
    }

    /**
     * Finds the x that provides the given <code>y = f(x)</code>.
     *
     * @param y a value from (0,1) that is in this path.
     */
    public float getX(float y) {
        int startIndex = 0;
        int endIndex = mY.length - 1;

        // Return early if out of bounds
        if (y <= 0) {
            return mX[endIndex];
        } else if (y >= 1) {
            return mX[startIndex];
        }

        // Do a binary search for index that bounds the y
        while (endIndex - startIndex > 1) {
            int midIndex = (startIndex + endIndex) / 2;
            if (y < mY[midIndex]) {
                startIndex = midIndex;
            } else {
                endIndex = midIndex;
            }
        }

        float yRange = mY[endIndex] - mY[startIndex];
        if (yRange == 0) {
            return mX[startIndex];
        }

        float tInRange = y - mY[startIndex];
        float fraction = tInRange / yRange;

        float startX = mX[startIndex];
        float endX = mX[endIndex];
        return startX + (fraction * (endX - startX));
    }

    /**
     * Returns the arclength of the path we are interpolating.
     */
    public float getArcLength() {
        return mArcLength;
    }

    //https://gist.github.com/alexjlockwood/7d3685fe9ce7dcfde33112c4e6c5ce4f
    private static final class PathCompat {
        private static final int MAX_NUM_POINTS = 100;
        private static final int FRACTION_OFFSET = 0;
        private static final int X_OFFSET = 1;
        private static final int Y_OFFSET = 2;
        private static final int NUM_COMPONENTS = 3;

        private PathCompat() {
        }

        /**
         * Approximate the <code>Path</code> with a series of line segments.
         * This returns float[] with the array containing point components.
         * There are three components for each point, in order:
         * <ul>
         * <li>Fraction along the length of the path that the point resides</li>
         * <li>The x coordinate of the point</li>
         * <li>The y coordinate of the point</li>
         * </ul>
         * <p>Two points may share the same fraction along its length when there is
         * a move action within the Path.</p>
         *
         * @param acceptableError The acceptable error for a line on the
         *                        Path. Typically this would be 0.5 so that
         *                        the error is less than half a pixel.
         * @return An array of components for points approximating the Path.
         */
        @NonNull
        public static float[] approximate(@NonNull Path path, @FloatRange(from = 0f) float acceptableError) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return path.approximate(acceptableError);
            }
            if (acceptableError < 0) {
                throw new IllegalArgumentException("acceptableError must be greater than or equal to 0");
            }
            // Measure the total length the whole pathData.
            final PathMeasure measureForTotalLength = new PathMeasure(path, false);
            float totalLength = 0;
            // The sum of the previous contour plus the current one. Using the sum here
            // because we want to directly subtract from it later.
            final List<Float> summedContourLengths = new ArrayList<>();
            summedContourLengths.add(0f);
            do {
                final float pathLength = measureForTotalLength.getLength();
                totalLength += pathLength;
                summedContourLengths.add(totalLength);
            } while (measureForTotalLength.nextContour());

            // Now determine how many sample points we need, and the step for next sample.
            final PathMeasure pathMeasure = new PathMeasure(path, false);

            final int numPoints = Math.min(MAX_NUM_POINTS, (int) (totalLength / acceptableError) + 1);

            final float[] coords = new float[NUM_COMPONENTS * numPoints];
            final float[] position = new float[2];

            int contourIndex = 0;
            final float step = totalLength / (numPoints - 1);
            float cumulativeDistance = 0;

            // For each sample point, determine whether we need to move on to next contour.
            // After we find the right contour, then sample it using the current distance value minus
            // the previously sampled contours' total length.
            for (int i = 0; i < numPoints; i++) {
                // The cumulative distance traveled minus the total length of the previous contours
                // (not including the current contour).
                final float contourDistance = cumulativeDistance - summedContourLengths.get(contourIndex);
                pathMeasure.getPosTan(contourDistance, position, null);

                coords[i * NUM_COMPONENTS + FRACTION_OFFSET] = cumulativeDistance / totalLength;
                coords[i * NUM_COMPONENTS + X_OFFSET] = position[0];
                coords[i * NUM_COMPONENTS + Y_OFFSET] = position[1];

                cumulativeDistance = Math.min(cumulativeDistance + step, totalLength);

                // Using a while statement is necessary in the rare case where step is greater than
                // the length a path contour.
                while (summedContourLengths.get(contourIndex + 1) < cumulativeDistance) {
                    contourIndex++;
                    pathMeasure.nextContour();
                }
            }

            coords[(numPoints - 1) * NUM_COMPONENTS + FRACTION_OFFSET] = 1f;
            return coords;
        }
    }
}
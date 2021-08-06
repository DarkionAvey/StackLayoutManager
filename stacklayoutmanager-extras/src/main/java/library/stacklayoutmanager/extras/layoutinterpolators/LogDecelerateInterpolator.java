package library.stacklayoutmanager.extras.layoutinterpolators;

import android.animation.TimeInterpolator;

/**
 * Standard ease out interpolator from Launcher3 package
 * <p>
 * Preview: https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/gifs/log_decelerate_60_interpolator.webp
 */
public class LogDecelerateInterpolator implements TimeInterpolator {
    private final float mBase;
    private final float mDrift;
    private final float mTimeScale;
    private final float mOutputScale;

    public LogDecelerateInterpolator(float base, int drift) {
        mBase = base;
        mDrift = drift;
        mTimeScale = 1f;
        mOutputScale = 1f / computeLog(1f);
    }

    private float computeLog(float t) {
        return 1f - (float) Math.pow(mBase, -t * mTimeScale) + (mDrift * t);
    }

    @Override
    public float getInterpolation(float t) {
        return computeLog(t) * mOutputScale;
    }
}

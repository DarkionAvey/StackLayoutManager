package net.darkion.stacklayoutmanager.demo.layoutinterpolators;


import android.animation.TimeInterpolator;

public class LogAccelerateInterpolator implements TimeInterpolator {
    int mBase;
    int mDrift;
    final float mLogScale;
    public LogAccelerateInterpolator(int base, int drift) {
        mBase = base;
        mDrift = drift;
        mLogScale = 1f / computeLog(1, mBase, mDrift);
    }
    static float computeLog(float t, int base, int drift) {
        return (float) -Math.pow(base, -t) + 1 + (drift * t);
    }
    @Override
    public float getInterpolation(float t) {
        return Float.compare(t, 1f) == 0 ? 1f : 1 - computeLog(1 - t, mBase, mDrift) * mLogScale;
    }
}
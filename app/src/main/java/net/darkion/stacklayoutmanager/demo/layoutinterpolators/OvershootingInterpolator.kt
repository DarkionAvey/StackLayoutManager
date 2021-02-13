package net.darkion.stacklayoutmanager.demo.layoutinterpolators

import android.graphics.Path

/**
 * Similar to OvershootInterpolator but this implementation
 * makes sure that the view stays inside the screen when
 * the interpolation reaches 1
 * and
 */
class OvershootingInterpolator : FreePathInterpolator(Path().apply {
    //y is -1 to make sure that that the view doesn't remain
    // static at the bottom of the screen when vertical mode is activated
    // it basically means the view is displaced 1x its length offscreen
    moveTo(0f, -1f)
    //y = 1.05f means the overshoot should be 0.05f of the view's length
    quadTo(0.4f, 0.5f, 0.7f, 1.05f)
    lineTo(1f, 1f)
})
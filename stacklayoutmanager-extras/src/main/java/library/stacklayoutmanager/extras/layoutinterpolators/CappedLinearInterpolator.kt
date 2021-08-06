package library.stacklayoutmanager.extras.layoutinterpolators

import android.view.animation.LinearInterpolator

/**
 * Just like with OvershootingInterpolator, this interpolator doesn't allow
 * the view to go offscreen once interpolation has reached the end (aka clamp value)
 *
 * Preview: https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/gifs/capped_linear_interpolator.webp
 */
class CappedLinearInterpolator : LinearInterpolator() {
    override fun getInterpolation(t: Float): Float {
        return super.getInterpolation(kotlin.math.min(1f, t))
    }
}
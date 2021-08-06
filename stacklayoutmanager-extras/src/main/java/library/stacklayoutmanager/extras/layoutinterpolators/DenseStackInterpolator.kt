package library.stacklayoutmanager.extras.layoutinterpolators

import android.graphics.Path

/**
 * This interpolator recreates the original behaviour that was intended by Google in their TaskStackLayout
 * @param stackedViews: the number of views shown at once.
 *                      3f means we want to have three views shown inside the recyclerview
 * Preview: https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/gifs/dense_interpolator.webp
 */
class DenseStackInterpolator(private val stackedViews: Float = 3f) :
    FreePathInterpolator(layoutPath) {
    override fun getInterpolation(t: Float): Float {
        return super.getInterpolation(t / stackedViews)
    }

    companion object {
        private val layoutPath: Path by lazy {
            Path().apply {
                /*
                    simple interpretation of this is:
                    when x equals to [value], y should be [value]
                    where y=1 --> view is at its final position
                    y = 0  --> offscreen, with 0 displacement
                    y = -0.5 --> offscreen, with displacement equals to (1f-(-0.5f)) of the view length
                    which means 1.5f * view width or height depending on the current mode
                    similarly, y = -1.5 --> the view is located offscreen, with displacement
                    that is equal to 2.5x its length
                     */
                moveTo(0f, -0.5f)
                quadTo(0.1f, 0.5f, 0.2f, 0.95f)
                quadTo(0.2f, 1f, 1f, 1.03f)
            }
        }
    }
}
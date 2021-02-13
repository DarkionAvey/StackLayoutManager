package net.darkion.stacklayoutmanager.demo.layoutinterpolators

import android.animation.TimeInterpolator
import android.graphics.Path


//This interpolator emulates the original behaviour created by Google in their
//TaskStackLayout
class DenseStackInterpolator : TimeInterpolator {
    private val pathInterpolator = getLayoutPath()

    //3f means we want to have three views shown inside the recyclerview
    private val stackedViews = 3f

    private fun getLayoutPath(): FreePathInterpolator {
        //simple interpretation of this is:
        //when x equals to [value], y should be [value]
        //where y=1 --> view is at its final position
        //y = 0  --> offscreen, with 0 displacement
        //y = -0.5 --> offscreen, with displacement equals to (1f-(-0.5f)) of the view length
        //which means 1.5f * view width or height depending on the current mode
        //similarly, y = -1.5 --> the view is located offscreen, with displacement
        //that is equal to 2.5x its length

        val p = Path()
        p.moveTo(0f, -0.5f)
        p.quadTo(0.1f, 0.5f, 0.2f, 0.95f)
        p.quadTo(0.2f, 1f, 1f, 1.03f)

        return FreePathInterpolator(p)
    }

    override fun getInterpolation(input: Float): Float {
        return pathInterpolator.getInterpolation(input / stackedViews)
    }
}
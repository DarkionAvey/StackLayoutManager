package net.darkion.stacklayoutmanager.demo.transformers

import android.graphics.Path
import android.view.View
import net.darkion.stacklayoutmanager.demo.layoutinterpolators.FreePathInterpolator
import net.darkion.stacklayoutmanager.library.StackLayoutManager

object RotationTransformer {
    private val scalePath =
        FreePathInterpolator(
            Path().apply {
                moveTo(0f, 0.4f)
                lineTo(0.6f, 1f)
                lineTo(1f, 1f)
            })

    private val rotationPath =
        FreePathInterpolator(
            Path().apply {
                moveTo(0f, 0f)
                //stop rotation at 50%
                lineTo(0.6f, 1f)
                lineTo(1f, 1f)
            })

    fun transform(x: Float, v: View, stackLayoutManager: StackLayoutManager) {
        v.translationZ = 0f

        val scale = scalePath.getInterpolation(1f - x)
        val rotation = 1f - rotationPath.getInterpolation(1f - x)
        v.pivotY = v.height.toFloat() / 2f
        v.pivotX = v.width.toFloat() / 2f

        //using rotation to reduce scale
        //ensures that all of the view remains
        //inside the screen while transforming
        v.scaleX = scale * (1f - rotation)
        v.scaleY = scale * (1f - rotation)


        if (stackLayoutManager.horizontalLayout)
            v.rotationY = rotation * 90f
        else v.rotationX = -rotation * 90f
    }
}
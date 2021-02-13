package net.darkion.stacklayoutmanager.demo.transformers

import android.graphics.Path
import android.view.View
import net.darkion.stacklayoutmanager.demo.layoutinterpolators.FreePathInterpolator
import net.darkion.stacklayoutmanager.library.StackLayoutManager

object ScaleTransformer {
    private val scalePath =
        FreePathInterpolator(
            Path().apply {
                //0.7f is the minimum scale
                moveTo(0f, 0.7f)
                lineTo(1f, 1f)
            })

    fun transform(x: Float, v: View, stackLayoutManager: StackLayoutManager) {
        StackLayoutManager.ElevationTransformer.transform(x, v, stackLayoutManager)
        val scale = if (x == 0f) 1f else scalePath.getInterpolation(1f - kotlin.math.abs(x))
        v.scaleX = scale
        v.scaleY = scale
    }
}
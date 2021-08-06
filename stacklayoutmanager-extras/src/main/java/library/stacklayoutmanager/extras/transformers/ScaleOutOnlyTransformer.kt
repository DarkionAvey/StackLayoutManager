package library.stacklayoutmanager.extras.transformers

import android.graphics.Path
import android.view.View
import library.StackLayoutManager
import library.stacklayoutmanager.extras.layoutinterpolators.FreePathInterpolator

/**
 * This transformer gives the same effect as the Shazam android app (an old version, global chart cards)
 *
 * Preview: https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/gifs/scale_out_transform.webp
 */
object ScaleOutOnlyTransformer {
    private val scalePath =
        FreePathInterpolator(
            Path().apply {
                //0.7f is the minimum scale
                moveTo(0f, 0.7f)
                lineTo(1f, 1f)
            })

    fun transform(x: Float, v: View, stackLayoutManager: StackLayoutManager) {
        StackLayoutManager.ElevationTransformer.transform(x, v, stackLayoutManager)
        val scale = if (x > 0f) 1f else scalePath.getInterpolation(1f - kotlin.math.abs(x))
        v.scaleX = scale
        v.scaleY = scale
    }
}
package net.darkion.stacklayoutmanager.demo.layoutinterpolators

import android.content.res.Resources
import android.graphics.Path
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import net.darkion.stacklayoutmanager.library.StackLayoutManager
import kotlin.math.abs

class ReverseStackInterpolator : LinearInterpolator() {
    val path = FreePathInterpolator(Path().apply {
        moveTo(0f, 0.9f)
        lineTo(1f, 0.95f)
        lineTo(2f, 3f)
    })
    val path2 = FreePathInterpolator(Path().apply {
        moveTo(0f, 1f)
        lineTo(1f, 2f)
    })
    val logDecelerateInterpolator = LogAccelerateInterpolator(40, 0)

    object Transformer {
        private val maxTranslationZ by lazy {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20f,
                Resources.getSystem().displayMetrics
            )
        }

        fun transform(x: Float, v: View, stackLayoutManager: StackLayoutManager) {
            v.elevation = maxTranslationZ
            v.translationZ = (1f-x) * 2f
        }
    }

    private fun mapRange(value: Float, min: Float, max: Float): Float {
        return min + value * (max - min)
    }

    override fun getInterpolation(input: Float): Float {
//        var input = input
//        val x = 1f - input
        if (input < 0f) return path.getInterpolation(1f - abs(input))
//        //  if (x > 0f) return 1f
//        return 1f + input

        if (input < 1f) return mapRange(input,0.95f,1f)
        return input
    }
}
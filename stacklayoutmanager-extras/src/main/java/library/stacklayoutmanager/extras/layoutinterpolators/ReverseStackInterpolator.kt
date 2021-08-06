package library.stacklayoutmanager.extras.layoutinterpolators

import android.content.res.Resources
import android.graphics.Path
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import library.StackLayoutManager
import kotlin.math.max

/**
 * This interpolator draws views in reverse
 *
 * Preview: https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/gifs/reverse_interpolator.webp
 */

class ReverseStackInterpolator : LinearInterpolator() {
    private val path = FreePathInterpolator(Path().apply {
        moveTo(0f, 0.9f)
        lineTo(1f, 0.95f)
        lineTo(2f, 3f)
    })

    object ReverseStackTransformer {
        private val maxTranslationZ by lazy {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20f,
                Resources.getSystem().displayMetrics
            )
        }

        fun transform(x: Float, v: View, stackLayoutManager: StackLayoutManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                v.elevation = maxTranslationZ
                v.translationZ = (1f - x) * 2f
            }
        }
    }

    override fun getInterpolation(input: Float): Float {
        return max((0.95f + input * 0.05f).coerceAtLeast(0.95f), input)
    }
}
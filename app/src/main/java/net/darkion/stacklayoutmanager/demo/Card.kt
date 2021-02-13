package net.darkion.stacklayoutmanager.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewOutlineProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import net.darkion.stacklayoutmanager.library.StackLayoutManager


//this view demonstrates how to use the
//dimming value supplied by StackLayoutParams
//(see 'override fun draw' method)
class Card @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            if (MainActivity.squareItems) widthMeasureSpec else heightMeasureSpec
        )
    }

    //we use draw instead of onDraw to
    //draw over the image view which is a
    //child of this ViewGroup
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        canvas.drawColor(
            ColorUtils.setAlphaComponent(
                Color.BLACK,
                ((layoutParams as StackLayoutManager.StackLayoutParams).dimAmount * 255).toInt()
            )
        )
    }
}
package net.darkion.stacklayoutmanager.demo

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils

class DoubleTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val strokeWidth = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        10f,
        Resources.getSystem().displayMetrics
    )

    override fun onDraw(canvas: Canvas?) {
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = strokeWidth
        setTextColor(ColorUtils.setAlphaComponent(Color.BLACK, 80))
        super.onDraw(canvas)

        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        setTextColor(Color.WHITE)
        super.onDraw(canvas)
    }

}
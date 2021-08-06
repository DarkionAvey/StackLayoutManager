package library

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * A lightweight and highly-customizable, interpolator-based layout manager for RecyclerView
 * visit https://github.com/DarkionAvey/StackLayoutManager for more info
 */
class StackLayoutManager(
    //toggle between horizontal and vertical modes
    horizontalLayout: Boolean = true,
    //should first item be centered?
    val centerFirstItem: Boolean = true,
    //how many items should be scrolled in one edge-to-edge swipe
    val scrollMultiplier: Float = 1.2f,
    // the max number of children the recyclerview should have
    val maxViews: Int = 6,
    //interpolator for laying out views
    layoutInterpolator: TimeInterpolator = Internal.LAYOUT_INTERPOLATOR,
    //supply a higher-order function to receive ViewTransformation object
    //for custom view transformations.
    //x represents the raw x value that needs to be interpolated into y
    viewTransformer: ((x: Float, view: View, stackLayoutManager: StackLayoutManager) -> Unit?)? = ElevationTransformer::transform

) : RecyclerView.LayoutManager() {

    var viewTransformer: ((x: Float, view: View, stackLayoutManager: StackLayoutManager) -> Unit?)? =
        viewTransformer
        set(value) {
            //due to the dynamic way view transformation works
            //the caller is responsible for resetting the views
            //to their original, pre-transformation state
            requestSimpleAnimationsInNextLayout()
            field = value
            requestLayout()
        }
    var layoutInterpolator = layoutInterpolator
        set(value) {
            field = value
            requestSimpleAnimationsInNextLayout()
            requestLayout()
        }
    private val stopScrollingRunnable by lazy { Runnable { stopScrolling() } }
    private var displayRect = Rect()
    private val viewRect = Rect()
    private val marginsRect = Rect()
    private val tmpRect = Rect()
    private var scrollAnimator: ValueAnimator? = null
    private val stackAlgorithm: LayoutAlgorithm = LayoutAlgorithm()
    private val scroller: StackScroller = StackScroller()
    private var decoratedChildHeight = -1
    private val firstItemPosition: Int
        get() = max(
            0,
            min(itemCount - maxViews, currentItem - maxViews / 2)
        )
    private val lastVisibleItemPosition: Int
        get() = if (firstItemPosition == 0) min(
            maxViews,
            itemCount
        ) else min(itemCount, currentItem + maxViews / 2)

    var horizontalLayout: Boolean = horizontalLayout
        set(value) {
            if (field == value) return
            field = value
            requestSimpleAnimationsInNextLayout()
            displayRect.setEmpty()
            requestLayout()
        }

    //return how far has the recyclerview been scrolled relative to its length
    val relativeScroll: Float
        get() = scroller.stackScroll

    //return how far has the recyclerview been scrolled in pixels
    val absoluteScroll: Float
        get() = stackAlgorithm.getYForDeltaP(scroller.stackScroll).toFloat()

    //return currently focused item
    val currentItem: Int
        get() = min(
            max(
                0,
                kotlin.math.floor(
                    scroller.stackScroll.toDouble()
                ).toInt()
            ), itemCount
        )

    override fun isSmoothScrolling(): Boolean {
        return super.isSmoothScrolling() || scrollAnimator != null && scrollAnimator!!.isRunning
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: Recycler,
        state: RecyclerView.State
    ): Int {
        return scroll(dy.toFloat(), recycler, state)
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: Recycler,
        state: RecyclerView.State
    ): Int {
        return scroll(dx.toFloat(), recycler, state)
    }

    override fun canScrollVertically(): Boolean {
        return itemCount != 0 && !horizontalLayout
    }

    override fun canScrollHorizontally(): Boolean {
        return itemCount != 0 && horizontalLayout
    }

    private fun scroll(
        thisMuch: Float,
        recycler: Recycler,
        state: RecyclerView.State
    ): Int {
        var delta = thisMuch
        stopScrolling()

        if (childCount == 0) {
            return 0
        }
        val deltaP = stackAlgorithm.getDeltaPForY(delta)
        var scrollCurrent = scroller.stackScroll + deltaP
        if (scrollCurrent < 0) {
            scrollCurrent = 0f
            delta = 0f
        } else if (scrollCurrent >= itemCount - 1) {
            scrollCurrent = itemCount - 1.toFloat()
            delta = 0f
        }
        scroller.stackScroll = scrollCurrent
        onLayoutChildren(recycler, state)
        return delta.toInt()
    }

    override fun scrollToPosition(position: Int) {
        scrollToPosition(position.toFloat(), true, null)
    }

    fun scrollToPosition(
        position: Float,
        animated: Boolean = true,
        duration: Long? = null,
        endRunnable: Runnable? = null
    ) {
        animateScrollToItem(position, endRunnable)
            .also {
                if (animated.not())
                    it.duration = 0
                else if (duration != null)
                    it.duration = duration
            }
            .start()
    }

    private fun animateScrollToItem(
        toItem: Float,
        postRunnable: Runnable?
    ): ValueAnimator {
        stopScrolling()
        return ValueAnimator.ofFloat(scroller.stackScroll, toItem).apply {
            addUpdateListener { animation ->
                scroller.stackScroll = animation.animatedValue as Float
                requestLayout()
            }
            if (postRunnable != null)
                addListener(object :
                    AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        postRunnable.run()
                    }
                })
            interpolator = Internal.SCROLL_INTERPOLATOR
            duration = (150 * kotlin.math.abs(toItem - scroller.stackScroll)).roundToInt()
                .toLong()
        }.also { animator ->
            scrollAnimator = animator
        }
    }

    override fun onMeasure(
        recycler: Recycler,
        state: RecyclerView.State,
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        super.onMeasure(recycler, state, widthMeasureSpec, heightMeasureSpec)
        if (state.itemCount <= 0) return
        setMeasuredDimension(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
    }

    override fun onLayoutChildren(
        recycler: Recycler,
        state: RecyclerView.State
    ) {
        if (state.itemCount <= 0) {
            removeAndRecycleAllViews(recycler)
            return
        }

        if (updateDisplayRect()) {
            val v = recycler.getViewForPosition(0)
            addDisappearingView(v)
            measureChildWithMargins(v, 0, 0)
            val childWidth = v.measuredWidth
            decoratedChildHeight = v.measuredHeight
            val left: Int
            val top: Int
            val right: Int
            val bottom: Int
            val additionalPadding = 0
            left =
                additionalPadding + paddingStart + kotlin.math.abs(displayRect.width() - childWidth) / 2
            top = additionalPadding + paddingTop + (displayRect.height() - decoratedChildHeight) / 2
            right = childWidth - paddingEnd - additionalPadding
            bottom = decoratedChildHeight - additionalPadding
            viewRect.setEmpty()
            viewRect[left, top, left + right] = top + bottom
            if (v.right == 0) {
                v.left = 0
                v.right = childWidth
            }
            if (v.bottom == 0) {
                v.top = 0
                v.bottom = decoratedChildHeight
            }
            getDecoratedBoundsWithMargins(v, marginsRect)
            removeAndRecycleView(v, recycler)
        }
        stackAlgorithm.update(lastVisibleItemPosition)
        bindVisibleViews(recycler, state)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        removeAllViews()
    }

    fun forceRemeasureInNextLayout() {
        displayRect.setEmpty()
        viewRect.setEmpty()
    }

    private fun updateDisplayRect(): Boolean {
        if (displayRect.width() == 0 ||
            displayRect.height() == 0 ||
            displayRect.width() != width - paddingEnd ||
            displayRect.height() != height - paddingBottom
        ) {
            displayRect.left = paddingStart
            displayRect.top = paddingTop
            displayRect.right = width - paddingEnd
            displayRect.bottom = height - paddingBottom
            return true
        }
        return false
    }

    private fun bindVisibleViews(recycler: Recycler, state: RecyclerView.State) {
        val currentLowerLimit = firstItemPosition
        val currentUpperLimit = lastVisibleItemPosition
        if (scroller.isScrollOutOfBounds) {
            scroller.boundScroll()
        }
        for (i in childCount - 1 downTo 0) {
            val tv = getChildAt(i) ?: continue
            detachAndScrapView(tv, recycler)
        }

        for (i in currentLowerLimit until currentUpperLimit) {
            if (i >= state.itemCount) continue
            val v = createView(i, recycler, state)
            if (v != null) {
                v.bringToFront()
                stackAlgorithm.transform(
                    i.toFloat(),
                    scroller.stackScroll,
                    v
                )
            }
        }

    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
        return lp is StackLayoutParams
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return StackLayoutParams(lp)
    }

    override fun generateLayoutParams(
        c: Context,
        attrs: AttributeSet
    ): RecyclerView.LayoutParams {
        return StackLayoutParams(c, attrs)
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return StackLayoutParams(-2, -2)
    }

    class StackLayoutParams : RecyclerView.LayoutParams {
        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(c: Context?, attrs: AttributeSet?) :
                super(c, attrs)

        var dimAmount: Float = 0f
    }

    internal inner class LayoutAlgorithm {
        var mMinScrollP = 0f
        var mMaxScrollP = 0f
        private var mInitialScrollP = 0f

        fun update(upperLimit: Int) {
            if (upperLimit == 0) {
                mInitialScrollP = 0f
                mMaxScrollP = mInitialScrollP
                mMinScrollP = mMaxScrollP
                return
            }
            val launchIndex = upperLimit - 1
            mMinScrollP = 0f
            mMaxScrollP = max(
                mMinScrollP, upperLimit - 1f
            )
            mInitialScrollP = Internal.clamp(
                launchIndex.toFloat(),
                mMinScrollP,
                mMaxScrollP
            )
        }


        private fun getLength(rect: Rect): Int {
            return if (!horizontalLayout) {
                rect.height()
            } else rect.width()
        }

        fun transform(
            position: Float,
            scroll: Float,
            view: View
        ) {
            tmpRect.setEmpty()

            //even though we should clamp the value between 0 and 1
            //it is better to leave it for the interpolator to handle
            //overshooting values
            val interpolatedValue =
                1f - layoutInterpolator.getInterpolation(
                    1f - (position - scroll)
                )


            val displacementFloat =
                interpolatedValue * getLength(marginsRect) * (
                        if (position < 1f && !centerFirstItem)
                            getLength(marginsRect).toFloat()
                        else 1f
                        )


            var displacement = displacementFloat.toInt()

            if (centerFirstItem && position < 1f && displacement > 0)
                displacement = 0

            tmpRect.set(viewRect)

            if (!horizontalLayout) {
                tmpRect.offset(0, displacement)
            } else {
                tmpRect.offset(displacement, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                view.setLeftTopRightBottom(tmpRect.left, tmpRect.top, tmpRect.right, tmpRect.bottom)
            } else {
                view.left = tmpRect.left
                view.top = tmpRect.top
                view.right = tmpRect.right
                view.bottom = tmpRect.bottom
            }

            view.getStackLayoutParams().dimAmount =
                if (scroll <= position) 0f
                else
                    Internal.clamp01(
                        Internal.DIMMING_INTERPOLATOR.getInterpolation(scroll - position)
                    ) * 0.4f


            viewTransformer?.invoke(position - scroll, view, this@StackLayoutManager)
        }

        fun getDeltaPForY(dy: Float): Float {
            return dy / getLength(viewRect) * scrollMultiplier
        }

        fun getYForDeltaP(scroll: Float): Int {
            return (scroll * getLength(viewRect) *
                    (1f / scrollMultiplier)).toInt()
        }
    }

    object ElevationTransformer {
        private fun mapRange(value: Float, min: Float, max: Float): Float {
            return min + value * (max - min)
        }

        private val minTranslationZ by lazy {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                Resources.getSystem().displayMetrics
            )
        }
        private val maxTranslationZ by lazy {
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                10f,
                Resources.getSystem().displayMetrics
            )
        }

        fun transform(x: Float, view: View, stackLayoutManager: StackLayoutManager) {
            if (Build.VERSION.SDK_INT < 21) return
            ViewCompat.setTranslationZ(
                view, mapRange(
                    max(
                        0f,
                        min(
                            1f,
                            stackLayoutManager.layoutInterpolator.getInterpolation(x)
                        )
                    ),
                    minTranslationZ,
                    maxTranslationZ
                )
            )
        }
    }

    internal inner class StackScroller {
        var stackScroll = 0f
            internal set

        fun boundScroll() {
            val curScroll = stackScroll
            val newScroll = getBoundedStackScroll(curScroll)
            if (newScroll.compareTo(curScroll) != 0) {
                stackScroll = newScroll
            }
        }

        private fun getBoundedStackScroll(scroll: Float): Float {
            return Internal.clamp(
                scroll,
                stackAlgorithm.mMinScrollP,
                stackAlgorithm.mMaxScrollP
            )
        }

        private fun getScrollAmountOutOfBounds(scroll: Float): Float {
            if (scroll < stackAlgorithm.mMinScrollP) {
                return kotlin.math.abs(scroll - stackAlgorithm.mMinScrollP)
            } else if (scroll > stackAlgorithm.mMaxScrollP) {
                return kotlin.math.abs(scroll - stackAlgorithm.mMaxScrollP)
            }
            return 0f
        }

        val isScrollOutOfBounds: Boolean
            get() = getScrollAmountOutOfBounds(stackScroll).compareTo(0f) != 0

    }

    fun findFirstVisibleItemPosition(): Int {
        val child = findOneVisibleChild(0, childCount, false)
        return child?.let { getPosition(it) } ?: RecyclerView.NO_POSITION
    }

    fun findFirstCompletelyVisibleItemPosition(): Int {
        val child = findOneVisibleChild(0, childCount, true)
        return child?.let { getPosition(it) } ?: RecyclerView.NO_POSITION
    }

    fun findLastVisibleItemPosition(): Int {
        val child = findOneVisibleChild(childCount - 1, -1, false)
        return child?.let { getPosition(it) } ?: RecyclerView.NO_POSITION
    }

    fun findLastCompletelyVisibleItemPosition(): Int {
        val child = findOneVisibleChild(childCount - 1, -1, true)
        return child?.let { getPosition(it) } ?: RecyclerView.NO_POSITION
    }

    fun stopScrolling() {
        scrollAnimator?.also { scrollAnimator ->
            if (scrollAnimator.isRunning) {
                scrollAnimator.removeAllUpdateListeners()
                scrollAnimator.removeAllListeners()
                scrollAnimator.end()
                scrollAnimator.cancel()
            }
        }
    }

    companion object {
        private fun View.getStackLayoutParams(): StackLayoutParams {
            return this.layoutParams as StackLayoutParams
        }
    }

    private fun findOneVisibleChild(
        fromIndex: Int,
        toIndex: Int,
        completelyVisible: Boolean
    ): View? {
        val next = if (toIndex > fromIndex) 1 else -1
        var partiallyVisible: View? = null
        var i = fromIndex
        while (i != toIndex) {
            val child = getChildAt(i)
            if (child!!.top < height) {
                if (completelyVisible) {
                    if (child.top < height && child.top + decoratedChildHeight < height) {
                        return child
                    } else if (!completelyVisible && partiallyVisible == null) {
                        partiallyVisible = child
                    }
                } else {
                    return child
                }
            }
            i += next
        }
        return partiallyVisible
    }

    private fun createView(
        index: Int,
        recycler: Recycler,
        state: RecyclerView.State
    ): View? {
        if (state.itemCount == 0) return null
        val tv = recycler.getViewForPosition(index)
        if (tv.parent == null) {
            addView(tv, index % maxViews)
            layoutView(tv)
        }
        return tv
    }

    private fun layoutView(tv: View) {
        measureChildWithMargins(tv, 0, 0)
        tmpRect.setEmpty()
        if (tv.background != null) {
            tv.background.getPadding(tmpRect)
        }
        layoutDecoratedWithMargins(
            tv,
            viewRect.left - tmpRect.left,
            viewRect.top - tmpRect.top,
            viewRect.right + tmpRect.right,
            viewRect.bottom + tmpRect.bottom
        )
    }

    fun peek() {
        stopScrolling()
        val out =
            animateScrollToItem(scroller.stackScroll + 0.6f) {
                val `in` = animateScrollToItem(
                    scroller.stackScroll - 0.6f,
                    stopScrollingRunnable
                )
                `in`.interpolator = Internal.ACCELERATE_INTERPOLATOR
                `in`.duration = 400
                `in`.startDelay = 50
                `in`.start()
            }
        out.interpolator = Internal.SCROLL_INTERPOLATOR
        out.duration = 300
        out.start()
    }

    fun snap() {
        val toPosition = scroller.stackScroll.roundToInt().toFloat()
        animateScrollToItem(toPosition, null)
            .also {
                val delta = kotlin.math.abs(scroller.stackScroll - toPosition)
                it.duration = (500f * (1f - delta)).toLong()
            }
            .start()
    }

    internal object Internal {
        val ACCELERATE_INTERPOLATOR: TimeInterpolator = LogAccelerateInterpolator(60, 0)
        val SCROLL_INTERPOLATOR: TimeInterpolator = LogDecelerateInterpolator(60f, 0f)
        val LAYOUT_INTERPOLATOR: TimeInterpolator = LogDecelerateInterpolator(20f, 0f)
        val DIMMING_INTERPOLATOR: TimeInterpolator = LinearInterpolator()

        internal class LogDecelerateInterpolator(
            private val base: Float,
            private val drift: Float
        ) :
            TimeInterpolator {
            private fun computeLog(t: Float): Float {
                return 1f - base.toDouble().pow(-t.toDouble()).toFloat() + drift * t
            }

            override fun getInterpolation(t: Float): Float {
                return computeLog(t) / computeLog(1f)
            }

        }

        internal class LogAccelerateInterpolator(private val mBase: Int, private val mDrift: Int) :
            TimeInterpolator {
            private val mLogScale: Float = 1f / computeLog(1f, mBase, mDrift)
            override fun getInterpolation(t: Float): Float {
                return 1 - computeLog(
                    1 - t,
                    mBase,
                    mDrift
                ) * mLogScale
            }

            private fun computeLog(t: Float, base: Int, drift: Int): Float {
                return (-base.toDouble().pow(-t.toDouble())).toFloat() + 1 + drift * t
            }

        }

        fun clamp(value: Float, min: Float, max: Float): Float {
            return max(min, min(max, value))
        }

        fun clamp01(value: Float): Float {
            return max(0f, min(1f, value))
        }
    }
}
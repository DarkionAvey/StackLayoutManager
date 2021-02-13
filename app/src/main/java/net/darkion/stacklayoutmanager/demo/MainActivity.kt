package net.darkion.stacklayoutmanager.demo

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.google.android.material.snackbar.Snackbar
import net.darkion.stacklayoutmanager.demo.layoutinterpolators.*
import net.darkion.stacklayoutmanager.demo.transformers.RotationTransformer
import net.darkion.stacklayoutmanager.demo.transformers.ScaleInOnlyTransformer
import net.darkion.stacklayoutmanager.demo.transformers.ScaleOutOnlyTransformer
import net.darkion.stacklayoutmanager.demo.transformers.ScaleTransformer
import net.darkion.stacklayoutmanager.library.StackLayoutManager


@Suppress("ConstantConditionIf")
class MainActivity : AppCompatActivity() {

    companion object {
        //this flag is used to hide UI elements
        //for screen recording purposes
        const val showcaseMode = false
        var squareItems = true
    }

    private val data by lazy { Data(isConnected()) }

    private val stackLayoutManager by lazy {
        StackLayoutManager(
            maxViews = 8,
            horizontalLayout = false,
            scrollMultiplier = 1.5f
        )
    }
    private val recyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerView).also {
            it.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        }
    }
    private val snapHelper = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState != SCROLL_STATE_DRAGGING) {
                stackLayoutManager.snap()
            }
        }
    }
    private var attachedSnapHelper = false
    private val glide by lazy {
        Glide.with(this)
    }
    private var currentTransformer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.apply {
            hasFixedSize()
            layoutManager = stackLayoutManager
            adapter = CardsAdapter()
            postDelayed({ stackLayoutManager.peek() }, 1000)
        }
        setUpDemo()
    }

    private fun hideUiElementsIfShowcase() {
        if (!showcaseMode) return
        val views = arrayOf(
            R.id.background,
            R.id.landscape,
            R.id.attention,
            R.id.snap,
            R.id.interpolator,
            R.id.transformer,
            R.id.guideline,
            R.id.size_fullscreen,
            R.id.size_square
        )
        for (id in views) {
            findViewById<View>(id).alpha =
                if (showcaseMode) 0f else 1f
        }

        findViewById<View>(R.id.recyclerView).layoutParams.height = -1
        findViewById<View>(R.id.container).setBackgroundColor(Color.WHITE)

        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }

    private fun changeItemsSize(square: Boolean) {
        if (squareItems == square) return
        squareItems = square
        stackLayoutManager.forceRemeasureInNextLayout()
        recyclerView.adapter?.notifyItemRangeChanged(
            recyclerView.getChildAdapterPosition(
                recyclerView.getChildAt(0)
            ), recyclerView.childCount
        )
        toast("${if (squareItems) "Square" else "Full"} mode")
    }

    private fun setUpDemo() {
        hideUiElementsIfShowcase()

        findViewById<View>(R.id.size_square).setOnClickListener {
            changeItemsSize(true)
        }

        findViewById<View>(R.id.size_fullscreen).setOnClickListener {
            changeItemsSize(false)
        }

        findViewById<View>(R.id.landscape).setOnClickListener {
            resetViews()
            stackLayoutManager.horizontalLayout = !stackLayoutManager.horizontalLayout
            toast("Showing in ${if (stackLayoutManager.horizontalLayout) "horizontal" else "vertical"} mode")
        }

        findViewById<View>(R.id.attention).setOnClickListener {
            stackLayoutManager.peek()
        }

        findViewById<View>(R.id.snap).setOnClickListener {
            if (!attachedSnapHelper) {
                recyclerView.addOnScrollListener(snapHelper)
                stackLayoutManager.snap()
            } else recyclerView.removeOnScrollListener(snapHelper)
            attachedSnapHelper = !attachedSnapHelper
            toast("Snapping ${if (attachedSnapHelper) "enabled" else "disabled"}")
        }

        findViewById<View>(R.id.interpolator).setOnClickListener {
            resetViews()
            val nextInterpolator =
                layoutInterpolators.indexOf(stackLayoutManager.layoutInterpolator) + 1
            val interpolatorObj = layoutInterpolators[nextInterpolator % layoutInterpolators.size]
            if (interpolatorObj is ReverseStackInterpolator) {
                //ReverseStackInterpolator uses its own transformer
                //to allow most recent views (higher position) to be stacked underneath
                //current view by reversing the translationZ property
                stackLayoutManager.viewTransformer = ReverseStackInterpolator.Transformer::transform
            } else if (stackLayoutManager.viewTransformer == ReverseStackInterpolator.Transformer::transform) {
                //if ReverseStackInterpolator transformer was previously set, revert to the last used
                //transformer
                stackLayoutManager.viewTransformer =
                    transformers[currentTransformer % transformers.size]
                resetViews()
            }
            stackLayoutManager.layoutInterpolator = interpolatorObj
            toast("Current interpolator: ${interpolatorObj.javaClass.simpleName}")
        }
        findViewById<View>(R.id.transformer).setOnClickListener {
            if (stackLayoutManager.layoutInterpolator is ReverseStackInterpolator) {
                toast("The path currently set does not support transformation. Please choose another path")
                return@setOnClickListener
            }
            resetViews()
            currentTransformer++
            stackLayoutManager.viewTransformer =
                transformers[currentTransformer % transformers.size]
            toast(
                "Current transform: ${data.transformersNames[currentTransformer % data.transformersNames.size]}"
            )
        }
    }

    private fun resetViews() {
        recyclerView.stopScroll()
        recyclerView.children.forEach {
            it.resetTransformations()
        }
    }

    //manually remove all transformations made to a view
    //we need to do this manually since the layout manager
    //does not know what transformations were made to a view
    private fun View?.resetTransformations() {
        this?.apply {
            scaleY = 1f
            scaleX = 1f
            translationX = 0f
            translationY = 0f
            elevation = 0f
            translationZ = 0f
            rotationX = 0f
            rotationY = 0f
        }
    }

    private fun toast(text: String) {
        Snackbar.make(recyclerView, text, 4000).setAnchorView(R.id.background).show()
    }

    private val transformers by lazy {
        arrayOf(
            //this is the default transformer supplied by StackLayoutManager
            StackLayoutManager.ElevationTransformer::transform,
            //scale out the view as it exists the screen
            ScaleOutOnlyTransformer::transform,
            //scale in the view as it enters the screen
            ScaleInOnlyTransformer::transform,
            //a combination of scale-out and scale-in transformers
            ScaleTransformer::transform,
            //rotate the view along either of its axes
            RotationTransformer::transform
        )
    }

    private val layoutInterpolators by lazy {
        arrayOf(
            LinearInterpolator(),
            DenseStackInterpolator(),
            ReverseStackInterpolator(),
            LogDecelerateInterpolator(60f, 0),
            OvershootingInterpolator(),
            CappedLinearInterpolator()
        )
    }

    inner class CardsAdapter : RecyclerView.Adapter<CardsViewHolder>(), View.OnClickListener,
        View.OnLongClickListener {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardsViewHolder {
            return CardsViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.recycler_view_item, parent, false)
            )
        }

        init {
            setHasStableIds(true)
        }

        override fun onViewRecycled(holder: CardsViewHolder) {
            super.onViewRecycled(holder)
            glide.clear(holder.thumbImageView)
        }

        override fun getItemId(position: Int): Long {
            //this is fine for demo purposes but
            //more advanced identification is usually needed
            //for complex data
            return position.toLong()
        }

        override fun getItemCount(): Int {
            return 101
        }

        override fun onBindViewHolder(holder: CardsViewHolder, position: Int) {
            holder.itemView.resetTransformations()
            holder.card.setOnClickListener(this)
            holder.card.setOnLongClickListener(this)
            holder.positionTextView.text = position.toString()
            glide
                .load(data.images[position % data.images.size])
                .format(DecodeFormat.PREFER_ARGB_8888)
                .centerCrop()
                .thumbnail(0.3f)
                .into(holder.thumbImageView)
        }

        override fun onClick(v: View?) {
            v ?: return
            recyclerView?.also { recyclerView ->
                val position = recyclerView.getChildViewHolder(v)?.adapterPosition ?: -1
                if (position >= 0) {
                    (recyclerView.layoutManager as? StackLayoutManager)?.scrollToPosition(position)
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            v ?: return false
            recyclerView?.also { recyclerView ->
                val position = recyclerView.getChildViewHolder(v)?.adapterPosition ?: -1
                if (position >= 0) {
                    (recyclerView.layoutManager as? StackLayoutManager)?.scrollToPosition(
                        position + data.images.size.toFloat(),
                        animated = true,
                        duration = data.images.size * 250L
                    )
                }
            }
            return true
        }

    }

    class CardsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card = view as Card
        val thumbImageView = view.findViewById(R.id.thumb) as AppCompatImageView
        val positionTextView = (view.findViewById(R.id.position) as TextView).also {
            it.alpha = if (showcaseMode) 0f else 1f
        }

    }

    //simple internet check
    fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return cm.activeNetwork != null
        }
        val nInfo = cm.activeNetworkInfo
        return nInfo != null && nInfo.isAvailable && nInfo.isConnected
    }

}
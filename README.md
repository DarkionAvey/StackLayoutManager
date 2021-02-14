<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/cover_static.png">
</p>

# StackLayoutManager
StackLayoutManager is a lightweight and highly-customizable layout manager for RecyclerView which lays out views by using a [`android.animation.TimeInterpolator`](https://developer.android.com/reference/android/animation/TimeInterpolator) object. This makes the views travel along the path that the interpolator provides, thus allowing us to create sophisticated transitions. Additionally, it supports setting a custom view transformation callback, which allows more elaborate transitions to be made through direct access to all transformation properties a view can have.

# Installation
StackLayoutManager is a single Kotlin file, so you just need to copy the [`StackLayoutManager.kt`](https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/stacklayoutmanager/src/main/java/net/darkion/stacklayoutmanager/library/StackLayoutManager.kt) file to your project and change the `package` declaration. That's it!


# Setup
StackLayoutManager constructer doesn't require any parameters, so you can just use it directly like this
```
recyclerView.layoutManager = StackLayoutManager()
```
By default, StackLayoutManager will layout views horizontally and will center the first item (`position == 0`). If you want to change either of those, or change other parameters, the constructer has a few optional, named parameters which you can set to customize your experience. Those parameters are:


| Parameter  | Type | Default | Discription | 
|:--- | :---: | :---: | :--- | 
| `horizontalLayout`  | Boolean | `true`  | whether views should be laid out horizontally or vertically|
| `centerFirstItem`  | Boolean | `true`  | whether first item (with `position == 0`) should be centered. In some instances, specially when using Log interpolators, setting this to false will position the first child off-screen which could be confusing to users |
| `scrollMultiplier`  | Float | `2f`  | the number of items (as float, where each item is equal to `1f`) that should be scrolled in one swipe. Setting higher values will cause stuttering because of float rounding. Even though the default value is 2f, it is recommended that you set this to `1.2f` |
| `maxViews`  | Int | `6` | the maximum number of views that the RecyclerView should have. Higher values require more resources and might cause lag in some cases. It may be neccessary to increase this number when using layout interpolators that create stacking effect, such as the case with [`DenseStackInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/DenseStackInterpolator.kt). It is recommeneded that you set this to anything between 6 and 10 |
| `layoutInterpolator`  | TimeInterpolator | `LogDecelerateInterpolator`  | the interpolator which will be used to layout views. Any type of interpolator is accepted, such as `LinearInterpolator()`, `FastOutSlowInInterpolator()`, or the ones shown in the demo app. The interpolation typically starts from `x, y = 0f, 0f` and ends at `x, y = 1f, 1f`, but that is not always the case, as demonstrated in [`DenseStackInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/DenseStackInterpolator.kt). StackLayoutManager itself comes with only one interpolator, so you might need to copy one of the provided interpolators from the demo app or make your own interpolator. Check out the section below to see some of the provided interpolators |
| `viewTransformer` | `((x: Float, view: View, stackLayoutManager: StackLayoutManager) -> Unit?)?` | [`ElevationTransformer`](https://github.com/DarkionAvey/StackLayoutManager/blob/fc5f05a72d4d9a784783cda1c2403ef8ccc7175b/stacklayoutmanager/src/main/java/net/darkion/stacklayoutmanager/library/StackLayoutManager.kt#L449) | supply a higher-order function that takes `Float, View, StackLayoutManager` and returns `Unit` (void). This is used to set a callback which allows the caller to have a direct access to a view after it has been laid out by the layout manager. By using this, you will be able to apply additional transforms to the view, including the stock transforms such as **rotation**, **scale**, **elevation**; as well as any custom attribute of a custom view. The discriptions of the function parameters are as follows: <ul><li>`x: Float` a float value that represents the progress of the supplied view. This value equals to 0 when the view is at the center of the screen, greater than 0 when entering the screen (e.g., sliding in from bottom), and less than 0 when exiting the screen (e.g., sliding out to top). This value is not capped between -1f and 1f since it depends on `maxViews` property, and it essentially represents `position - scroll`. This value may be used with interpolators to obtain an interpolated `y` value</li><li>`view: View` the view that is currently being transformed</li><li>`stackLayoutManager: StackLayoutManager` the instance of StackLayoutManager for convenience should you want to check the parameters of the layout manager (for example, check `horizontalLayout` to rotate view along the corresponding axis)</li></ul>Check out the section below to see some of the provided transformers|

# Public functions


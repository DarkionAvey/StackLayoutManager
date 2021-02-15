<p align="center">
  <img width="50%" src="https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/cover_static.png">
</p>

<img width="30%" align="right" src="https://github.com/DarkionAvey/StackLayoutManager/raw/master/Showcase/screenshot_demo_app.png" >

# StackLayoutManager
StackLayoutManager is a lightweight and highly-customizable layout manager for RecyclerView which lays out views by using a [`TimeInterpolator`](https://developer.android.com/reference/android/animation/TimeInterpolator) object. This makes the views travel along the path that the interpolator provides, thus allowing us to create sophisticated transitions. Additionally, it supports setting a custom view transformation callback, which allows more elaborate transitions to be made through direct access to all transformation properties a view can have.

# Demo app
You can download the demo APK from [here](https://github.com/DarkionAvey/StackLayoutManager/releases/download/v1/app-release.apk)

# Installation
StackLayoutManager is a single Kotlin file, so you just need to copy the [`StackLayoutManager.kt`](https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/stacklayoutmanager/src/main/java/net/darkion/stacklayoutmanager/library/StackLayoutManager.kt) file to your project and change the `package` declaration. That's it!

<br/>

# Showcase

## Interpolators

|![Default/LogDecelerateInterpolator](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/log_decelerate_60_interpolator.webp?raw=true)Default|![DenseStackInterpolator](https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/gifs/dense_interpolator_vertical.webp)[`DenseStackInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/DenseStackInterpolator.kt)|![LinearInterpolator](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/linear_interpolator.webp?raw=true)`LinearInterpolator`|
| :--: | :--: | :--: |
|![OvershootingInterpolator](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/overshooting_interpolator.webp?raw=true)[`OvershootingInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/OvershootingInterpolator.kt)|![ReverseStackInterpolator](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/reverse_interpolator.webp?raw=true)[`ReverseStackInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/ReverseStackInterpolator.kt) |![CappedLinearInterpolator](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/capped_linear_interpolator.webp?raw=true)[`CappedLinearInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/CappedLinearInterpolator.kt)|


## Transformers
|![ScaleInOnlyTransformer](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/scale_in_transform.webp?raw=true)|![ScaleOutOnlyTransformer](https://raw.githubusercontent.com/DarkionAvey/StackLayoutManager/master/Showcase/gifs/scale_out_transform.webp)|![ScaleTransformer](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/scale_in_out.webp?raw=true)|![RotationTransformer](https://github.com/DarkionAvey/StackLayoutManager/blob/master/Showcase/gifs/3d_transform.webp?raw=true)|
| :--: | :--: | :--: | :--: |
|[`ScaleIn...former.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/transformers/ScaleInOnlyTransformer.kt)|[`ScaleOut...ormer.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/transformers/ScaleOutOnlyTransformer.kt)|[`ScaleTransformer.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/transformers/ScaleTransformer.kt)|[`Rotation...ormer.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/transformers/RotationTransformer.kt)|

# Setup
StackLayoutManager constructor doesn't require any parameters, so you can just use it directly like this
```
recyclerView.layoutManager = StackLayoutManager()
```
By default, StackLayoutManager will layout views horizontally and will center the first item (`position == 0`). If you want to change either of those, or change other parameters, the constructor has a few optional, named parameters which you can set to customize your experience. Those parameters are:


| Parameter  | Type | Default | Description | 
|:---: | :-: | :-: | :--- | 
| `horizontalLayout`  | Boolean | `true`  | whether views should be laid out horizontally or vertically|
| `centerFirstItem`  | Boolean | `true`  | whether the first item (with `position == 0`) should be centered. In some instances, specially when using Log interpolators, setting this to false will position the first child off-screen which could be confusing to users |
| `scrollMultiplier`  | Float | `1.2f`  | the number of items (as float, where each item is equal to `1f`) that should be scrolled in one swipe. Setting higher values will cause stuttering because of float rounding. It is recommended that you set this to `1.2f` |
| `maxViews`  | Int | `6` | the maximum number of views that the RecyclerView should have. Higher values require more resources and might cause lag in some cases. It may be necessary to increase this number when using layout interpolators that create stacking effects, such as the case with [`DenseStackInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/DenseStackInterpolator.kt). It is recommended that you set this to anything between 6 and 10 |
| `layoutInterpolator`  | TimeInterpolator | `LogDecelerateInterpolator`  | the interpolator which will be used to layout views. Any type of interpolator is accepted, such as `LinearInterpolator()`, `FastOutSlowInInterpolator()`, or the ones shown in the demo app. The interpolation typically starts from `x, y = 0f, 0f` and ends at `x, y = 1f, 1f`, but that is not always the case, as demonstrated in [`DenseStackInterpolator.kt`](https://github.com/DarkionAvey/StackLayoutManager/blob/master/app/src/main/java/net/darkion/stacklayoutmanager/demo/layoutinterpolators/DenseStackInterpolator.kt). StackLayoutManager itself comes with only one interpolator, so you might need to copy one of the provided interpolators from the demo app or make your own interpolator. Check out the showcase section to see some of the provided interpolators |
| `viewTransformer` | `((x: Float, view: View, stackLayoutManager: StackLayoutManager) -> Unit?)?` | [`ElevationTransformer`](https://github.com/DarkionAvey/StackLayoutManager/blob/fc5f05a72d4d9a784783cda1c2403ef8ccc7175b/stacklayoutmanager/src/main/java/net/darkion/stacklayoutmanager/library/StackLayoutManager.kt#L449) | supply a higher-order function that takes `Float, View, StackLayoutManager` and returns `Unit` (void). This is used to set a callback which allows the caller to have direct access to a view after it has been laid out by the layout manager. By using this, you will be able to apply additional transforms to the view, including the stock transforms such as **rotation**, **scale**, **elevation**; as well as any custom attribute of a custom view. The descriptions of the function parameters are as follows: <ul><li>`x: Float` a float value that represents the progress of the supplied view. This value equals to 0 when the view is at the center of the screen, greater than 0 when entering the screen (e.g., sliding in from bottom), and less than 0 when exiting the screen (e.g., sliding out to top). This value is not capped between -1f and 1f since it depends on `maxViews` property, and it essentially represents `position - scroll`. This value may be used with interpolators to obtain an interpolated `y` value</li><li>`view: View` the view that is currently being transformed</li><li>`stackLayoutManager: StackLayoutManager` the instance of StackLayoutManager for convenience should you want to check the parameters of the layout manager (for example, check `horizontalLayout` to rotate view along the corresponding axis)</li></ul>Check out the showcase section to see some of the provided transformers|

# Public functions
| Function  | Description | 
|:--- | :--- | 
| `scrollToPosition` | scroll to position with more control over the animation compared to the method provided by the default layout manager. Using this function, you can pass a float value as the target position, which translates to "target position + extra distance". You can also pass named parameters for more control. Those include: `animated`; `duration` which is applicable when `animated` is `true`; and `endRunnable` which is executed after the scroll has ended|
| `forceRemeasureInNextLayout` | notify the layout manager that the dimensions of the views have changed. By default, the layout manager measures only one child and then assumes the same dimensions for the rest of the views. Calling this method will remeasure that one standard view |
| `findFirstVisibleItemPosition`, `findFirstCompletelyVisibleItemPosition`, `findLastVisibleItemPosition`, and `findLastCompletelyVisibleItemPosition` | return the corresponding position of view as described in the function's name |
| `stopScrolling` | helper method to force stop scroll animator. This does not stop the scroll initiated by user input (e.g., fling) |
| `peek` | a helpful function that can be used to nudge or guide the user to the correct scrolling direction, or to inform the user that there are some views that are off-screen | 
| `snap` | as the name suggests, this will cause the layout manager to snap to the next or previous view depending on the current scroll fraction |

# Credits
The demo app uses assets from:
* [SVGBackgrounds](https://www.svgbackgrounds.com/)
* [Simone Hutsch @ Unsplash](https://unsplash.com/@heysupersimi)
* WishforgeGames @ IconFinder
* RoyyanWijaya @ IconFinder

# License 
MIT
```
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```


@file:Suppress("ConstantConditionIf")

package net.darkion.stacklayoutmanager.demo

//if not connected to internet, show the
//drawables that are packaged with the apk
class Data(connected: Boolean) {

    //names of the transformers for displaying toasts
    val transformersNames by lazy {
        arrayOf(
            "Default (ElevationTransformer)",
            "Scale Out",
            "Scale In",
            "Scale In and Out ",
            "3D rotation"
        )
    }

    val images: Array<Any> =
        if (connected || MainActivity.showcaseMode) {
            arrayOf(
                //amazing architecture pictures by Simone Hutsch
                "https://images.unsplash.com/photo-1506438689880-92e5d6b50ff9?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=800&q=80" as Any,
                "https://images.unsplash.com/photo-1532079746053-4fb0c408ce6e?ixlib=rb-1.2.1&ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&auto=format&fit=crop&w=2040&q=80",
                "https://images.unsplash.com/photo-1534240724593-cfacb25cf6ad?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=635&q=80",
                "https://images.unsplash.com/photo-1536736693558-ad17e44c156b?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=1378&q=80",
                "https://images.unsplash.com/photo-1603347201544-a4fba96efa04?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=675&q=80",
                "https://images.unsplash.com/photo-1531319596683-4712e6d1db2c?ixlib=rb-1.2.1&ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&auto=format&fit=crop&w=1943&q=80",
                "https://images.unsplash.com/photo-1535462009050-27ddea306055?ixlib=rb-1.2.1&ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&auto=format&fit=crop&w=649&q=80",
                "https://images.unsplash.com/photo-1529307474719-3d0a417aaf8a?ixlib=rb-1.2.1&ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&auto=format&fit=crop&w=627&q=80",
                "https://images.unsplash.com/photo-1526547050953-b9fe7299eb69?ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&ixlib=rb-1.2.1&auto=format&fit=crop&w=657&q=80",
                "https://images.unsplash.com/photo-1531323803217-ba21570d0e41?ixlib=rb-1.2.1&ixid=MXwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHw%3D&auto=format&fit=crop&w=2128&q=80"
            )
        } else {
            arrayOf(
                R.drawable.ic_bullseye_gradient as Any,
                R.drawable.ic_flat_mountains,
                R.drawable.ic_confetti_doodles,
                R.drawable.ic_rainbow_vortex,
                R.drawable.ic_repeating_chevrons,
                R.drawable.ic_hollowed_boxes,
                R.drawable.ic_geometric_intersection,
                R.drawable.ic_cornered_stairs
            )

        }
}
commit f3f84d2f2112549be6e9e6bd82ef09a81e109a5c
Author: Weston Ruter <weston@xwp.co>
Date:   Mon Mar 21 21:59:29 2016 +0000

    Customize: Require opt-in for selective refresh of widgets.

    * Introduces `customize-selective-refresh-widgets` theme support feature and adds to themes.
    * Introduces `customize_selective_refresh` arg for `WP_Widget::$widget_options` and adds to all core widgets.
    * Remove `selective_refresh` from being a component that can be removed via `customize_loaded_components` filter.
    * Add `WP_Customize_Widgets::get_selective_refreshable_widgets()` and `WP_Customize_Widgets::is_widget_selective_refreshable()`.
    * Fix default `selector` for `Partial` instances.
    * Implement and improve Masronry sidebar refresh logic in Twenty Thirteen and Twenty Fourteen, including preservation of initial widget position after refresh.
    * Re-initialize ME.js when refreshing `Twenty_Fourteen_Ephemera_Widget`.

    See #27355.
    Fixes #35855.

    Built from https://develop.svn.wordpress.org/trunk@37040


    git-svn-id: http://core.svn.wordpress.org/trunk@37007 1a063a9b-81f0-0310-95a4-ce76da25c4cd
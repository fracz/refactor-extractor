commit 1fe64b1c6582ca4b87f026ab259306c0f053bd8a
Author: Weston Ruter <weston@xwp.co>
Date:   Sat Oct 10 09:06:25 2015 +0000

    Customizer: Fix scalability performance problem for previewing multidimensional settings.

    As the number of multidimensional settings (serialized options and theme mods) increase for a given ID base (e.g. a widget of a certain type), the number of calls to the `multidimensional` methods on `WP_Customize_Setting` increase exponentially, and the time for the preview to refresh grows in time exponentially as well.

    To improve performance, this change reduces the number of filters needed to preview the settings off of a multidimensional root from N to 1. This improves performance from `O(n^2)` to `O(n)`, but the linear increase is so low that the performance is essentially `O(1)` in comparison. This is achieved by introducing the concept of an "aggregated multidimensional" setting, where the root value of the multidimensional serialized setting value gets cached in a static array variable shared across all settings.

    Also improves performance by only adding preview filters if there is actually a need to do so: there is no need to add a filter if there is an initial value and if there is no posted value for a given setting (if it is not dirty).

    Fixes #32103.

    Built from https://develop.svn.wordpress.org/trunk@35007


    git-svn-id: http://core.svn.wordpress.org/trunk@34972 1a063a9b-81f0-0310-95a4-ce76da25c4cd
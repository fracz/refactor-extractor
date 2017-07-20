commit a31d79196d0e7d313ca614359c7a7458c74f19b5
Author: Ella Iseulde Van Dorpe <*@iseulde.com>
Date:   Wed Jun 17 04:42:25 2015 +0000

    TinyMCE: improve reposition method inline toolbars

    * Make sure the toolbar does not overlap the target, unless it is higher than half the visible editor area's height.
    * Allow the toolbar to have the preference to position itself above or below the target.
    * Cache DOM lookups.
    * Simplify the logic and fix various positioning issues.

    See #32604.


    Built from https://develop.svn.wordpress.org/trunk@32816


    git-svn-id: http://core.svn.wordpress.org/trunk@32787 1a063a9b-81f0-0310-95a4-ce76da25c4cd
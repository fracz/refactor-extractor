commit 3a37220f2a63023d3572298cd1ac710b65f73c26
Author: Weston Ruter <weston@xwp.co>
Date:   Mon May 2 19:52:28 2016 +0000

    Customize: Allow Esc key to collapse the currently-expanded panel, section (or control).

    Pressing Esc collapses any expanded widget or nav menu item controls, or any control that implements the expanding interface. Also improves alignment between `WidgetControl` and `MenuItemControl`, adding the `expanded` state and associated `expand`/`collapse` methods to nav menu items.

    Props purcebr, celloexpressions, westonruter.
    Fixes #22237.

    Built from https://develop.svn.wordpress.org/trunk@37347


    git-svn-id: http://core.svn.wordpress.org/trunk@37313 1a063a9b-81f0-0310-95a4-ce76da25c4cd
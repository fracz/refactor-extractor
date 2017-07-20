commit 0a848a6171cdbc6ded5d1ede82838da5e32315e2
Author: Scott Taylor <scott.c.taylor@mac.com>
Date:   Wed Sep 17 15:14:17 2014 +0000

    `wptexturize()` improvements:

    * Expand the `wptexturize()` RegEx to include the list of registered shortcodes.
    * Avoid backtracking after `[` chars by not filtering params in registered shortcodes. This will cause escaped shortcodes and their params to become texturized if not registered.
    * Registered shortcode params will never be texturized, even when escaped.
    * Move all tests involving unregistered shortcodes to a new and improved unit.
    * Update one test involving HTML within shortcode params.

    Props miqrogroove.
    See #29557.

    Built from https://develop.svn.wordpress.org/trunk@29748


    git-svn-id: http://core.svn.wordpress.org/trunk@29520 1a063a9b-81f0-0310-95a4-ce76da25c4cd
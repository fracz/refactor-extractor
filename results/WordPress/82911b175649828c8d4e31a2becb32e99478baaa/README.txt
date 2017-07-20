commit 82911b175649828c8d4e31a2becb32e99478baaa
Author: Aaron Jorbin <aaron@jorb.in>
Date:   Fri Oct 21 02:54:34 2016 +0000

    Cache API: introduce wp_cache_get_last_changed to improve DRY

    One thing fairly common to the cache groups is a block of code to look to see when the cache was last changed, and if there isn't one, to set it for the current microtime(). It appears in 8 different places in core. This adds a new helper `wp_cache_get_last_changed` to DRY things up a bit.

    Since `wp-includes/cache.php` isn't guaranteed to be loaded, this new function is in `wp-includes/functions.php`

    Props spacedmonkey, desrosj.
    Fixes #37464.


    Built from https://develop.svn.wordpress.org/trunk@38849


    git-svn-id: http://core.svn.wordpress.org/trunk@38792 1a063a9b-81f0-0310-95a4-ce76da25c4cd
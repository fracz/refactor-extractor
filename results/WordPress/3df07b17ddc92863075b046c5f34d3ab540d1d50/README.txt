commit 3df07b17ddc92863075b046c5f34d3ab540d1d50
Author: Dominik Schilling <dominikschilling+git@gmail.com>
Date:   Thu Apr 21 19:58:27 2016 +0000

    Media: Don't cache the results of `wp_mkdir_p()` in a persistent cache.

    To improve the performance of `wp_upload_dir()` the result of `wp_mkdir_p()` was stored in a persistent cache, introduced in [36565]. But this becomes an issue when WordPress is scaled horizontally. You may end up caching a value for a server where the directory doesn't exist which will prevent further uploads on other servers because of the persistent cache.
    The fix is to use a non-persistent cache.

    Props azaozz, ocean90.
    See #34359.
    Fixes #36621.
    Built from https://develop.svn.wordpress.org/trunk@37285


    git-svn-id: http://core.svn.wordpress.org/trunk@37251 1a063a9b-81f0-0310-95a4-ce76da25c4cd
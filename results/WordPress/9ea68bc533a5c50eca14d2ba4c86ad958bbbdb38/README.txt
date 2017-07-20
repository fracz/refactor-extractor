commit 9ea68bc533a5c50eca14d2ba4c86ad958bbbdb38
Author: Pascal Birchler <pascal.birchler@gmail.com>
Date:   Fri Jan 15 10:23:25 2016 +0000

    Script Loader: Add `Etag: $wp_version` header in `load-scripts.php` and `load-styles.php`.

    This improves performance since browsers won't re-download the scripts and styles when there was no change in `$wp_version`.

    Props sergej.mueller, dd32, swissspidy.
    Fixes #28722.
    Built from https://develop.svn.wordpress.org/trunk@36312


    git-svn-id: http://core.svn.wordpress.org/trunk@36279 1a063a9b-81f0-0310-95a4-ce76da25c4cd
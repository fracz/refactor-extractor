commit 5a5c86139f91fdf7ef0c30fe560dec8120d2a415
Author: Scott Taylor <scott.c.taylor@mac.com>
Date:   Thu May 15 19:58:15 2014 +0000

    Eliminate use of `extract()` in `wp_get_object_terms()`.

    There are 3 properties, just set them to variables. They are used too often to warrant a refactor.

    See #22400.

    Built from https://develop.svn.wordpress.org/trunk@28441


    git-svn-id: http://core.svn.wordpress.org/trunk@28268 1a063a9b-81f0-0310-95a4-ce76da25c4cd
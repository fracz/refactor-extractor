commit 91312e71cdd6e5b49acae2bebf85468210c9469c
Author: Scott Taylor <scott.c.taylor@mac.com>
Date:   Mon Jan 12 16:17:22 2015 +0000

    In lieu of refactoring, add a `private` field to `WP_MS_Themes_List_Table`, `$has_items`. Ideally, this class would overload `->has_items()` and not set a `private` field.

    See #30891.

    Built from https://develop.svn.wordpress.org/trunk@31163


    git-svn-id: http://core.svn.wordpress.org/trunk@31144 1a063a9b-81f0-0310-95a4-ce76da25c4cd
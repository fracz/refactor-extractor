commit 7d8451ff70a528233791dcc78c780b016cbeb247
Author: James Nylen <jnylen@gmail.com>
Date:   Thu Apr 13 20:22:44 2017 +0000

    Media: Improve filters that allow overriding slow media queries.

    As a follow-up to [40382], this commit makes the following improvements:

    - Make the filter names more specific.
    - Fix the inline documentation (use `@param` instead of `@return).
    - Use `null ===` instead of `is_null` to avoid extra function calls.
    - Rename the `$has_audio` and `$has_video` variables now that they actually represent something slightly different.

    See #31071.

    Built from https://develop.svn.wordpress.org/trunk@40421


    git-svn-id: http://core.svn.wordpress.org/trunk@40319 1a063a9b-81f0-0310-95a4-ce76da25c4cd
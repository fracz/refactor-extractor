commit 6b3251e931d01a9095b320ce12179cb9fe9dff63
Author: Scott Taylor <scott.c.taylor@mac.com>
Date:   Thu Aug 7 19:11:18 2014 +0000

    wptexturize: After [28727], leverage RegeEx possessives (++) to improve performance and avoid libpcre segfaults.

    Props kovshenin.
    Fixes #12690.

    Built from https://develop.svn.wordpress.org/trunk@29431


    git-svn-id: http://core.svn.wordpress.org/trunk@29209 1a063a9b-81f0-0310-95a4-ce76da25c4cd
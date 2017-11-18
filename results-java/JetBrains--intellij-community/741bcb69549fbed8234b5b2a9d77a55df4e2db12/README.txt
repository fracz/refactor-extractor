commit 741bcb69549fbed8234b5b2a9d77a55df4e2db12
Author: Kirill Likhodedov <Kirill.Likhodedov@jetbrains.com>
Date:   Fri May 18 21:37:08 2012 +0400

    [merge] refactoring: make FragmentSide to be enum

    * Make some DRY.
    * Use IllegalArgumentException to a not-public InvalidParameterException
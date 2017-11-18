commit 9ef7307324f5e58d9a8dc0063110c1b1bd1e3d6d
Author: Jason Monk <jmonk@google.com>
Date:   Fri Jan 27 13:32:51 2017 -0500

    Fix broken lock screen affordances

    The state change callback stopped getting added as part of the
    dependency injection refactor, which turns out has a pretty serious
    effect on how the buttons work.

    Change-Id: Ic2c412248373de80acc581d09e3073f541238e9a
    Fixes: 34739383
    Test: Click on lock screen mic or camera
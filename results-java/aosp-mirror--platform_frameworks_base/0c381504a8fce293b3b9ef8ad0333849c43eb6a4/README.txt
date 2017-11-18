commit 0c381504a8fce293b3b9ef8ad0333849c43eb6a4
Author: Svetoslav Ganov <svetoslavganov@google.com>
Date:   Mon Sep 10 10:48:58 2012 -0700

    Improve scaling vs pan in screen magnifier.

    1. Due to frequent changes of the behavior of ScaleGestureDetector
       this patch rolls in a gesture detector used for changing the
       screen magnification level. It has an improved algorithm which
       uses the diameter of min circle around the points as the span, the
       center of this circle as the focal point, and the average slop
       of the lines from each pointer to the center to determine the
       angle of the diameter used when computing the span x and y.

    Change-Id: I5cee8dba84032a0702016b8f9632f78139024bbe
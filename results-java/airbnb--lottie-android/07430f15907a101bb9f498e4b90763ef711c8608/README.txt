commit 07430f15907a101bb9f498e4b90763ef711c8608
Author: Gabriel Peal <gpeal@users.noreply.github.com>
Date:   Wed Feb 15 23:02:32 2017 -0800

    Major refactor to store all animatable data in keyframes (#110)

    Prior to this, all animatable data was stored in lists of values, key times, and interpolators. This cleans that up significantly and stores it all in a new Keyframe class. The new code is much more readable and maintainable.
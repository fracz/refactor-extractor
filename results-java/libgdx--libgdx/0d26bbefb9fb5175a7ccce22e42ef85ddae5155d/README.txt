commit 0d26bbefb9fb5175a7ccce22e42ef85ddae5155d
Author: badlogicgames <badlogicgames@6c4fd544-2939-11df-bb46-9574ba5d0bfa>
Date:   Sat Jan 8 05:02:46 2011 +0000

    [changed] gdx2d nearly fully implemented. Todo: fix rgba4444 blending bug, improve blending by swizzling (see commented core in set_pixel_RGBA8888), add blitters. We are faster than Skia at the moment even with blending enabled :D
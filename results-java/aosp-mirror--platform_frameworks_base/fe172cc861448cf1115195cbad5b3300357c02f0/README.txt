commit fe172cc861448cf1115195cbad5b3300357c02f0
Author: Daniel Sandler <dsandler@android.com>
Date:   Mon Sep 12 13:47:25 2011 -0400

    Fix the takeoff position of a flung windowshade.

    We were carefully tracking the vertical offset between the view
    origin and the user's incident touch, but not actually
    including it when performing a fling. (This might have
    worked at one point but been later buried under some
    refactoring rubble.)

    Bug: 5210198
    Change-Id: I97ae883491a5dedf1b48683441096fe9938d118f
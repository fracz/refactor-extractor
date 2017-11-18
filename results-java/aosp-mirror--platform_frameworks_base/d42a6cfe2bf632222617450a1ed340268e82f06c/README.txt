commit d42a6cfe2bf632222617450a1ed340268e82f06c
Author: Winson Chung <winsonc@google.com>
Date:   Tue Jun 3 16:24:04 2014 -0700

    Exploring transitions to/from Recents.

    - refactored hwlayers and change view property animations to use a reference counted trigger
    - cleaned up RecentsConfiguration, and move it into classes using it
    - moved task bar animations back into TaskBarView
    - refactoring enter/exit animations to use an animation context

    Change-Id: Ia66b622b094f22145c2fab07c2a9bdfd62344be2
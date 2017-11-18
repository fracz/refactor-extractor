commit ebfc6981828b0699eef85c58b23a61f2cac41af3
Author: Winson Chung <winsonc@google.com>
Date:   Tue Aug 26 12:25:34 2014 -0700

    Additional changes to improve performance when loading Recents. (Bug 16987565)

    - Ensuring that we consistently load activity icon, title and color for both full and shallow stacks
    - Adding activity info caches when loading the stacks
    - Should not be scaling pin icon when launching from pin
    - Tweaking snap-back, over scroll, and shadows

    Change-Id: I556b93562bb2c69e4c25ce787a7a34532ab706ca
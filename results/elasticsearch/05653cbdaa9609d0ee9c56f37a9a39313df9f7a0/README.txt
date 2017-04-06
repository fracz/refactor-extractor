commit 05653cbdaa9609d0ee9c56f37a9a39313df9f7a0
Author: Shay Banon <kimchy@gmail.com>
Date:   Sat Sep 29 14:06:28 2012 +0200

    move cleaning filter cache on closed readers to separate thread
    improve cleaning the global weighted cache when a reader closes, move it to a separate thread, so iterating over the cache entries will nto happen on each segment closed, but instead be "bulked"
commit a5c86675ec20f9eb5468cdd037f6efac5e13d4a1
Author: Ognjen Dragoljevic <plamenko@fb.com>
Date:   Wed Mar 9 08:37:31 2016 -0800

    Minor refactor in multi-pointer gesture detector

    Summary:* Chnge internal state to `started` only after the listener is notified. This makes the contract more consistent and avoids some problems along the road.
    * Start gesture immediately on tap-down, instead of on first move. This was originally done in move event in case we'd like to start gesture only after pointer moves over some threshold, but this is still possible (if ever needed) and it is cleaner this way.
    * Keep information about `newCount` so that `onGestureStop` listener can know both the number of taps before and after. This information is useful for zoomable controller.
    * Other than that, some code is extracted to methods.

    Reviewed By: kirwan, aagnes

    Differential Revision: D2991569

    fb-gh-sync-id: fb388c791c458eeb861fb058981a6c329f6632d2
    fbshipit-source-id: fb388c791c458eeb861fb058981a6c329f6632d2
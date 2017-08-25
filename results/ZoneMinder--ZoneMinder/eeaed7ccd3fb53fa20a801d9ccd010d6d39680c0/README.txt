commit eeaed7ccd3fb53fa20a801d9ccd010d6d39680c0
Author: Kfir Itzhak <mastertheknife@gmail.com>
Date:   Tue Jun 7 23:14:01 2011 +0300

    Some small changes
    1) Fixed wrong size calculation in Image::AssignDirect.
    2) Improved libjpeg subpixel order selection.
    3) Finished adding support in the simple Overlay function.
    4) Completed Flip and Rotate 32bit RGB support.
    5) Allow ZM to compile on 32bit with omit frame pointer disabled by defining _DEBUG
       To use one less register in the SSE algorithms.
    6) Removed the counter variable in some loops to reduce loop overhead.
    7) Modified crop query error handling.
    8) Most of the shared data now declared as volatile.
    9) Small improvements to the AlarmedPixels motion detection.
    10) Changed the default blend percent from 7% to 12%.
    11) Fixed an earlier bug created by me: motion detection checking the wrong pixels.
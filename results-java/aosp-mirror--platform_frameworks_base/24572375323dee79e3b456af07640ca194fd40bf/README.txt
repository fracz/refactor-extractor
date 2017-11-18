commit 24572375323dee79e3b456af07640ca194fd40bf
Author: Jeff Brown <jeffbrown@google.com>
Date:   Thu Jun 9 19:05:15 2011 -0700

    Optimize orientation changes.

    Modified setRotation to allow it to restart a rotation in
    progress as long as the rotation animation has not yet started.
    This enables the system to recover more quickly from mispredicted
    orientation changes.

    Removed the call to System.gc() when freezing the display, which
    added 60-80ms before we even started the orientation change.
    We used to need this to make it less likely that an upcoming GC
    would cause a pause during the window animation, but this is
    not longer a concern with the concurrent GC in place.

    Changed the wallpaper surface to be 32bit.  This accelerates
    drawing and improves the overall appearance slightly.

    Reduced code duplication in the WallpaperManager.

    Change-Id: Ic6e5e8bdce4b970b11badddd0355baaed40df88a
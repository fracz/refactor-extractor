commit a2a6d028a45168efddca2e3f70409ad3e78e12ba
Author: abrahambotros <abe@humandx.org>
Date:   Fri Nov 18 14:38:23 2016 -0800

    Add 1080p, 720p, and 480p capture qualities. (#469)

    * Add 1080p, 720p, and 480p capture qualities.

    * Minor improvements for picture/video sizing.

    - Minor refactoring for getting supported sizes (DRY).
    - Add explicit pictureSize setting for 480p/720p/1080p in still/picture mode.

    * Use util.Size objects for 480p/720p/1080p sizing.

    - Note using Camera.Size objects would require a camera instance prior to creating the
      size objects, which would be manageable but not too clean.

    * Remove 480p for iOS; 16:9/HD aspect ratio for Android 480p

    - iOS only has a 640x480 480p-like AVCaptureSessionPreset, which is not the typical 16:9/HD aspect ratio
      desired. Removing this option as a result of this.
    - Android 480p updated to use 16:9/HD aspect ratio.

    * Add notes for (in)exact sizing for 1080p/720p/480p

    * Re-add 480p on iOS, more notes on resolutions.

    - Add notes on non-HD-aspect-ratio for iOS 480p.
    - Add more explanation of variable resolution/sizes, especially for 480p on Android.

    * Use custom Resolution class to hold 480p/720p/1080p resolution sizes

    - Mistakenly used util.Size class before, which was not added until Android API level 21 (current
      min is 16).
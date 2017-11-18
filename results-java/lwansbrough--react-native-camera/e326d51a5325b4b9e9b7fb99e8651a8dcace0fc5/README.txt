commit e326d51a5325b4b9e9b7fb99e8651a8dcace0fc5
Author: Marc Johnson <mj@aftercode.io>
Date:   Sat Aug 27 20:49:46 2016 -0500

    Android support for recording video (#262)

    * Initial commit with Android video support

    * stopCapture now works

    * Bug fixes and parameter enhancements.  README updated.

    * Modified stopCapture parameter count to match iOS

    * fixed promise bug on stopCapture

    * Update RCTCameraModule.java

    In Android preview and recording sizes are different, which can cause an error.  This fix detects the difference and chooses a recording resolution that matches.

    * Update RCTCameraModule.java

    * Update RCTCamera.java

    Creating video functions in style/convention of existing

    * Update RCTCameraModule.java

    Use new functions for adjusting video capture size and quality

    * Update RCTCameraModule.java

    Fixes issue where file not video playable (readable) on older devices

    * Update AndroidManifest.xml

    Since we're reading and writing video and pictures, need permissions for it.

    * Fixed upside down camera (on some platforms), and misc bugs and crashes

    * Added camera-roll and capture to memory support, new options, and support for duration, filesize, and metadata

    * To make merge nicer, temporarily reverting "Added camera-roll and capture to memory support, new options, and support for duration, filesize, and metadata"

    This reverts commit 9ea1ad409c7e6121cf0197172e752b7523d4b092.

    * Fixed merge & brought back all improvements from 9ea1ad4

    * Fixed logic for video -> camera roll

    * Updates

    * Uncommenting setProfile

    * Fix support for React Native 0.25

    * Renamed Camera to index

    * * Fix after merge android recording

    * * Fixed android camera roll file saving
    * Added recording to example

    * * Android promise rejections with exceptions
    * Fixed preview, video and photo sizes
    * Android recording result in new, javascript object, format

    * * Removed example.index.android.js as there is Example project

    * * Readme for example

    * don't force a specific codec

    * always use cache dir

    * * Using MediaScannerConnection instead of ACTION_MEDIA_SCANNER_SCAN_FILE intent

    * * As described in https://github.com/lwansbrough/react-native-camera/pull/262#issuecomment-239622268:
    - fixed video the wrong direction and recoder start fail at "low,medium" on the nexus 5 x
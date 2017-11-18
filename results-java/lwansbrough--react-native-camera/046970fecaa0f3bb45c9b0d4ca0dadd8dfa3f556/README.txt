commit 046970fecaa0f3bb45c9b0d4ca0dadd8dfa3f556
Author: Marc Johnson <mj@aftercode.io>
Date:   Mon Sep 12 22:29:14 2016 -0500

    Android video flipped rotated #388 and activity pause support (#394)

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

    * Fix minor orientation bug with front recording on android

    * (Android) Made video stop on activity pause

    * Revert "Merge pull request #6 from Reaction-Framework/marcejohnson-master"

    This reverts commit 8729c65a72af2afc8297e4a4de3c07a54da11580, reversing
    changes made to 50416eb0daae447b822307f257c31a1cbc240a2c.

    * Revert "Revert "Merge pull request #6 from Reaction-Framework/marcejohnson-master""

    This reverts commit 4b87b48c7bd92840566ad76c96961325c2291ee0.

    * replace System.console with Log.e (#390)
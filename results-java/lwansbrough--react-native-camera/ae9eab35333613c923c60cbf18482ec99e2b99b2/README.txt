commit ae9eab35333613c923c60cbf18482ec99e2b99b2
Author: Abe Botros <abrahambotros@gmail.com>
Date:   Mon Apr 24 09:25:49 2017 -0700

    (android): Android tap-to-focus and improved (continuous) auto-focus (#575)

    * Android tap-to-focus and improved (continuous) auto-focus

    Tap-to-focus

    - On tap, compute focus area around motion event's location, and pass this to the camera parameters
      as the new focus area.
    - Adds RCTCameraUtils.java file, so far with only a single function that helps compute the focus area
      from a motion event. This file can serve as a location for utility constants and functions for the
      rest of the app, where such things can be extracted out.

    Improved (continuous) auto-focus

    - Use FOCUS_MODE_CONTINUOUS_PICTURE/VIDEO when possible to enable continuous auto-focus; fall back to
      FOCUS_MODE_AUTO otherwise, if able.

    Other changes

    - Update README to specify differences between iOS and Android for focus and zoom functionality.
    - Update AndroidManifest with more thorough list of permissions and features.
    - Update Example package.json react and react-native dependencies to match root package's package.json.

    * Example: default empty onFocusChanged callback

    - Enables default tap-to-focus behavior in Example app, facilitating
      testing of focus features in the Example app
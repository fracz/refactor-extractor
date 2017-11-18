commit caf24fc2c4bb9747eb80138bf3ce0be067851749
Author: Jim Miller <jaggies@google.com>
Date:   Tue Sep 10 18:37:01 2013 -0700

    Add camera affordance to navigation bar on phones

    This adds a camera button on phones that can be used to show
    and launch the camera.

    - Minor refactoring of touch event dispatch in PagedView.
    - Disables usability hints when keyguard loads.
    - Only add a touch handler for camera icon once during layout.
    - Update after review.
    - Updated with latest UX camera and camera background assets

    Change-Id: I09cd5cb0e501fd0f4659bea96d00c92b07f805c4
commit e663cb77281c4c76241b820f6126543f1c2d859f
Author: Ruben Brunk <rubenbrunk@google.com>
Date:   Tue Sep 16 13:18:31 2014 -0700

    camera2: refactor LEGACY mode error handling.

    Bug: 17431462
    Bug: 17407537

    - Add Surface format/size validation during configure.
    - Update exception error codes used in binder calls.
    - Report dropped requests, frames, captures, and device
      errors in binder callback properly.
    - Fixes CameraDeviceTest errors for incorrect metering
      rectangle weight in template tests.
    - Fixes CameraDeviceTest errors for missing
      noiseReductionMode field in template tests.
    - Implement flush call.

    Change-Id: I0da803bccf2bfb9b4c0cf61208e160a86c577497
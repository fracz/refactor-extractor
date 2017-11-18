commit 9503785393ba8999473b7a924ede2baf520e367c
Author: Eino-Ville Talvala <etalvala@google.com>
Date:   Sun Sep 14 14:07:05 2014 -0700

    Camera2: Clean up corner case error handling

    - If a session is closed, and a new session is created immediately
    afterwards, but then fails to be configured, the first session sees an
    onUnconfigured call which it wasn't expecting, and throws an
    exception on an internal thread, leading to app death.
    Add a guard against this case.

    - If the lower levels skip a frame (illegal per design), be slightly more
    robust to that by accepting any successful result as the latest completed
    frame, instead of just incrementing the completed frame count. This will
    lead to missing results, but should allow shutdown, etc, to complete
    cleanly.

    - Convert TIMED_OUT error codes to CAMERA_ERROR CameraAccessExceptions.
    This is a common error code returned by waitUntilIdle.

    Also, improve debug logging to log a session ID with verbose logging,
    and add a few verbose logs.

    Bug: 16899526
    Change-Id: I7a31f0a12effc2611e1f9c2408224ee82c37c912
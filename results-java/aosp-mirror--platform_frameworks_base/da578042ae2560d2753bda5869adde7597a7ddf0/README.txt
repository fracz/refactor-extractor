commit da578042ae2560d2753bda5869adde7597a7ddf0
Author: fionaxu <fionaxu@google.com>
Date:   Fri Mar 10 10:16:09 2017 -0800

    integrate portal webview to the default app

    Based on the UX review feedback, we plan to implement portal webview
    function inside the carrier default app instead of reusing the existing
    portal app. This will give us more flexibility and control, also will
    improve UX flow by getting rid of the some unwanted dialogues.

    Test: Manual
    Bug: 36002256
    Change-Id: I2627d5a43039ce433006c058bb4f2c1a39113e59
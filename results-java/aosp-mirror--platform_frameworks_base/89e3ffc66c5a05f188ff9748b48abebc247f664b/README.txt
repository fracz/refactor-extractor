commit 89e3ffc66c5a05f188ff9748b48abebc247f664b
Author: Phil Weaver <pweaver@google.com>
Date:   Mon Sep 19 13:51:10 2016 -0700

    Add tests for MagnificationController.

    Also refactoring the class to make it easier to test and
    chaning behavior where the current behavior seemed poorly
    defined.

    Refactoring:
    - Combined all handlers into one.
    - Simplified animation to use a ValueAnimator.
    - Eliminated ACCESSIBILITY_DISPLAY_MAGNIFICATION_AUTO_UPDATE
      setting. Move rest of settings reading into mockable class.
    - Move callbacks from WindowManager into the main class.
    - Pulled out my instrumented Handler from the
      MotionEventInjectorTest into its own class so I can reuse
      it.

    Behavior changes:
    - Always constraining out-of-bounds values rather than
      refusing to change them.
    - Constraining offsets on bounds changes. We previously
      left them alone, even if they were out of bounds.
    - Keeping track of the animation starting point. We were
      interpolating between the current magnification spec
      and the final one. This change means the magnification
      animates to a different profile.

    Test: This CL adds tests. I've also run a11y CTS.

    Bugs: 31855954, 30325691

    Change-Id: Ie00e29ae88b75d9fe1016f9d107257c9cf6425bb
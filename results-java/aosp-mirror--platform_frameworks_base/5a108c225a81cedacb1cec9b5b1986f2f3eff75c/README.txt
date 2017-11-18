commit 5a108c225a81cedacb1cec9b5b1986f2f3eff75c
Author: Jorim Jaggi <jjaggi@google.com>
Date:   Thu Oct 13 14:33:27 2016 +0200

    The big keyguard transition refactor (2/n)

    Introduce UnknownVisibilityController, which keeps track of apps that
    may or may not be visible when launching an activity behind Keyguard.
    When Keyguard is occluded and we launch another activity, we don't
    know whether we still have to keep Keyguard occluded until the app
    has gone through the resume path and issued a relayout call to update
    the Keyguard flags.

    This class keeps track of that state and defers the app transition
    until the unknown visibility of all apps is resolved.

    Test:
    1) Have an occluding activity that starts another occluding
    activity, ensure that there is no flicker.
    2) Have an occluding activity while the Keyguard is insecure, start
    a DISMISS_KEYGUARD activity, ensure there is no flicker.
    3) runtest frameworks-services -c com.android.server.wm.UnknownVisibilityControllerTest

    Bug: 32057734
    Change-Id: I5145b272722ab8c31dd7c5383286f5c9473e26a4
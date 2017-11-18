commit 9b0dc2894df1c3d26aa6196ecdef1989967e6ec9
Author: Adam Powell <adamp@google.com>
Date:   Wed Jul 31 13:58:43 2013 -0700

    Fix a regression where android:windowContentOverlay did not draw properly.

    This was the victim of an earlier refactoring. Have the
    ActionBarOverlayLayout draw this directly over the content so that it
    can stay properly in sync with any animations and also remove an extra
    couple of views from the decor layout.

    Some apps now expect the broken behavior in default themes. Protect
    them from themselves until they bump their targetSdkVersion.

    Public bug https://code.google.com/p/android/issues/detail?id=58280

    Change-Id: I4284503577e322f3e68d4a7fabda8441d3749b98
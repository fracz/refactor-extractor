commit 0594476ffa39481063cf7f1546ccfce509f02c65
Author: Amith Yamasani <yamasani@google.com>
Date:   Fri Oct 8 13:52:38 2010 -0700

    SearchView: Add some more listeners, IME dismiss improvements and focus control.

    Addresses following bugs:
    3067611 : Submit button incorrectly displayed
    3064371 : Callback to report suggestion click
    3008580 : Setting initial focus

    The SearchView will take initial focus now, reverting an earlier change. If
    you don't want it to take initial focus, then you must requestFocus on a
    different view on launching the activity, since the initial focus is desirable
    in other cases. This is normal behavior for all EditText widgets and SearchView
    shouldn't have a different behavior.
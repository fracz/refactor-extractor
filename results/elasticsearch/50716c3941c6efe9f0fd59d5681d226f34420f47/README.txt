commit 50716c3941c6efe9f0fd59d5681d226f34420f47
Author: Ryan Ernst <ryan@iernst.net>
Date:   Fri Dec 4 14:10:16 2015 -0800

    Plugins: Add nicer error message when an existing plugin's descriptor is missing

    Currently, when a user tries to install an old plugin (pre 2.x) on a 2.x
    node, the error message is cryptic (just printing the file path that was
    missing, when looking for the descriptor). This improves the message to
    be more explicit that the descriptor is missing, and suggests the
    problem might be the plugin was built before 2.0.

    closes #15197
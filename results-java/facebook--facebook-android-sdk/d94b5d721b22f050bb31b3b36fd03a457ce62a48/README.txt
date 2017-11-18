commit d94b5d721b22f050bb31b3b36fd03a457ce62a48
Author: Chris Lang <clang@fb.com>
Date:   Tue Nov 6 14:34:11 2012 -0800

    [android-sdk] Fix error handling in WebDialog.

    Summary:
    Last-minute refactoring prior to commit reversed the case of an if clause, meaning we interpreted successful WebDialog
    results as errors. Fixing that.

    Test Plan:
    - Ran FriendPickerSample, was able to log in when no access token was cached (failed previously)

    Revert Plan:

    Reviewers: mmarucheck, mingfli, karthiks

    Reviewed By: karthiks

    Differential Revision: https://phabricator.fb.com/D622520

    Task ID: 1768823
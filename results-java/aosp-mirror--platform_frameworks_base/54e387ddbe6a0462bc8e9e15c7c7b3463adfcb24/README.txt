commit 54e387ddbe6a0462bc8e9e15c7c7b3463adfcb24
Author: Robert Sesek <rsesek@google.com>
Date:   Fri Dec 2 17:27:50 2016 -0500

    Dynamically add the webview_zygote's preloaded APK to the zygote FD whitelist.

    This refactors the whitelist to be a class, rather than just a static C array.
    The whitelist can then be augmented dynamically when the package path is known
    in the webview_zygote.

    Test: m
    Test: sailfish boots
    Test: Enable Multi-process WebView in developer options, perform a search in GSA.

    Bug: 21643067
    Change-Id: Ia1f2535c7275b42b309631b4fe7859c30cbf7309
    (cherry picked from commit 061ee3088a79ab0e07d37d1c0897d51422f29c4e)
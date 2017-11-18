commit d9730182212b157083321ea4e2209182f1fbb72f
Author: Gustav Sennton <gsennton@google.com>
Date:   Thu Jun 18 16:56:26 2015 +0100

    Revert "Load WebView from one out of a list of packages."

    This was not a clean revert!

    This reverts commit 2ed6fee15c85ff991f64ecfa8c1c4738e0fdf9b6.
    We essentially only revert the functionality for going through a list of
    WebView package names and picking the first compatible one.
    Except for that functionality we also fetched the name of the shared
    library from a flag in WebView and made some minor refactoring in the
    initial commit, these changes have been left alone in this revert.

    Bug: 21893371
    Change-Id: Idb2539dc33cc5f9e2894ecd665c23573c6cba9f3
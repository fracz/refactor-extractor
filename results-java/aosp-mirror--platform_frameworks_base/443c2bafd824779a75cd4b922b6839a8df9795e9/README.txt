commit 443c2bafd824779a75cd4b922b6839a8df9795e9
Author: Yohei Yukawa <yukawa@google.com>
Date:   Wed Sep 10 14:10:30 2014 +0900

    Use public APIs to instantiate InputMethodSubtype

    This is a groundwork for subsequent CLs that are
    supposed to improve default input method selection
    logics.

    Historically we have had a @hide constructor of
    InputMethodSubtype. However, this contructor is
    a bit obsolete because we can not specify some
    parameters that were added in recent platform
    releases. We should use InputMethodSubtypeBuilder
    instead.

    BUG: 17347871
    Change-Id: I72ad79682a58344e14380eb20e26edf98aee37cd
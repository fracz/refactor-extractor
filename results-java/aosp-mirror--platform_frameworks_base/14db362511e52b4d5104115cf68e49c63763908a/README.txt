commit 14db362511e52b4d5104115cf68e49c63763908a
Author: Winson <winsonc@google.com>
Date:   Mon May 9 13:34:07 2016 -0700

    Fixing regression in call to showRecents().

    - Previously, the call was made to showRecents() which took a single
      boolean to determine alt-tab state, and after the refactoring, it
      no longer made the same call.

    Bug: 28663474
    Change-Id: I75fb793e56c9a094a4372d7157dbd0dd7ecdbda7